package com.northernwall.hadrian;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.northernwall.hadrian.access.AccessHandlerFactory;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.db.DataAccessFactory;
import com.northernwall.hadrian.maven.MavenHelper;
import com.northernwall.hadrian.maven.MavenHelperFactory;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.webhook.WebHookSender;
import com.northernwall.hadrian.webhook.WebHookSenderFactory;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.OkHttpClient;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.server.Handler;

public class HadrianBuilder {

    private final Parameters parameters;
    private OkHttpClient client;
    private DataAccess dataAccess;
    private MavenHelper mavenHelper;
    private AccessHelper accessHelper;
    private Handler accessHandler;
    private WebHookSender webHookSender;
    private MetricRegistry metricRegistry;

    public static HadrianBuilder create(Parameters parameters) {
        return new HadrianBuilder(parameters);
    }

    private HadrianBuilder(Parameters parameters) {
        this.parameters = parameters;
    }

    public HadrianBuilder setDataAccess(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        return this;
    }

    public HadrianBuilder setMavenHelper(MavenHelper mavenHelper) {
        this.mavenHelper = mavenHelper;
        return this;
    }

    public HadrianBuilder setAccessHandler(Handler accessHandler) {
        this.accessHandler = accessHandler;
        return this;
    }

    public HadrianBuilder setWebHookSender(WebHookSender webHookSender) {
        this.webHookSender = webHookSender;
        return this;
    }

    public HadrianBuilder setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        return this;
    }

    public Hadrian builder() {
        try {
            client = new OkHttpClient();
            client.setConnectTimeout(2, TimeUnit.SECONDS);
            client.setReadTimeout(2, TimeUnit.SECONDS);
            client.setWriteTimeout(2, TimeUnit.SECONDS);
            client.setFollowSslRedirects(false);
            client.setFollowRedirects(false);
            client.setConnectionPool(new ConnectionPool(5, 60 * 1000));
        } catch (NumberFormatException nfe) {
            throw new RuntimeException("Error Creating HTTPClient, could not parse property");
        } catch (Exception e) {
            throw new RuntimeException("Error Creating HTTPClient: ", e);
        }

        if (metricRegistry == null) {
            metricRegistry = new MetricRegistry();
            if (parameters.getBoolean("metrics.console", true)) {
                ConsoleReporter reporter = ConsoleReporter.forRegistry(metricRegistry)
                        .convertRatesTo(TimeUnit.SECONDS)
                        .convertDurationsTo(TimeUnit.MILLISECONDS)
                        .build();
                reporter.start(1, TimeUnit.MINUTES);
            }
        }

        if (dataAccess == null) {
            String factoryName = parameters.getString(Const.DATA_ACCESS_FACTORY_CLASS_NAME, Const.DATA_ACCESS_FACTORY_CLASS_NAME_DEFAULT);
            Class c;
            try {
                c = Class.forName(factoryName);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException("Could not build Hadrian, could not find Data Access class " + factoryName);
            }
            DataAccessFactory factory;
            try {
                factory = (DataAccessFactory) c.newInstance();
            } catch (InstantiationException ex) {
                throw new RuntimeException("Could not build Hadrian, could not instantiation Data Access class " + factoryName);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Could not build Hadrian, could not access Data Access class " + factoryName);
            }
            dataAccess = factory.createDataAccess(parameters, metricRegistry);
        }

        if (mavenHelper == null) {
            String factoryName = parameters.getString(Const.MAVEN_HELPER_FACTORY_CLASS_NAME, Const.MAVEN_HELPER_FACTORY_CLASS_NAME_DEFAULT);
            Class c;
            try {
                c = Class.forName(factoryName);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException("Could not build Hadrian, could not find Data Access class " + factoryName);
            }
            MavenHelperFactory mavenHelperFactory;
            try {
                mavenHelperFactory = (MavenHelperFactory) c.newInstance();
            } catch (InstantiationException ex) {
                throw new RuntimeException("Could not build Hadrian, could not instantiation Data Access class " + factoryName);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Could not build Hadrian, could not access Data Access class " + factoryName);
            }
            mavenHelper = mavenHelperFactory.create(parameters, client);
        }

        accessHelper = new AccessHelper(dataAccess);

        if (accessHandler == null) {
            String factoryName = parameters.getString(Const.ACCESS_HANDLER_FACTORY_CLASS_NAME, Const.ACCESS_HANDLER_FACTORY_CLASS_NAME_DEFAULT);
            Class c;
            try {
                c = Class.forName(factoryName);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException("Could not build Hadrian, could not find Data Access class " + factoryName);
            }
            AccessHandlerFactory accessHanlderFactory;
            try {
                accessHanlderFactory = (AccessHandlerFactory) c.newInstance();
            } catch (InstantiationException ex) {
                throw new RuntimeException("Could not build Hadrian, could not instantiation Data Access class " + factoryName);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Could not build Hadrian, could not access Data Access class " + factoryName);
            }
            accessHandler = accessHanlderFactory.create(accessHelper);
        }

        if (webHookSender == null) {
            String factoryName = parameters.getString(Const.WEB_HOOK_SENDER_FACTORY_CLASS_NAME, Const.WEB_HOOK_SENDER_FACTORY_CLASS_NAME_DEFAULT);
            Class c;
            try {
                c = Class.forName(factoryName);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException("Could not build Hadrian, could not find Data Access class " + factoryName);
            }
            WebHookSenderFactory webHookSenderFactory;
            try {
                webHookSenderFactory = (WebHookSenderFactory) c.newInstance();
            } catch (InstantiationException ex) {
                throw new RuntimeException("Could not build Hadrian, could not instantiation Data Access class " + factoryName);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Could not build Hadrian, could not access Data Access class " + factoryName);
            }
            webHookSender = webHookSenderFactory.create(parameters, client);
        }

        return new Hadrian(parameters, client, dataAccess, mavenHelper, accessHelper, accessHandler, webHookSender, metricRegistry);
    }

}
