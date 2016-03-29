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
package com.northernwall.hadrian.db.cassandra;

import com.codahale.metrics.MetricRegistry;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.db.DataAccessFactory;
import com.northernwall.hadrian.parameters.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraDataAccessFactory implements DataAccessFactory, Runnable {

    private final static Logger logger = LoggerFactory.getLogger(CassandraDataAccessFactory.class);

    private Cluster cluster;
    private CassandraDataAccess dataAccess;

    @Override
    public DataAccess createDataAccess(Parameters parameters, MetricRegistry metricRegistry) {
        String nodes = parameters.getString(Const.CASS_NODES, Const.CASS_NODES_DEFAULT);
        String dataCenter = parameters.getString(Const.CASS_DATA_CENTER, null);
        String username = parameters.getString(Const.CASS_USERNAME, null);
        String password = parameters.getString(Const.CASS_PASSWORD, null);
        boolean createKeyspace = parameters.getBoolean(Const.CASS_CREATE_KEY_SPACE, Const.CASS_CREATE_KEY_SPACE_DEFAULT);
        String keyspace = parameters.getString(Const.CASS_KEY_SPACE, Const.CASS_KEY_SPACE_DEFAULT);
        int replicationFactor = parameters.getInt(Const.CASS_REPLICATION_FACTOR, Const.CASS_REPLICATION_FACTOR_DEFAULT);
        int auditTimeToLive = parameters.getInt(Const.CASS_AUDIT_TTL_DAYS, Const.CASS_AUDIT_TTL_DAYS_DEFAULT) * 86_400;

        connect(nodes, dataCenter, username, password);

        setup(createKeyspace, keyspace, replicationFactor);

        Thread thread = new Thread(this);
        Runtime.getRuntime().addShutdownHook(thread);

        dataAccess = new CassandraDataAccess(cluster, keyspace, username, dataCenter, auditTimeToLive, metricRegistry);
        return dataAccess;
    }

    private void connect(String nodes, String dataCenter, String username, String password) {
        Builder builder = Cluster.builder();
        if (nodes == null || nodes.isEmpty()) {
            throw new RuntimeException(Const.CASS_NODES + " is not defined");
        }
        if (dataCenter != null && !dataCenter.isEmpty()) {
            builder.withLoadBalancingPolicy(new DCAwareRoundRobinPolicy(dataCenter));
        }
        String[] nodeParts = nodes.split(",");
        for (String node : nodeParts) {
            node = node.trim();
            if (!node.isEmpty()) {
                logger.info("Adding Cassandra node {}", node);
                builder.addContactPoint(node);
            }
        }
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            builder.withCredentials(username, password);
        }
        cluster = builder.build();
        Metadata metadata = cluster.getMetadata();
        logger.info("Connected to cluster: {}", metadata.getClusterName());
        for (Host host : metadata.getAllHosts()) {
            logger.info("Datacenter: {} Host: {} Rack: {}", host.getDatacenter(), host.getAddress(), host.getRack());
        }
    }

    private void setup(boolean createKeyspace, String keyspace, int replicationFactor) {
        Session session = cluster.connect();

        if (createKeyspace) {
            session.execute("CREATE KEYSPACE IF NOT EXISTS " + keyspace + " WITH replication = {'class':'SimpleStrategy', 'replication_factor':" + replicationFactor + "};");
            logger.info("Keyspace {} created", keyspace);
        } else {
            logger.info("Not calling create keyspace for {}", keyspace);
        }

        //Version tables
        session.execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".version (component text, version text, PRIMARY KEY (component));");
        //Data tables
        session.execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".service (id text, data text, PRIMARY KEY (id));");
        session.execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".team (id text, data text, PRIMARY KEY (id));");
        session.execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".user (id text, data text, PRIMARY KEY (id));");
        session.execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".workItem (id text, data text, PRIMARY KEY (id));");
        //Data tables below Service
        session.execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".customFunction (serviceId text, id text, data text, PRIMARY KEY (serviceId, id));");
        session.execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".dataStore (serviceId text, id text, data text, PRIMARY KEY (serviceId, id));");
        session.execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".host (serviceId text, id text, data text, PRIMARY KEY (serviceId, id));");
        session.execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".module (serviceId text, id text, data text, PRIMARY KEY (serviceId, id));");
        session.execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".vip (serviceId text, id text, data text, PRIMARY KEY (serviceId, id));");
        //Ref tables
        session.execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".serviceRefClient (clientServiceId text, serverServiceId text, PRIMARY KEY (clientServiceId, serverServiceId));");
        session.execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".serviceRefServer (serverServiceId text, clientServiceId text, PRIMARY KEY (serverServiceId, clientServiceId));");
        session.execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".vipRefHost (hostId text, vipId text, data text, PRIMARY KEY (hostId, vipId));");
        session.execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".vipRefVip(vipId text, hostId text, PRIMARY KEY (vipId, hostId));");
        //Audit table
        session.execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".audit (serviceId text, time timeuuid, data text, PRIMARY KEY (serviceId, time));");
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
