/*
 * Copyright 2015 Richard Thurston.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.northernwall.hadrian.db;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.parameters.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraDataAccessFactory implements DataAccessFactory, Runnable {
    private final static Logger logger = LoggerFactory.getLogger(CassandraDataAccessFactory.class);

    private Cluster cluster;
    private CassandraDataAccess dataAccess;

    @Override
    public DataAccess createDataAccess(Parameters parameters) {
        String node = parameters.getString(Const.CASS_NODE, Const.CASS_NODE_DEFAULT);
        String keyspace = parameters.getString(Const.CASS_KEY_SPACE, Const.CASS_KEY_SPACE_DEFAULT);
        String replicationFactor = parameters.getString(Const.CASS_REPLICATION_FACTOR, Const.CASS_REPLICATION_FACTOR_DEFAULT);

        connect(node);
        setup(keyspace, replicationFactor);
        
        Thread thread = new Thread(this);
        Runtime.getRuntime().addShutdownHook(thread);

        dataAccess = new CassandraDataAccess(cluster, keyspace);
        return dataAccess;
    }

    private void connect(String node) {        
        cluster = Cluster.builder().addContactPoint(node).build();
        Metadata metadata = cluster.getMetadata();
        logger.info("Connected to cluster: {}", metadata.getClusterName());
        for (Host host : metadata.getAllHosts()) {
            logger.info("Datacenter: {} Host: {} Rack: {}", host.getDatacenter(), host.getAddress(), host.getRack());
        }
    }

    private void setup(String keyspace, String replicationFactor) {
        Session session = cluster.connect();

        session.execute("CREATE KEYSPACE IF NOT EXISTS "+keyspace+" WITH replication = {'class':'SimpleStrategy', 'replication_factor':"+replicationFactor+"};");
        logger.info("Keyspace created");
        
        session = cluster.connect();
        session.execute("CREATE TABLE IF NOT EXISTS "+keyspace+".customFunction (serviceId text, id text, data text, PRIMARY KEY (serviceId, id));");
        session.execute("CREATE TABLE IF NOT EXISTS "+keyspace+".dataStore (serviceId text, id text, data text, PRIMARY KEY (serviceId, id));");
        session.execute("CREATE TABLE IF NOT EXISTS "+keyspace+".host (serviceId text, id text, data text, PRIMARY KEY (serviceId, id));");
        session.execute("CREATE TABLE IF NOT EXISTS "+keyspace+".service (id text, data text, PRIMARY KEY (id));");
        session.execute("CREATE TABLE IF NOT EXISTS "+keyspace+".serviceRefClient (clientServiceId text, serverServiceId text, PRIMARY KEY (clientServiceId, serverServiceId));");
        session.execute("CREATE TABLE IF NOT EXISTS "+keyspace+".serviceRefServer (serverServiceId text, clientServiceId text, PRIMARY KEY (serverServiceId, clientServiceId));");
        session.execute("CREATE TABLE IF NOT EXISTS "+keyspace+".team (id text, data text, PRIMARY KEY (id));");
        session.execute("CREATE TABLE IF NOT EXISTS "+keyspace+".user (id text, data text, PRIMARY KEY (id));");
        session.execute("CREATE TABLE IF NOT EXISTS "+keyspace+".userSession (id text, data text, PRIMARY KEY (id));");
        session.execute("CREATE TABLE IF NOT EXISTS "+keyspace+".vip (serviceId text, id text, data text, PRIMARY KEY (serviceId, id));");
        session.execute("CREATE TABLE IF NOT EXISTS "+keyspace+".vipRefHost (hostId text, vipId text, data text, PRIMARY KEY (hostId, vipId));");
        session.execute("CREATE TABLE IF NOT EXISTS "+keyspace+".vipRefVip(vipId text, hostId text, PRIMARY KEY (vipId, hostId));");
        session.execute("CREATE TABLE IF NOT EXISTS "+keyspace+".workItem (id text, data text, PRIMARY KEY (id));");
        logger.info("Tables created");

        session.close();
    }

    @Override
    public void run() {
        dataAccess.close();
        cluster.close();
        logger.info("Connection to cluster closed");
    }

}
