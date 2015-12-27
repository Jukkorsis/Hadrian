package com.northernwall.hadrian;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.northernwall.hadrian.access.AccessHandlerFactory;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.db.DataAccessFactory;
import com.northernwall.hadrian.domain.Config;
import com.northernwall.hadrian.graph.GraphHandler;
import com.northernwall.hadrian.maven.MavenHelper;
import com.northernwall.hadrian.maven.MavenHelperFactory;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.proxy.PostProxyHandler;
import com.northernwall.hadrian.proxy.PreProxyHandler;
import com.northernwall.hadrian.service.ConfigHandler;
import com.northernwall.hadrian.service.CustomFuntionHandler;
import com.northernwall.hadrian.service.DataStoreHandler;
import com.northernwall.hadrian.service.HostHandler;
import com.northernwall.hadrian.service.ServiceHandler;
import com.northernwall.hadrian.service.TeamHandler;
import com.northernwall.hadrian.service.UserHandler;
import com.northernwall.hadrian.service.VipHandler;
import com.northernwall.hadrian.service.WorkItemHandler;
import com.northernwall.hadrian.service.helper.HostDetailsHelper;
import com.northernwall.hadrian.service.helper.InfoHelper;
import com.northernwall.hadrian.tree.TreeHandler;
import com.northernwall.hadrian.utilityHandlers.AvailabilityHandler;
import com.northernwall.hadrian.utilityHandlers.ContentHandler;
import com.northernwall.hadrian.utilityHandlers.RedirectHandler;
import com.northernwall.hadrian.webhook.WebHookCallbackHandler;
import com.northernwall.hadrian.webhook.WebHookSender;
import com.northernwall.hadrian.webhook.WebHookSenderFactory;
import com.northernwall.hadrian.webhook.simple.SimpleWebHookHandler;
import com.northernwall.hadrian.webhook.simple.SimpleWebHookSender;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.OkHttpClient;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.net.BindException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;

public class Hadrian {

    private final static Logger logger = LoggerFactory.getLogger(Hadrian.class);

    private final Parameters parameters;
    private Config config;
    private DataAccess dataAccess;
    private OkHttpClient client;
    private MavenHelper mavenHelper;
    private AccessHelper accessHelper;
    private Handler accessHandler;
    private WebHookSender webHookSender;
    private InfoHelper infoHelper;
    private HostDetailsHelper hostDetailsHelper;
    private int port;
    private Server server;

    public Hadrian(Parameters parameters) {
        this.parameters = parameters;
    }

    public void setup() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        startLogging();
        loadConfig();
        startDataAccess();
        startHelpers();
        setupJetty();
    }

    private void startLogging() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        String filename = parameters.getString(Const.LOGBACK_FILENAME, Const.LOGBACK_FILENAME_DEFAULT);
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
        String temp = parameters.getString(key, defaultValue);
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
        String factoryName = parameters.getString(Const.DATA_ACCESS_FACTORY_CLASS_NAME, Const.DATA_ACCESS_FACTORY_CLASS_NAME_DEFAULT);
        Class c = Class.forName(factoryName);
        DataAccessFactory factory = (DataAccessFactory) c.newInstance();
        dataAccess = factory.createDataAccess(parameters);
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

        String factoryName = parameters.getString(Const.MAVEN_HELPER_FACTORY_CLASS_NAME, Const.MAVEN_HELPER_FACTORY_CLASS_NAME_DEFAULT);
        Class c = Class.forName(factoryName);
        MavenHelperFactory mavenHelperFactory = (MavenHelperFactory) c.newInstance();
        mavenHelper = mavenHelperFactory.create(parameters, client);

        accessHelper = new AccessHelper(dataAccess);

        factoryName = parameters.getString(Const.ACCESS_HANDLER_FACTORY_CLASS_NAME, Const.ACCESS_HANDLER_FACTORY_CLASS_NAME_DEFAULT);
        c = Class.forName(factoryName);
        AccessHandlerFactory accessHanlderFactory = (AccessHandlerFactory) c.newInstance();
        accessHandler = accessHanlderFactory.create(accessHelper);

        factoryName = parameters.getString(Const.WEB_HOOK_SENDER_FACTORY_CLASS_NAME, Const.WEB_HOOK_SENDER_FACTORY_CLASS_NAME_DEFAULT);
        c = Class.forName(factoryName);
        WebHookSenderFactory webHookSenderFactory = (WebHookSenderFactory) c.newInstance();
        webHookSender = webHookSenderFactory.create(parameters, client);

        infoHelper = new InfoHelper(client);
        hostDetailsHelper = new HostDetailsHelper(client, parameters);
    }

    private void setupJetty() {
        port = Integer.parseInt(parameters.getString(Const.JETTY_PORT, Const.JETTY_PORT_DEFAULT));

        server = new Server(new QueuedThreadPool(10, 5));

        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSendServerVersion(false);
        HttpConnectionFactory httpFactory = new HttpConnectionFactory(httpConfig);
        ServerConnector connector = new ServerConnector(server, httpFactory);
        connector.setPort(port);
        connector.setIdleTimeout(Integer.parseInt(parameters.getString(Const.JETTY_IDLE_TIMEOUT, Const.JETTY_IDLE_TIMEOUT_DEFAULT)));
        connector.setAcceptQueueSize(Integer.parseInt(parameters.getString(Const.JETTY_ACCEPT_QUEUE_SIZE, Const.JETTY_ACCEPT_QUEUE_SIZE_DEFAULT)));
        server.addConnector(connector);

        HandlerList handlers = new HandlerList();
        handlers.addHandler(new AvailabilityHandler(accessHandler, dataAccess, mavenHelper));
        handlers.addHandler(new ContentHandler());
        handlers.addHandler(new WebHookCallbackHandler(dataAccess, webHookSender));
        if (webHookSender instanceof SimpleWebHookSender) {
            handlers.addHandler(new SimpleWebHookHandler(client, parameters));
        }
        handlers.addHandler(new PreProxyHandler());
        handlers.addHandler(accessHandler);
        handlers.addHandler(new PostProxyHandler(client));
        handlers.addHandler(new TreeHandler(dataAccess));
        handlers.addHandler(new UserHandler(accessHelper, dataAccess));
        handlers.addHandler(new TeamHandler(accessHelper, dataAccess));
        handlers.addHandler(new ServiceHandler(accessHelper, dataAccess, webHookSender, mavenHelper, infoHelper));
        handlers.addHandler(new VipHandler(accessHelper, dataAccess, webHookSender));
        handlers.addHandler(new HostHandler(accessHelper, config, dataAccess, webHookSender, client, hostDetailsHelper));
        handlers.addHandler(new CustomFuntionHandler(accessHelper, dataAccess, client));
        handlers.addHandler(new WorkItemHandler(dataAccess));
        handlers.addHandler(new DataStoreHandler(accessHelper, dataAccess));
        handlers.addHandler(new ConfigHandler(config));
        handlers.addHandler(new GraphHandler(dataAccess));
        handlers.addHandler(new RedirectHandler());
        server.setHandler(handlers);
    }

    public void start() {
        startJetty();
    }

    private void startJetty() {
        try {
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
