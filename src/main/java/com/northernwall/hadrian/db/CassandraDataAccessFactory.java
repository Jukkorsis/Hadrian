package com.northernwall.hadrian.db;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;
import com.northernwall.hadrian.Const;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraDataAccessFactory implements DataAccessFactory, Runnable {
    private final static Logger logger = LoggerFactory.getLogger(CassandraDataAccessFactory.class);

    private Cluster cluster;

    @Override
    public DataAccess createDataAccess(Properties properties) {
        String node = properties.getProperty(Const.CASS_NODE, Const.CASS_NODE_DEFAULT);
        String keyspace = properties.getProperty(Const.CASS_KEY_SPACE, Const.CASS_KEY_SPACE_DEFAULT);
        String replicationFactor = properties.getProperty(Const.CASS_REPLICATION_FACTOR, Const.CASS_REPLICATION_FACTOR_DEFAULT);

        connect(node);
        setup(keyspace, replicationFactor);
        
        Thread thread = new Thread(this);
        Runtime.getRuntime().addShutdownHook(thread);

        return new CassandraDataAccess(cluster, keyspace);
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
        
        session.execute("CREATE TABLE IF NOT EXISTS "+keyspace+".customFunction (serviceId text, id text, data text, PRIMARY KEY (serviceId, id));");
        session.execute("CREATE TABLE IF NOT EXISTS "+keyspace+".dataStore (serviceId text, id text, data text, PRIMARY KEY (serviceId, id));");
        session.execute("CREATE TABLE IF NOT EXISTS "+keyspace+".host (serviceId text, id text, data text, PRIMARY KEY (serviceId, id));");
        session.execute("CREATE TABLE IF NOT EXISTS "+keyspace+".service (id text, data text, PRIMARY KEY (id));");
        session.execute("CREATE TABLE IF NOT EXISTS "+keyspace+".team (id text, data text, PRIMARY KEY (id));");
        session.execute("CREATE TABLE IF NOT EXISTS "+keyspace+".user (id text, data text, PRIMARY KEY (id));");
        session.execute("CREATE TABLE IF NOT EXISTS "+keyspace+".userSession (id text, data text, PRIMARY KEY (id));");
        session.execute("CREATE TABLE IF NOT EXISTS "+keyspace+".vip (serviceId text, id text, data text, PRIMARY KEY (serviceId, id));");
        session.execute("CREATE TABLE IF NOT EXISTS "+keyspace+".workItem (id text, data text, PRIMARY KEY (id));");
        logger.info("Tables created");
    }

    @Override
    public void run() {
        cluster.close();
        logger.info("Connection to cluster closed");
    }

}
