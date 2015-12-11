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

import com.northernwall.hadrian.webhook.WebHookSender;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.northernwall.hadrian.access.Access;
import com.northernwall.hadrian.access.AccessFactory;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.db.DataAccessFactory;
import com.northernwall.hadrian.utilityHandlers.AvailabilityHandler;
import com.northernwall.hadrian.utilityHandlers.ContentHandler;
import com.northernwall.hadrian.service.DataStoreHandler;
import com.northernwall.hadrian.service.VipHandler;
import com.northernwall.hadrian.graph.GraphHandler;
import com.northernwall.hadrian.service.ConfigHandler;
import com.northernwall.hadrian.service.CustomFuntionHandler;
import com.northernwall.hadrian.service.HostHandler;
import com.northernwall.hadrian.service.helper.InfoHelper;
import com.northernwall.hadrian.maven.MavenHelper;
import com.northernwall.hadrian.maven.MavenHelperFactory;
import com.northernwall.hadrian.utilityHandlers.RedirectHandler;
import com.northernwall.hadrian.service.ServiceHandler;
import com.northernwall.hadrian.service.TeamHandler;
import com.northernwall.hadrian.tree.TreeHandler;
import com.northernwall.hadrian.access.LoginHandler;
import com.northernwall.hadrian.domain.Config;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.proxy.PostProxyHandler;
import com.northernwall.hadrian.proxy.PreProxyHandler;
import com.northernwall.hadrian.service.UserHandler;
import com.northernwall.hadrian.service.WorkItemHandler;
import com.northernwall.hadrian.webhook.WebHookCallbackHandler;
import com.northernwall.hadrian.webhook.WebHookHandler;
import com.northernwall.hadrian.webhook.WebHookSenderFactory;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.OkHttpClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.BindException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
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
    private Config config;
    private DataAccess dataAccess;
    private OkHttpClient client;
    private MavenHelper mavenHelper;
    private Access access;
    private WebHookSender webHookSender;
    private InfoHelper infoHelper;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Main main = new Main();
            main.loadProperties(args);
            main.startLogging();
            main.loadConfig();
            main.startDataAccess();
            main.startHelpers();
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
            filename = Const.PROPERTIES_FILENAME;
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

        String filename = properties.getProperty(Const.LOGBACK_FILENAME, Const.LOGBACK_FILENAME_DEFAULT);
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        context.reset();
        File file = new File(filename);
        try {
            if (file.exists()) {
                configurator.doConfigure(file);
            } else {
                System.out.println("Can not load logback config from " + filename + ", using defaults");
                configurator.doConfigure(this.getClass().getResourceAsStream("/" + Const.LOGBACK_FILENAME_DEFAULT));
            }
        } catch (JoranException je) {
            System.out.println("Could not find/load logback config file, exiting");
            System.exit(0);
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }
    
    private void loadConfig(String key, String defaultValue, List<String> target) {
        String temp = properties.getProperty(key, defaultValue);
        String[] parts = temp.split(",");
        for (String part : parts) {
            part = part.trim();
            if (!part.isEmpty()) {
                target.add(part);
            }
        }
    }
    
    private void loadConfig() {
        config = new Config();
        
        loadConfig(Const.CONFIG_DATA_CENTERS, Const.CONFIG_DATA_CENTERS_DEFAULT, config.dataCenters);
        loadConfig(Const.CONFIG_NETWORKS, Const.CONFIG_NETWORKS_DEFAULT, config.networks);
        loadConfig(Const.CONFIG_ENVSS, Const.CONFIG_ENVS_DEFAULT, config.envs);
        loadConfig(Const.CONFIG_SIZES, Const.CONFIG_SIZES_DEFAULT, config.sizes);
        loadConfig(Const.CONFIG_PROTOCOLS, Const.CONFIG_PROTOCOLS_DEFAULT, config.protocols);
        loadConfig(Const.CONFIG_DOMAINS, Const.CONFIG_DOMAINS_DEFAULT, config.domains);
        loadConfig(Const.CONFIG_ARTIFACT_TYPES, Const.CONFIG_ARTIFACT_TYPES_DEFAULT, config.artifactTypes);
    }

    private void startDataAccess() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        String factoryName = properties.getProperty(Const.DATA_ACCESS_FACTORY_CLASS_NAME, Const.DATA_ACCESS_FACTORY_CLASS_NAME_DEFAULT);
        Class c = Class.forName(factoryName);
        DataAccessFactory factory = (DataAccessFactory)c.newInstance();
        dataAccess = factory.createDataAccess(properties);
        
        List<User> users = dataAccess.getUsers();
        if (users == null || users.isEmpty()) {
            logger.info("No users found, creating default 'admin' user");
            dataAccess.saveUser(new User("admin", "Administator", true, true));
        }
        
        com.northernwall.hadrian.db.InitData.init(dataAccess);
    }

    private void startHelpers() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        try {
            client = new OkHttpClient();
            client.setConnectTimeout(2, TimeUnit.SECONDS);
            client.setReadTimeout(2, TimeUnit.SECONDS);
            client.setWriteTimeout(2, TimeUnit.SECONDS);
            client.setFollowSslRedirects(false);
            client.setFollowRedirects(false);
            client.setConnectionPool(new ConnectionPool(5, 60 * 1000));
        } catch (NumberFormatException nfe) {
            throw new IllegalStateException("Error Creating HTTPClient, could not parse property");
        } catch (Exception e) {
            throw new IllegalStateException("Error Creating HTTPClient: ", e);
        }
        
        String factoryName = properties.getProperty(Const.MAVEN_HELPER_FACTORY_CLASS_NAME, Const.MAVEN_HELPER_FACTORY_CLASS_NAME_DEFAULT);
        Class c = Class.forName(factoryName);
        MavenHelperFactory mavenFactory = (MavenHelperFactory)c.newInstance();
        mavenHelper = mavenFactory.create(properties, client);
        
        factoryName = properties.getProperty(Const.ACCESS_FACTORY_CLASS_NAME, Const.ACCESS_FACTORY_CLASS_NAME_DEFAULT);
        c = Class.forName(factoryName);
        AccessFactory accessFactory = (AccessFactory)c.newInstance();
        access = accessFactory.create(dataAccess);
        
        factoryName = properties.getProperty(Const.WEB_HOOK_SENDER_FACTORY_CLASS_NAME, Const.WEB_HOOK_SENDER_FACTORY_CLASS_NAME_DEFAULT);
        c = Class.forName(factoryName);
        WebHookSenderFactory webHookSenderFactory = (WebHookSenderFactory)c.newInstance();
        webHookSender = webHookSenderFactory.create(properties, client);
        
        infoHelper = new InfoHelper(properties, client);
    }

    private void startJetty() {
        int port = Integer.parseInt(properties.getProperty(Const.JETTY_PORT, Const.JETTY_PORT_DEFAULT));
        try {
            Server server = new Server(new QueuedThreadPool(10, 5));

            HttpConfiguration httpConfig = new HttpConfiguration();
            httpConfig.setSendServerVersion(false);
            HttpConnectionFactory httpFactory = new HttpConnectionFactory(httpConfig);
            ServerConnector connector = new ServerConnector(server, httpFactory);
            connector.setPort(port);
            connector.setIdleTimeout(Integer.parseInt(properties.getProperty(Const.JETTY_IDLE_TIMEOUT, Const.JETTY_IDLE_TIMEOUT_DEFAULT)));
            connector.setAcceptQueueSize(Integer.parseInt(properties.getProperty(Const.JETTY_ACCEPT_QUEUE_SIZE, Const.JETTY_ACCEPT_QUEUE_SIZE_DEFAULT)));
            server.addConnector(connector);

            HandlerList handlers = new HandlerList();
            handlers.addHandler(new AvailabilityHandler(access, dataAccess, mavenHelper));
            handlers.addHandler(new WebHookCallbackHandler(dataAccess, webHookSender));
            handlers.addHandler(new WebHookHandler(client, properties));
            handlers.addHandler(new PreProxyHandler());
            handlers.addHandler(new LoginHandler(access));
            handlers.addHandler(new PostProxyHandler(client));
            handlers.addHandler(new ContentHandler());
            handlers.addHandler(new TreeHandler(dataAccess));
            handlers.addHandler(new UserHandler(access, dataAccess));
            handlers.addHandler(new TeamHandler(access, dataAccess));
            handlers.addHandler(new ServiceHandler(access, dataAccess, webHookSender, mavenHelper, infoHelper));
            handlers.addHandler(new VipHandler(access, dataAccess, webHookSender));
            handlers.addHandler(new HostHandler(access, config, dataAccess, webHookSender, client, properties));
            handlers.addHandler(new CustomFuntionHandler(access, dataAccess, client));
            handlers.addHandler(new WorkItemHandler(dataAccess));
            handlers.addHandler(new DataStoreHandler(access, dataAccess));
            handlers.addHandler(new ConfigHandler(config));
            handlers.addHandler(new GraphHandler(dataAccess));
            handlers.addHandler(new RedirectHandler());
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