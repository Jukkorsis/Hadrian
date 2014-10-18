/*
 * Copyright 2014 Richard Thurston.
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

package com.northernwall.hadrian;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.google.gson.Gson;
import com.northernwall.hadrian.db.CouchDataAccess;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Config;
import com.northernwall.hadrian.domain.ConfigItem;
import com.northernwall.hadrian.handler.AvailabilityHandler;
import com.northernwall.hadrian.handler.ConfigHandler;
import com.northernwall.hadrian.handler.ContentHandler;
import com.northernwall.hadrian.handler.EnvHandler;
import com.northernwall.hadrian.handler.GraphHandler;
import com.northernwall.hadrian.handler.ImageHandler;
import com.northernwall.hadrian.handler.RedirectHandler;
import com.northernwall.hadrian.handler.ServiceHandler;
import com.northernwall.hadrian.handler.VersionHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.BindException;
import java.util.Properties;
import org.apache.http.Consts;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Thurston
 */
public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);
    private Properties properties;
    private DataAccess dataAccess;
    private CloseableHttpClient client;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Main main = new Main();
            main.loadProperties(args);
            main.startLogging();
            main.startDataAccess();
            main.checkConfig();
            main.startHttpClient();
            main.startJetty();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProperties(String[] args) {
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

        String filename = properties.getProperty("logback.filename", "logback.xml");
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

    private void startDataAccess() {
        dataAccess = new CouchDataAccess(properties);
    }

    private void checkConfig() {
        Config config = dataAccess.getConfig();
        ConfigItem item;
        boolean requiresUpdate = false;

        if (config.dataCenters.isEmpty()) {
            logger.info("Adding default data centers to config");
            
            item = new ConfigItem();
            item.code = "DC1";
            item.description = "The west coast data center";
            config.dataCenters.add(item);

            item = new ConfigItem();
            item.code = "DC2";
            item.description = "The east coast data center";
            config.dataCenters.add(item);

            item = new ConfigItem();
            item.code = "DC3";
            item.description = "The central data center";
            config.dataCenters.add(item);
            
            requiresUpdate = true;
        }

        if (config.teams.isEmpty()) {
            logger.info("Adding default teams to config");
            
            item = new ConfigItem();
            item.code = "My Team";
            item.description = "My Team";
            item.url = "https://github.com/Jukkorsis";
            config.teams.add(item);
            
            item = new ConfigItem();
            item.code = "Other Team";
            item.description = "Other Team";
            config.teams.add(item);
            
            requiresUpdate = true;
        }

        if (config.products.isEmpty()) {
            logger.info("Adding default products to config");
            
            item = new ConfigItem();
            item.code = "Product A";
            item.description = "My Team's Product";
            item.url = "https://github.com/Jukkorsis";
            config.products.add(item);
            
            item = new ConfigItem();
            item.code = "Product B";
            item.description = "Other Team's Product";
            config.products.add(item);
            
            requiresUpdate = true;
        }

        if (config.haDimensions.isEmpty()) {
            logger.info("Adding default HA Dimensions to config");
            
            ConfigItem dimension = new ConfigItem();
            dimension.code = "Points of Failure";
            item = new ConfigItem();
            item.code = "None";
            item.description = "No single point of failure, 3+ data centers in Active-Active configuration";
            dimension.subItems.add(item);
            item = new ConfigItem();
            item.code = "Active-Standby";
            item.description = "Some compoents exist in 2 data centers in Active-Standby configuration";
            dimension.subItems.add(item);
            item = new ConfigItem();
            item.code = "Single DC";
            item.description = "Some compoents exist on 2+ hosts, but in a single data center";
            dimension.subItems.add(item);
            item = new ConfigItem();
            item.code = "Single Host";
            item.description = "Some compoents exist on a single host";
            dimension.subItems.add(item);
            config.haDimensions.add(dimension);

            dimension = new ConfigItem();
            dimension.code = "Intervention";
            item = new ConfigItem();
            item.code = "None";
            item.description = "No manual intervention is required to respond to a failure";
            dimension.subItems.add(item);
            item = new ConfigItem();
            item.code = "short";
            item.description = "Manual intervention is required to respond to a failure. Process once started take less than 15 minutes to complete.";
            dimension.subItems.add(item);
            item = new ConfigItem();
            item.code = "Long";
            item.description = "Manual intervention is required to respond to a failure. Process once started take more than 15 minutes to complete.";
            dimension.subItems.add(item);
            config.haDimensions.add(dimension);
            
            requiresUpdate = true;
        }
        
        if (config.classDimensions.isEmpty()) {
            logger.info("Adding default Classification Dimensions to config");
            
            ConfigItem dimension = new ConfigItem();
            dimension.code = "Business Value";
            item = new ConfigItem();
            item.code = "High";
            item.description = "High Value services provides critical business functionality.";
            dimension.subItems.add(item);
            item = new ConfigItem();
            item.code = "Medium";
            item.description = "Medium Value service supports \"High\" Value services, for example reporting, configuration, logging systems.";
            dimension.subItems.add(item);
            item = new ConfigItem();
            item.code = "Low";
            item.description = "Low Value services provide non-critical business functionality, for example a system that stores meta-data about other services...doh.";
            dimension.subItems.add(item);
            config.classDimensions.add(dimension);

            dimension = new ConfigItem();
            dimension.code = "PII";
            item = new ConfigItem();
            item.code = "None";
            item.description = "Service does not store or process Personally Identifying Information.";
            dimension.subItems.add(item);
            item = new ConfigItem();
            item.code = "Processes";
            item.description = "Service processes Personally Identifying Information.";
            dimension.subItems.add(item);
            item = new ConfigItem();
            item.code = "Stores";
            item.description = "Service stores Personally Identifying Information.";
            dimension.subItems.add(item);
            config.classDimensions.add(dimension);

            dimension = new ConfigItem();
            dimension.code = "Sensitive";
            item = new ConfigItem();
            item.code = "Public";
            item.description = "service contains no sensitive information.";
            dimension.subItems.add(item);
            item = new ConfigItem();
            item.code = "NonPublic";
            item.description = "Service contains sensitive information to can not be shared with the public..";
            dimension.subItems.add(item);
            config.classDimensions.add(dimension);
            
            requiresUpdate = true;
        }
        
        if (requiresUpdate) {
            dataAccess.save(config);
        }
    }

    private void startHttpClient() {
        try {
            int maxConnections = Integer.parseInt(properties.getProperty("http.maxConnections", "100"));
            int maxPerRoute = Integer.parseInt(properties.getProperty("http.maxPerRoute", "10"));
            int socketTimeout = Integer.parseInt(properties.getProperty("http.socketTimeout", "1000"));
            int connectionTimeout = Integer.parseInt(properties.getProperty("http.connectionTimeout", "1000"));

            RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create();
            Registry<ConnectionSocketFactory> registry = registryBuilder.register("http", PlainConnectionSocketFactory.INSTANCE).build();

            PoolingHttpClientConnectionManager ccm = new PoolingHttpClientConnectionManager(registry);
            ccm.setMaxTotal(maxConnections);
            ccm.setDefaultMaxPerRoute(maxPerRoute);

            HttpClientBuilder clientBuilder = HttpClients.custom()
                    .setConnectionManager(ccm)
                    .setDefaultConnectionConfig(ConnectionConfig.custom()
                            .setCharset(Consts.UTF_8).build())
                    .setDefaultRequestConfig(RequestConfig.custom()
                            .setSocketTimeout(socketTimeout)
                            .setConnectTimeout(connectionTimeout).build());
            client = clientBuilder.build();
        } catch (NumberFormatException nfe) {
            throw new IllegalStateException("Error Creating HTTPClient, could not parse property");
        } catch (Exception e) {
            throw new IllegalStateException("Error Creating HTTPClient: ", e);
        }
    }

    private void startJetty() {
        int port = -1;
        try {
            Gson gson = new Gson();
            WarningProcessor warningProcessor = new WarningProcessor(dataAccess);

            port = Integer.parseInt(properties.getProperty("jetty.port", "9090"));
            Server server = new Server(new QueuedThreadPool(10, 5));

            HttpConfiguration httpConfig = new HttpConfiguration();
            httpConfig.setSendServerVersion(false);
            HttpConnectionFactory httpFactory = new HttpConnectionFactory(httpConfig);
            ServerConnector connector = new ServerConnector(server, httpFactory);
            connector.setPort(port);
            connector.setIdleTimeout(Integer.parseInt(properties.getProperty("jetty.idleTimeout", "1000")));
            connector.setAcceptQueueSize(Integer.parseInt(properties.getProperty("jetty.acceptQueueSize", "100")));
            server.addConnector(connector);

            Handler availabilityHandler = new AvailabilityHandler();
            Handler contentHandler = new ContentHandler();
            Handler configHandler = new ConfigHandler(dataAccess, gson);
            Handler serviceHandler = new ServiceHandler(dataAccess, gson, warningProcessor, client);
            Handler versionHandler = new VersionHandler(dataAccess, gson, warningProcessor);
            Handler envHandler = new EnvHandler(dataAccess, gson, client);
            Handler imageHandler = new ImageHandler(dataAccess, gson);
            Handler graphHandler = new GraphHandler(dataAccess, gson);
            Handler redirectHandler = new RedirectHandler();

            HandlerList handlers = new HandlerList();
            handlers.addHandler(availabilityHandler);
            handlers.addHandler(contentHandler);
            handlers.addHandler(configHandler);
            handlers.addHandler(serviceHandler);
            handlers.addHandler(versionHandler);
            handlers.addHandler(envHandler);
            handlers.addHandler(imageHandler);
            handlers.addHandler(graphHandler);
            handlers.addHandler(redirectHandler);
            server.setHandler(handlers);

            server.start();
            logger.info("Jetty server started on port {}, joining with server thread now", port);
            server.join();
        } catch (BindException be) {
            logger.error("Can not bind to port {}, exiting", port);
            System.exit(0);
        } catch (Exception ex) {
            logger.error("Exception {} occured", ex.getMessage());
        }
    }

}
