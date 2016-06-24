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

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.northernwall.hadrian.access.AccessHandlerFactory;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.calendar.CalendarHelper;
import com.northernwall.hadrian.calendar.CalendarHelperFactory;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.db.DataAccessFactory;
import com.northernwall.hadrian.db.DataAccessUpdater;
import com.northernwall.hadrian.details.HostDetailsHelper;
import com.northernwall.hadrian.details.HostDetailsHelperFactory;
import com.northernwall.hadrian.details.VipDetailsHelper;
import com.northernwall.hadrian.details.VipDetailsHelperFactory;
import com.northernwall.hadrian.module.ModuleArtifactHelper;
import com.northernwall.hadrian.module.ModuleArtifactHelperFactory;
import com.northernwall.hadrian.module.ModuleConfigHelper;
import com.northernwall.hadrian.module.ModuleConfigHelperFactory;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import com.northernwall.hadrian.workItem.WorkItemProcessorImpl;
import com.northernwall.hadrian.workItem.WorkItemSender;
import com.northernwall.hadrian.workItem.WorkItemSenderFactory;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.OkHttpClient;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.server.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HadrianBuilder {

    private final static Logger logger = LoggerFactory.getLogger(HadrianBuilder.class);

    private final Parameters parameters;
    private OkHttpClient client;
    private ConfigHelper configHelper;
    private DataAccess dataAccess;
    private ModuleArtifactHelper moduleArtifactHelper;
    private ModuleConfigHelper moduleConfigHelper;
    private AccessHelper accessHelper;
    private HostDetailsHelper hostDetailsHelper;
    private VipDetailsHelper vipDetailsHelper;
    private Handler accessHandler;
    private CalendarHelper calendarHelper;
    private WorkItemSender workItemSender;
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

    public HadrianBuilder setModuleArtifactHelper(ModuleArtifactHelper moduleArtifactHelper) {
        this.moduleArtifactHelper = moduleArtifactHelper;
        return this;
    }

    public HadrianBuilder setAccessHandler(Handler accessHandler) {
        this.accessHandler = accessHandler;
        return this;
    }

    public HadrianBuilder setWorkItemSender(WorkItemSender workItemSender) {
        this.workItemSender = workItemSender;
        return this;
    }

    public HadrianBuilder setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
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

        if (metricRegistry == null) {
            metricRegistry = new MetricRegistry();

            final OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            metricRegistry.register("jvm.processCpuLoad", new Gauge<Double>() {
                @Override
                public Double getValue() {
                    return osBean.getProcessCpuLoad();
                }
            });
            metricRegistry.register("jvm.systemCpuLoad", new Gauge<Double>() {
                @Override
                public Double getValue() {
                    return osBean.getSystemCpuLoad();
                }
            });

            if (parameters.getBoolean("metrics.console", false)) {
                ConsoleReporter reporter = ConsoleReporter.forRegistry(metricRegistry)
                        .convertRatesTo(TimeUnit.SECONDS)
                        .convertDurationsTo(TimeUnit.MILLISECONDS)
                        .build();
                reporter.start(1, TimeUnit.MINUTES);
            }

            String graphiteUrl = parameters.getString("metrics.graphite.url", null);
            int graphitePort = parameters.getInt("metrics.graphite.port", -1);
            if (graphiteUrl != null && graphitePort > -1) {
                Graphite graphite = new Graphite(new InetSocketAddress(graphiteUrl, graphitePort));
                GraphiteReporter reporter = GraphiteReporter.forRegistry(metricRegistry)
                        .prefixedWith(parameters.getString("metrics.graphite.prefix", "hadrian") + "." + getHostname())
                        .convertRatesTo(TimeUnit.SECONDS)
                        .convertDurationsTo(TimeUnit.MILLISECONDS)
                        .filter(MetricFilter.ALL)
                        .build(graphite);
                reporter.start(parameters.getInt("metrics.graphite.poll", 20), TimeUnit.SECONDS);
            }
        }

        if (dataAccess == null) {
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
            dataAccess = factory.createDataAccess(parameters, metricRegistry);
        }

        if (moduleArtifactHelper == null) {
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

        if (moduleConfigHelper == null) {
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

        configHelper = new ConfigHelper(parameters, moduleArtifactHelper, moduleConfigHelper);

        accessHelper = new AccessHelper(dataAccess);

        if (accessHandler == null) {
            String factoryName = parameters.getString(Const.ACCESS_HANDLER_FACTORY_CLASS_NAME, Const.ACCESS_HANDLER_FACTORY_CLASS_NAME_DEFAULT);
            Class c;
            try {
                c = Class.forName(factoryName);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException("Could not build Hadrian, could not find Access class " + factoryName);
            }
            AccessHandlerFactory accessHanlderFactory;
            try {
                accessHanlderFactory = (AccessHandlerFactory) c.newInstance();
            } catch (InstantiationException ex) {
                throw new RuntimeException("Could not build Hadrian, could not instantiation Access class " + factoryName);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Could not build Hadrian, could not access Access class " + factoryName);
            }
            accessHandler = accessHanlderFactory.create(accessHelper, parameters, metricRegistry);
        }

        if (hostDetailsHelper == null) {
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

        if (vipDetailsHelper == null) {
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
                vipDetailsHelper = vipDetailsHelperFactory.create(client, parameters, configHelper);
            }
        }

        if (calendarHelper == null) {
            String factoryName = parameters.getString(Const.CALENDAR_HELPER_FACTORY_CLASS_NAME, Const.CALENDAR_HELPER_FACTORY_CLASS_NAME_DEFAULT);
            Class c;
            try {
                c = Class.forName(factoryName);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException("Could not build Hadrian, could not find Calendar Helper class " + factoryName);
            }
            CalendarHelperFactory calendarHelperFactory;
            try {
                calendarHelperFactory = (CalendarHelperFactory) c.newInstance();
            } catch (InstantiationException ex) {
                throw new RuntimeException("Could not build Hadrian, could not instantiation Calendar Helper class " + factoryName);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Could not build Hadrian, could not access Calendar Helper class " + factoryName);
            }
            calendarHelper = calendarHelperFactory.create(parameters, client);
        }

        if (workItemSender == null) {
            String factoryName = parameters.getString(Const.WORK_ITEM_SENDER_FACTORY_CLASS_NAME, Const.WORK_ITEM_SENDER_FACTORY_CLASS_NAME_DEFAULT);
            Class c;
            try {
                c = Class.forName(factoryName);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException("Could not build Hadrian, could not find WorkItemSender class " + factoryName);
            }
            WorkItemSenderFactory workItemSenderFactory;
            try {
                workItemSenderFactory = (WorkItemSenderFactory) c.newInstance();
            } catch (InstantiationException ex) {
                throw new RuntimeException("Could not build Hadrian, could not instantiation WorkItemSender class " + factoryName);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Could not build Hadrian, could not access WorkItemSender class " + factoryName);
            }
            workItemSender = workItemSenderFactory.create(parameters, dataAccess, client, metricRegistry);
        }

        WorkItemProcessor workItemProcessor = new WorkItemProcessorImpl(dataAccess, workItemSender, metricRegistry);
        workItemSender.setWorkItemProcessor(workItemProcessor);

        DataAccessUpdater.update(dataAccess);

        return new Hadrian(parameters, client, configHelper, dataAccess, moduleArtifactHelper, moduleConfigHelper, accessHelper, accessHandler, hostDetailsHelper, vipDetailsHelper, calendarHelper, workItemProcessor, workItemSender, metricRegistry);
    }

    private String getHostname() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            int i = hostname.indexOf(".");
            if (i > 0) {
                hostname = hostname.substring(0, i);
            }
            logger.info("Hostname is {}", hostname);
            return hostname;
        } catch (Exception e) {
            throw new RuntimeException("Failed to find hostname", e);
        }
    }

}
