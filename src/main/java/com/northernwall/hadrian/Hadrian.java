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
import com.northernwall.hadrian.graph.GraphAllHandler;
import com.northernwall.hadrian.graph.GraphFanInHandler;
import com.northernwall.hadrian.graph.GraphFanOutHandler;
import com.northernwall.hadrian.module.ModuleArtifactHelper;
import com.northernwall.hadrian.messaging.MessageSendHandler;
import com.northernwall.hadrian.messaging.MessagingCoodinator;
import com.northernwall.hadrian.module.ModuleConfigHelper;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.service.AuditCreateHandler;
import com.northernwall.hadrian.service.AuditGetHandler;
import com.northernwall.hadrian.service.AuditOutputGetHandler;
import com.northernwall.hadrian.service.CalendarGetHandler;
import com.northernwall.hadrian.service.ConfigGetHandler;
import com.northernwall.hadrian.service.CustomFuntionCreateHandler;
import com.northernwall.hadrian.service.CustomFuntionExecHandler;
import com.northernwall.hadrian.service.CustomFuntionDeleteHandler;
import com.northernwall.hadrian.service.CustomFuntionModifyHandler;
import com.northernwall.hadrian.service.DataStoreGetHandler;
import com.northernwall.hadrian.service.HostBackfillHandler;
import com.northernwall.hadrian.service.HostCreateHandler;
import com.northernwall.hadrian.service.HostDeleteHandler;
import com.northernwall.hadrian.service.HostDeploySoftwareHandler;
import com.northernwall.hadrian.service.HostGetDetailsHandler;
import com.northernwall.hadrian.service.HostVipDeleteHandler;
import com.northernwall.hadrian.service.HostRestartHandler;
import com.northernwall.hadrian.service.HostVipCreateHandler;
import com.northernwall.hadrian.service.ModuleCreateHandler;
import com.northernwall.hadrian.service.ModuleDeleteHandler;
import com.northernwall.hadrian.service.ModuleModifyHandler;
import com.northernwall.hadrian.service.ModuleFileCreateHandler;
import com.northernwall.hadrian.service.ModuleFileGetHandler;
import com.northernwall.hadrian.service.ServiceCreateHandler;
import com.northernwall.hadrian.service.ServiceDeleteHandler;
import com.northernwall.hadrian.service.ServiceGetHandler;
import com.northernwall.hadrian.service.ServiceNotUsesGetHandler;
import com.northernwall.hadrian.service.ServiceModifyHandler;
import com.northernwall.hadrian.service.ServiceRefCreateHandler;
import com.northernwall.hadrian.service.ServiceRefDeleteHandler;
import com.northernwall.hadrian.service.ServiceRefreshHandler;
import com.northernwall.hadrian.service.ServicesGetHandler;
import com.northernwall.hadrian.service.TeamAddUserHandler;
import com.northernwall.hadrian.service.TeamCreateHandler;
import com.northernwall.hadrian.service.TeamGetHandler;
import com.northernwall.hadrian.service.TeamModifyHandler;
import com.northernwall.hadrian.service.TeamRemoveUserHandler;
import com.northernwall.hadrian.service.UserGetHandler;
import com.northernwall.hadrian.service.UserModifyHandler;
import com.northernwall.hadrian.service.VipCreateHandler;
import com.northernwall.hadrian.service.VipDeleteHandler;
import com.northernwall.hadrian.service.VipModifyHandler;
import com.northernwall.hadrian.service.WorkItemGetHandler;
import com.northernwall.hadrian.service.helper.HostDetailsHelper;
import com.northernwall.hadrian.service.helper.InfoHelper;
import com.northernwall.hadrian.tree.TreeHandler;
import com.northernwall.hadrian.utilityHandlers.AvailabilityHandler;
import com.northernwall.hadrian.utilityHandlers.ContentHandler;
import com.northernwall.hadrian.utilityHandlers.FaviconHandler;
import com.northernwall.hadrian.utilityHandlers.HealthHandler;
import com.northernwall.hadrian.utilityHandlers.MetricHandler;
import com.northernwall.hadrian.utilityHandlers.RedirectHandler;
import com.northernwall.hadrian.utilityHandlers.VersionHandler;
import com.northernwall.hadrian.utilityHandlers.routingHandler.MethodRule;
import com.northernwall.hadrian.utilityHandlers.routingHandler.TargetRule;
import com.northernwall.hadrian.utilityHandlers.routingHandler.RoutingHandler;
import com.northernwall.hadrian.workItem.WorkItemCallbackHandler;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import com.northernwall.hadrian.workItem.WorkItemSender;
import com.squareup.okhttp.OkHttpClient;
import java.net.BindException;
import org.slf4j.LoggerFactory;
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
    private final ModuleArtifactHelper moduleArtifactHelper;
    private final ModuleConfigHelper moduleConfigHelper;
    private final AccessHelper accessHelper;
    private final Handler accessHandler;
    private final CalendarHelper calendarHelper;
    private final WorkItemProcessor workItemProcess;
    private final WorkItemSender workItemSender;
    private final InfoHelper infoHelper;
    private final HostDetailsHelper hostDetailsHelper;
    private final MessagingCoodinator messagingCoodinator;
    private int port;
    private Server server;

    Hadrian(Parameters parameters, OkHttpClient client, DataAccess dataAccess, ModuleArtifactHelper moduleArtifactHelper, ModuleConfigHelper moduleConfigHelper, AccessHelper accessHelper, Handler accessHandler, CalendarHelper calendarHelper, WorkItemProcessor workItemProcess, WorkItemSender workItemSender, MetricRegistry metricRegistry) {
        this.parameters = parameters;
        this.client = client;
        this.dataAccess = dataAccess;
        this.moduleArtifactHelper = moduleArtifactHelper;
        this.moduleConfigHelper = moduleConfigHelper;
        this.accessHelper = accessHelper;
        this.accessHandler = accessHandler;
        this.calendarHelper = calendarHelper;
        this.workItemProcess = workItemProcess;
        this.workItemSender = workItemSender;
        this.metricRegistry = metricRegistry;

        configHelper = new ConfigHelper(parameters, moduleArtifactHelper, moduleConfigHelper);
        infoHelper = new InfoHelper(client);
        hostDetailsHelper = new HostDetailsHelper(client, parameters);
        messagingCoodinator = new MessagingCoodinator(parameters, client);

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
        
        RoutingHandler routingHandler = new RoutingHandler();
        //These urls do not require a login
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/availability", new AvailabilityHandler(dataAccess), false);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/version", new VersionHandler(), false);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/health", new HealthHandler(accessHandler, calendarHelper, dataAccess, moduleArtifactHelper, moduleConfigHelper, parameters, workItemSender, messagingCoodinator), true);
        routingHandler.add(MethodRule.GET, TargetRule.STARTS_WITH, "/ui/", new ContentHandler("/webcontent"), false);
        routingHandler.add(MethodRule.POST, TargetRule.STARTS_WITH, "/webhook/callback", new WorkItemCallbackHandler(workItemProcess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/favicon.ico", new FaviconHandler(), false);
        //Accees Handler
        routingHandler.add(MethodRule.ANY, TargetRule.ANY, "/", accessHandler, false);
        //These urls require a login
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/config", new ConfigGetHandler(configHelper), true);
        routingHandler.add(MethodRule.GET, TargetRule.STARTS_WITH, "/ui/", new ContentHandler("/webapp"), false);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/tree", new TreeHandler(dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/users", new UserGetHandler(dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/user/modify", new UserModifyHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/team", new TeamGetHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/team/create", new TeamCreateHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/team/modify", new TeamModifyHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/team/addUser", new TeamAddUserHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/team/removeUser", new TeamRemoveUserHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/services", new ServicesGetHandler(dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/service", new ServiceGetHandler(accessHelper, dataAccess, configHelper, moduleArtifactHelper, moduleConfigHelper, infoHelper), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/service/refresh", new ServiceRefreshHandler(accessHelper, dataAccess, configHelper, moduleArtifactHelper, moduleConfigHelper, infoHelper), false);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/service/notuses", new ServiceNotUsesGetHandler(dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/service/audit", new AuditGetHandler(dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/service/auditOutput", new AuditOutputGetHandler(dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/service/create", new ServiceCreateHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/service/modify", new ServiceModifyHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/service/delete", new ServiceDeleteHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/service/createRef", new ServiceRefCreateHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/service/deleteRef", new ServiceRefDeleteHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/vip/create", new VipCreateHandler(accessHelper, dataAccess, workItemProcess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/vip/modify", new VipModifyHandler(accessHelper, dataAccess, workItemProcess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/vip/delete", new VipDeleteHandler(accessHelper, dataAccess, workItemProcess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/module/file", new ModuleFileGetHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/module/create", new ModuleCreateHandler(accessHelper, configHelper, dataAccess, workItemProcess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/module/modify", new ModuleModifyHandler(accessHelper, dataAccess, workItemProcess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/module/file", new ModuleFileCreateHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/module/delete", new ModuleDeleteHandler(accessHelper, dataAccess, workItemProcess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/host/details", new HostGetDetailsHandler(dataAccess, hostDetailsHelper), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/host/create", new HostCreateHandler(accessHelper, configHelper, dataAccess, workItemProcess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/host/deploy", new HostDeploySoftwareHandler(accessHelper, configHelper, dataAccess, workItemProcess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/host/restart", new HostRestartHandler(accessHelper, dataAccess, workItemProcess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/host/delete", new HostDeleteHandler(accessHelper, dataAccess, workItemProcess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/host/backfill", new HostBackfillHandler(accessHelper, configHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/hostvip/create", new HostVipCreateHandler(accessHelper, dataAccess, workItemProcess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/hostvip/delete", new HostVipDeleteHandler(accessHelper, dataAccess, workItemProcess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/cf/exec", new CustomFuntionExecHandler(accessHelper, dataAccess, client), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/cf/create", new CustomFuntionCreateHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/cf/modify", new CustomFuntionModifyHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/cf/delete", new CustomFuntionDeleteHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/workitems", new WorkItemGetHandler(dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/datastore", new DataStoreGetHandler(), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/calendar", new CalendarGetHandler(dataAccess, calendarHelper), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/graph/all", new GraphAllHandler(dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.MATCHES, "/v1/graph/fanin/\\w+-\\w+-\\w+-\\w+-\\w+", new GraphFanInHandler(dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.MATCHES, "/v1/graph/fanout/\\w+-\\w+-\\w+-\\w+-\\w+", new GraphFanOutHandler(dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/audit", new AuditCreateHandler(dataAccess, accessHelper), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/sendMessage", new MessageSendHandler(dataAccess, accessHelper, messagingCoodinator), true);
        //Catch all handler
        routingHandler.add(MethodRule.ANY, TargetRule.ANY, "/", new RedirectHandler(), true);
        handlers.addHandler(routingHandler);

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
