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
package com.northernwall.hadrian;

import com.northernwall.hadrian.config.ConfigHelper;
import com.northernwall.hadrian.config.Const;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.northernwall.hadrian.access.AccessHandlerFactory;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.access.AccessHelperFactory;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.db.DataAccessFactory;
import com.northernwall.hadrian.db.DataAccessUpdater;
import com.northernwall.hadrian.details.HostDetailsHelper;
import com.northernwall.hadrian.details.HostDetailsHelperFactory;
import com.northernwall.hadrian.details.VipDetailsHelper;
import com.northernwall.hadrian.details.VipDetailsHelperFactory;
import com.northernwall.hadrian.handlers.service.helper.FolderHelper;
import com.northernwall.hadrian.messaging.MessagingCoodinator;
import com.northernwall.hadrian.module.ModuleArtifactHelper;
import com.northernwall.hadrian.module.ModuleArtifactHelperFactory;
import com.northernwall.hadrian.module.ModuleConfigHelper;
import com.northernwall.hadrian.module.ModuleConfigHelperFactory;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.schedule.Leader;
import com.northernwall.hadrian.schedule.Scheduler;
import com.northernwall.hadrian.sshAccess.SshAccess;
import com.northernwall.hadrian.sshAccess.SshAccessFactory;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import com.northernwall.hadrian.workItem.helper.SmokeTestHelper;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.OkHttpClient;
import java.net.InetAddress;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.dshops.metrics.EventListener;
import org.dshops.metrics.JvmMetrics;
import org.dshops.metrics.MetricRegistry;
import org.dshops.metrics.listeners.ConsoleListener;
import org.dshops.metrics.listeners.KairosDBListenerFactory;
import org.eclipse.jetty.server.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HadrianBuilder {

    private final static Logger LOGGER = LoggerFactory.getLogger(HadrianBuilder.class);

    public static HadrianBuilder create(Parameters parameters) {
        return new HadrianBuilder(parameters);
    }

    private final Parameters parameters;
    private final Gson gson;
    private OkHttpClient client;
    private FolderHelper folderHelper;
    private ConfigHelper configHelper;
    private DataAccess dataAccess;
    private SshAccess sshAccess;
    private ModuleArtifactHelper moduleArtifactHelper;
    private ModuleConfigHelper moduleConfigHelper;
    private AccessHelper accessHelper;
    private HostDetailsHelper hostDetailsHelper;
    private VipDetailsHelper vipDetailsHelper;
    private Handler accessHandler;
    private MessagingCoodinator messagingCoodinator;
    private SmokeTestHelper smokeTestHelper;
    private Scheduler scheduler;
    private MetricRegistry metricRegistry;

    private HadrianBuilder(Parameters parameters) {
        this.parameters = parameters;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new GsonUTCDateAdapter())
                .create();
    }

    public HadrianBuilder setDataAccess(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        return this;
    }

    public HadrianBuilder setModuleArtifactHelper(ModuleArtifactHelper moduleArtifactHelper) {
        this.moduleArtifactHelper = moduleArtifactHelper;
        return this;
    }

    public HadrianBuilder setAccessHandler(Handler accessHandler) {
        this.accessHandler = accessHandler;
        return this;
    }

    public Hadrian builder() {
        client = new OkHttpClient();
        client.setConnectTimeout(2, TimeUnit.SECONDS);
        client.setReadTimeout(15, TimeUnit.SECONDS);
        client.setWriteTimeout(2, TimeUnit.SECONDS);
        client.setFollowSslRedirects(false);
        client.setFollowRedirects(false);
        client.setConnectionPool(new ConnectionPool(5, 60 * 1000));

        buildMetrics();

        if (dataAccess == null) {
            buildDataAccess();
        }
        
        buildSshAccess();

        if (moduleArtifactHelper == null) {
            buildModuleArtifactHelper();
        }

        if (moduleConfigHelper == null) {
            buildModuleConfigHelper();
        }

        configHelper = new ConfigHelper(parameters, gson, moduleConfigHelper);

        folderHelper = new FolderHelper(configHelper);

        if (accessHelper == null) {
            buildAccessHelper();
        }

        if (accessHandler == null) {
            buildAccessHandler();
        }

        if (hostDetailsHelper == null) {
            buildHostDetailsHelper();
        }

        if (vipDetailsHelper == null) {
            buildVipDetailsHelper();
        }

        messagingCoodinator = new MessagingCoodinator(
                dataAccess,
                parameters,
                client, 
                gson);

        smokeTestHelper = new SmokeTestHelper(
                parameters,
                gson,
                metricRegistry);

        buildScheduler();

        WorkItemProcessor workItemProcessor = new WorkItemProcessor(
                parameters,
                configHelper,
                dataAccess,
                messagingCoodinator,
                client,
                gson,
                smokeTestHelper,
                metricRegistry);

        DataAccessUpdater.update(
                dataAccess,
                configHelper.getConfig());

        return new Hadrian(
                parameters,
                client,
                configHelper,
                dataAccess,
                sshAccess,
                moduleArtifactHelper,
                moduleConfigHelper,
                accessHelper,
                accessHandler,
                hostDetailsHelper,
                vipDetailsHelper,
                workItemProcessor,
                scheduler,
                folderHelper,
                smokeTestHelper,
                metricRegistry,
                messagingCoodinator,
                gson);
    }

    private void buildMetrics() {
        String serviceTeam = parameters.getString("metrics.serviceTeam", "Hadrian");
        String application = parameters.getString("metrics.application", "Hadrian");
        String dataCenter = parameters.getString("metrics.dataCenter", "DC");

        metricRegistry = new MetricRegistry.Builder(
                serviceTeam,
                application,
                "server",
                getHostname(),
                dataCenter)
                .build();

        JvmMetrics.addMetrics(metricRegistry, 10);

        if (parameters.getBoolean("metrics.console", false)) {
            LOGGER.info("Configuring Metrics Console Listener");
            EventListener consoleListener = new ConsoleListener(System.out);
            metricRegistry.addEventListener(consoleListener);
        }

        String kairosDbUrl = parameters.getString("metrics.kairosDb.url", null);
        if (kairosDbUrl != null && !kairosDbUrl.isEmpty()) {
            LOGGER.info("Configuring Metrics KairosDB Listener,  {}", kairosDbUrl);
            EventListener kairosListener = KairosDBListenerFactory.buildListener(
                    kairosDbUrl, 
                    metricRegistry);
            /*
            EventListener kairosListener = new KairosDBListener(
                    kairosDbUrl,
                    parameters.getString("metrics.kairosDb.username", null),
                    parameters.getString("metrics.kairosDb.password", null),
                    metricRegistry);
             */
            metricRegistry.addEventListener(kairosListener);
        }
    }

    private void buildDataAccess() {
        String factoryName = parameters.getString(Const.DATA_ACCESS_FACTORY_CLASS_NAME, Const.DATA_ACCESS_FACTORY_CLASS_NAME_DEFAULT);
        Class c;
        try {
            c = Class.forName(factoryName);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Could not build Hadrian, could not find DataAccess class " + factoryName);
        }
        DataAccessFactory factory;
        try {
            factory = (DataAccessFactory) c.newInstance();
        } catch (InstantiationException ex) {
            throw new RuntimeException("Could not build Hadrian, could not instantiation DataAccess class " + factoryName);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Could not build Hadrian, could not access DataAccess class " + factoryName);
        }
        dataAccess = factory.createDataAccess(parameters, gson, metricRegistry);
    }

    private void buildSshAccess() {
        String factoryName = parameters.getString(Const.SSH_ACCESS_FACTORY_CLASS_NAME, Const.SSH_ACCESS_FACTORY_CLASS_NAME_DEFAULT);
        Class c;
        try {
            c = Class.forName(factoryName);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Could not build Hadrian, could not find SshAccess class " + factoryName);
        }
        SshAccessFactory factory;
        try {
            factory = (SshAccessFactory) c.newInstance();
        } catch (InstantiationException ex) {
            throw new RuntimeException("Could not build Hadrian, could not instantiation SshAccess class " + factoryName);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Could not build Hadrian, could not access SshAccess class " + factoryName);
        }
        sshAccess = factory.create(parameters, gson);
    }

    private void buildModuleArtifactHelper() {
        String factoryName = parameters.getString(Const.MODULE_ARTIFACT_HELPER_FACTORY_CLASS_NAME, Const.MODULE_ARTIFACT_HELPER_FACTORY_CLASS_NAME_DEFAULT);
        if (factoryName != null && !factoryName.isEmpty()) {
            Class c;
            try {
                c = Class.forName(factoryName);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException("Could not build Hadrian, could not find ModuleArtifactHelper class " + factoryName);
            }
            ModuleArtifactHelperFactory moduleArtifactHelperFactory;
            try {
                moduleArtifactHelperFactory = (ModuleArtifactHelperFactory) c.newInstance();
            } catch (InstantiationException ex) {
                throw new RuntimeException("Could not build Hadrian, could not instantiation ModuleArtifactHelper class " + factoryName);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Could not build Hadrian, could not access ModuleArtifactHelper class " + factoryName);
            }
            moduleArtifactHelper = moduleArtifactHelperFactory.create(parameters, client);
        }
    }

    private void buildModuleConfigHelper() {
        String factoryName = parameters.getString(Const.MODULE_CONFIG_HELPER_FACTORY_CLASS_NAME, null);
        if (factoryName != null && !factoryName.isEmpty()) {
            Class c;
            try {
                c = Class.forName(factoryName);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException("Could not build Hadrian, could not find ModuleConfigHelper class " + factoryName);
            }
            ModuleConfigHelperFactory moduleConfigHelperFactory;
            try {
                moduleConfigHelperFactory = (ModuleConfigHelperFactory) c.newInstance();
            } catch (InstantiationException ex) {
                throw new RuntimeException("Could not build Hadrian, could not instantiation ModuleConfigHelper class " + factoryName);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Could not build Hadrian, could not access ModuleConfigHelper class " + factoryName);
            }
            moduleConfigHelper = moduleConfigHelperFactory.create(parameters, client);
        }
    }

    private void buildAccessHandler() {
        String factoryName = parameters.getString(Const.ACCESS_HANDLER_FACTORY_CLASS_NAME, Const.ACCESS_HANDLER_FACTORY_CLASS_NAME_DEFAULT);
        Class c;
        try {
            c = Class.forName(factoryName);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Could not build Hadrian, could not find Access Handler class " + factoryName);
        }
        AccessHandlerFactory accessHanlderFactory;
        try {
            accessHanlderFactory = (AccessHandlerFactory) c.newInstance();
        } catch (InstantiationException ex) {
            throw new RuntimeException("Could not build Hadrian, could not instantiation Access Handler class " + factoryName);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Could not build Hadrian, could not access Access Handler class " + factoryName);
        }
        accessHandler = accessHanlderFactory.create(accessHelper, parameters, metricRegistry);
    }

    private void buildAccessHelper() {
        String factoryName = parameters.getString(Const.ACCESS_HELPER_FACTORY_CLASS_NAME, Const.ACCESS_HELPER_FACTORY_CLASS_NAME_DEFAULT);
        Class c;
        try {
            c = Class.forName(factoryName);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Could not build Hadrian, could not find Access Helper class " + factoryName);
        }
        AccessHelperFactory accessHelperFactory;
        try {
            accessHelperFactory = (AccessHelperFactory) c.newInstance();
        } catch (InstantiationException ex) {
            throw new RuntimeException("Could not build Hadrian, could not instantiation Access Helper class " + factoryName);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Could not build Hadrian, could not access Access Helper class " + factoryName);
        }
        accessHelper = accessHelperFactory.create(parameters);
    }

    private void buildHostDetailsHelper() {
        String factoryName = parameters.getString(Const.HOST_DETAILS_HELPER_FACTORY_CLASS_NAME, Const.HOST_DETAILS_HELPER_FACTORY_CLASS_NAME_DEFAULT);
        Class c;
        try {
            c = Class.forName(factoryName);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Could not build Hadrian, could not find Host Details Helper class " + factoryName);
        }
        HostDetailsHelperFactory hostDetailsHelperFactory;
        try {
            hostDetailsHelperFactory = (HostDetailsHelperFactory) c.newInstance();
        } catch (InstantiationException ex) {
            throw new RuntimeException("Could not build Hadrian, could not instantiation Host Details Helper class " + factoryName);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Could not build Hadrian, could not access Host Details Helper class " + factoryName);
        }
        hostDetailsHelper = hostDetailsHelperFactory.create(client, parameters);
    }

    private void buildVipDetailsHelper() {
        String factoryName = parameters.getString(Const.VIP_DETAILS_HELPER_FACTORY_CLASS_NAME, Const.VIP_DETAILS_HELPER_FACTORY_CLASS_NAME_DEFAULT);
        if (factoryName != null && !factoryName.isEmpty()) {
            Class c;
            try {
                c = Class.forName(factoryName);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException("Could not build Hadrian, could not find VIP Details Helper class " + factoryName);
            }
            VipDetailsHelperFactory vipDetailsHelperFactory;
            try {
                vipDetailsHelperFactory = (VipDetailsHelperFactory) c.newInstance();
            } catch (InstantiationException ex) {
                throw new RuntimeException("Could not build Hadrian, could not instantiation VIP Details Helper class " + factoryName);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Could not build Hadrian, could not access VIP Details Helper class " + factoryName);
            }
            vipDetailsHelper = vipDetailsHelperFactory.create(client, parameters, configHelper, gson);
        }
    }

    private void buildScheduler() {
        String factoryName = parameters.getString(Const.LEADER_CLASS_NAME, Const.LEADER_CLASS_NAME_DEFAULT);
        Class c;
        try {
            c = Class.forName(factoryName);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Could not build Hadrian, could not find Leader class " + factoryName);
        }
        try {
            Leader leader = (Leader) c.newInstance();
            leader.init(getHostname(), parameters, client);
            scheduler = new Scheduler(
                    dataAccess,
                    metricRegistry,
                    leader,
                    smokeTestHelper,
                    messagingCoodinator);
        } catch (InstantiationException ex) {
            throw new RuntimeException("Could not build Hadrian, could not instantiation Leader class " + factoryName);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Could not build Hadrian, could not access Leader class " + factoryName);
        }
    }

    private String getHostname() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            int i = hostname.indexOf(".");
            if (i > 0) {
                hostname = hostname.substring(0, i);
            }
            LOGGER.info("Hostname is {}", hostname);
            return hostname;
        } catch (Exception e) {
            throw new RuntimeException("Failed to find hostname", e);
        }
    }

}
