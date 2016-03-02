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

import com.codahale.metrics.MetricRegistry;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.calendar.CalendarHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.graph.GraphHandler;
import com.northernwall.hadrian.maven.MavenHelper;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.service.AuditHandler;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import com.northernwall.hadrian.service.CalendarHandler;
import com.northernwall.hadrian.service.ConfigHandler;
import com.northernwall.hadrian.service.CustomFuntionHandler;
import com.northernwall.hadrian.service.DataStoreHandler;
import com.northernwall.hadrian.service.HostHandler;
import com.northernwall.hadrian.service.ModuleHandler;
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
import com.northernwall.hadrian.utilityHandlers.MetricHandler;
import com.northernwall.hadrian.utilityHandlers.RedirectHandler;
import com.northernwall.hadrian.workItem.WorkItemCallbackHandler;
import com.squareup.okhttp.OkHttpClient;
import org.slf4j.LoggerFactory;
import java.net.BindException;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;

public class Hadrian {

    private final static Logger logger = LoggerFactory.getLogger(Hadrian.class);

    private final Parameters parameters;
    private final ConfigHelper configHelper;
    private final DataAccess dataAccess;
    private final MetricRegistry metricRegistry;
    private final OkHttpClient client;
    private final MavenHelper mavenHelper;
    private final AccessHelper accessHelper;
    private final Handler accessHandler;
    private final CalendarHelper calendarHelper;
    private final WorkItemProcessor workItemProcess;
    private final InfoHelper infoHelper;
    private final HostDetailsHelper hostDetailsHelper;
    private int port;
    private Server server;

    Hadrian(Parameters parameters, OkHttpClient client, DataAccess dataAccess, MavenHelper mavenHelper, AccessHelper accessHelper, Handler accessHandler, CalendarHelper calendarHelper, WorkItemProcessor workItemProcess, MetricRegistry metricRegistry) {
        this.parameters = parameters;
        this.client = client;
        this.dataAccess = dataAccess;
        this.mavenHelper = mavenHelper;
        this.accessHelper = accessHelper;
        this.accessHandler = accessHandler;
        this.calendarHelper = calendarHelper;
        this.workItemProcess = workItemProcess;
        this.metricRegistry = metricRegistry;

        configHelper = new ConfigHelper(parameters);
        infoHelper = new InfoHelper(client);
        hostDetailsHelper = new HostDetailsHelper(client, parameters);

        setupJetty();
    }

    private void setupJetty() {
        port = parameters.getInt(Const.JETTY_PORT, Const.JETTY_PORT_DEFAULT);

        server = new Server(new QueuedThreadPool(10, 5));
        server.setStopAtShutdown(true);

        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSendServerVersion(false);
        HttpConnectionFactory httpFactory = new HttpConnectionFactory(httpConfig);
        ServerConnector connector = new ServerConnector(server, httpFactory);
        connector.setPort(port);
        connector.setIdleTimeout(parameters.getInt(Const.JETTY_IDLE_TIMEOUT, Const.JETTY_IDLE_TIMEOUT_DEFAULT));
        connector.setAcceptQueueSize(parameters.getInt(Const.JETTY_ACCEPT_QUEUE_SIZE, Const.JETTY_ACCEPT_QUEUE_SIZE_DEFAULT));
        server.addConnector(connector);

        HandlerList handlers = new HandlerList();
        handlers.addHandler(new AvailabilityHandler(accessHandler, calendarHelper, dataAccess, mavenHelper, parameters, workItemProcess.getWorkItemSender()));
        handlers.addHandler(new ContentHandler("/webcontent"));
        handlers.addHandler(new WorkItemCallbackHandler(workItemProcess));
        handlers.addHandler(new AuditHandler(dataAccess));
        handlers.addHandler(accessHandler);
        handlers.addHandler(new ContentHandler("/webapp"));
        handlers.addHandler(new TreeHandler(dataAccess));
        handlers.addHandler(new UserHandler(accessHelper, dataAccess));
        handlers.addHandler(new TeamHandler(accessHelper, dataAccess));
        handlers.addHandler(new ServiceHandler(accessHelper, dataAccess, configHelper, mavenHelper, infoHelper));
        handlers.addHandler(new VipHandler(accessHelper, dataAccess, workItemProcess));
        handlers.addHandler(new ModuleHandler(accessHelper, configHelper, dataAccess, workItemProcess));
        handlers.addHandler(new HostHandler(accessHelper, configHelper, dataAccess, workItemProcess, hostDetailsHelper));
        handlers.addHandler(new CustomFuntionHandler(accessHelper, dataAccess, client));
        handlers.addHandler(new WorkItemHandler(dataAccess));
        handlers.addHandler(new DataStoreHandler(accessHelper, dataAccess));
        handlers.addHandler(new ConfigHandler(configHelper));
        handlers.addHandler(new CalendarHandler(dataAccess, calendarHelper));
        handlers.addHandler(new GraphHandler(dataAccess));
        handlers.addHandler(new RedirectHandler());

        MetricHandler metricHandler = new MetricHandler(handlers, metricRegistry);

        ContextHandler contextHandler = new ContextHandler();
        contextHandler.setHandler(metricHandler);
        contextHandler.setContextPath("/");

        server.setHandler(contextHandler);
    }

    public void start() {
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
