package com.northernwall.hadrian;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.google.gson.Gson;
import com.northernwall.hadrian.handler.ContentHandler;
import com.northernwall.hadrian.handler.GraphHandler;
import com.northernwall.hadrian.handler.ServiceHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);
    private Properties properties;
    private SoaRepDataAccess dataAccess;
    private Gson gson;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main main = new Main();
        main.loadConfig(args);
        main.startLogging();
        main.startCouch();
        main.startJetty();
    }

    private void loadConfig(String[] args) {
        String filename;
        properties = new Properties();
        if (args == null || args.length == 0) {
            System.out.println("Missing command line argument properties filename, using hadrian.properties");
            filename = "hadrian.properties";
        } else {
            filename = args[0];
        }
        try {
            properties.load(new FileInputStream(filename));
        } catch (IOException ex) {
            System.out.println("Can not load properties from " + filename + ", using defaults");
            properties = new Properties();
        }
    }

    private void startLogging() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        String filename = properties.getProperty("logback.filename","logback.xml");
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.doConfigure(new File(filename));
        } catch (JoranException je) {
            System.out.println("Can not load logback config from " + filename + ", exiting");
            System.exit(0);
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

    private void startCouch() {
        CouchDbProperties dbProperties = new CouchDbProperties()
                .setDbName(properties.getProperty("couchdb.name","soarep"))
                .setCreateDbIfNotExist(Boolean.parseBoolean(properties.getProperty("couchdb.if-not-exist", "true")))
                .setProtocol(properties.getProperty("couchdb.protocol", "http"))
                .setHost(properties.getProperty("couchdb.host", "127.0.0.1"))
                .setPort(Integer.parseInt(properties.getProperty("couchdb.port","5984")))
                .setMaxConnections(100)
                .setConnectionTimeout(0);
        CouchDbClient client = new CouchDbClient(dbProperties);
        gson = client.getGson();
        dataAccess = new SoaRepDataAccess(client);
        logger.info("Couch access established");
    }

    private void startJetty() {
        try {
            int port = Integer.parseInt(properties.getProperty("jetty.port", "9090"));
            Server server = new Server(new QueuedThreadPool(10, 5));

            ServerConnector connector = new ServerConnector(server);
            connector.setPort(port);
            connector.setIdleTimeout(Integer.parseInt(properties.getProperty("jetty.idleTimeout", "1000")));
            connector.setAcceptQueueSize(Integer.parseInt(properties.getProperty("jetty.acceptQueueSize", "100")));
            server.addConnector(connector);

            Handler contentHandler = new ContentHandler();
            Handler serviceHandler = new ServiceHandler(dataAccess, gson);
            Handler graphHandler = new GraphHandler(dataAccess, gson);

            HandlerList handlers = new HandlerList();
            handlers.addHandler(contentHandler);
            handlers.addHandler(serviceHandler);
            handlers.addHandler(graphHandler);
            server.setHandler(handlers);

            server.start();
            logger.info("Jetty server started on port {}, joining with server thread now", port);
            server.join();
        } catch (Exception ex) {
            logger.error("Exception {} occured", ex.getMessage());
        }
    }

}
