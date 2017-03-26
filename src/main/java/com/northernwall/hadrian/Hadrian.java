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
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.details.HostDetailsHelper;
import com.northernwall.hadrian.details.ServiceBuildHelper;
import com.northernwall.hadrian.details.VipDetailsHelper;
import com.northernwall.hadrian.handlers.dashboard.DashboardHandler;
import com.northernwall.hadrian.handlers.caching.ContentHandler;
import com.northernwall.hadrian.handlers.dashboard.DashboardAllHandler;
import com.northernwall.hadrian.handlers.graph.GraphAllHandler;
import com.northernwall.hadrian.handlers.graph.GraphFanInHandler;
import com.northernwall.hadrian.handlers.graph.GraphFanOutHandler;
import com.northernwall.hadrian.handlers.service.AuditCreateHandler;
import com.northernwall.hadrian.handlers.service.AuditGetHandler;
import com.northernwall.hadrian.handlers.service.AuditOutputGetHandler;
import com.northernwall.hadrian.handlers.service.ConfigGetHandler;
import com.northernwall.hadrian.handlers.service.CustomFuntionCreateHandler;
import com.northernwall.hadrian.handlers.service.CustomFuntionExecHandler;
import com.northernwall.hadrian.handlers.service.CustomFuntionDeleteHandler;
import com.northernwall.hadrian.handlers.service.CustomFuntionModifyHandler;
import com.northernwall.hadrian.handlers.service.DataStoreGetHandler;
import com.northernwall.hadrian.handlers.service.DocumentCreateHandler;
import com.northernwall.hadrian.handlers.service.DocumentDeleteHandler;
import com.northernwall.hadrian.handlers.service.DocumentGetHandler;
import com.northernwall.hadrian.handlers.host.HostBackfillHandler;
import com.northernwall.hadrian.handlers.host.HostCreateHandler;
import com.northernwall.hadrian.handlers.host.HostDeleteHandler;
import com.northernwall.hadrian.handlers.host.HostDeploySoftwareHandler;
import com.northernwall.hadrian.handlers.host.HostFindHandler;
import com.northernwall.hadrian.handlers.host.HostGetDetailsHandler;
import com.northernwall.hadrian.handlers.host.HostRebootHandler;
import com.northernwall.hadrian.handlers.host.HostRestartHandler;
import com.northernwall.hadrian.handlers.host.HostsGetHandler;
import com.northernwall.hadrian.handlers.module.ModuleCreateHandler;
import com.northernwall.hadrian.handlers.module.ModuleDeleteHandler;
import com.northernwall.hadrian.handlers.module.ModuleFileDeleteHandler;
import com.northernwall.hadrian.handlers.module.ModuleModifyHandler;
import com.northernwall.hadrian.handlers.module.ModuleFileCreateHandler;
import com.northernwall.hadrian.handlers.module.ModuleFileGetHandler;
import com.northernwall.hadrian.handlers.service.ServiceBuildHandler;
import com.northernwall.hadrian.handlers.service.ServiceCreateHandler;
import com.northernwall.hadrian.handlers.service.ServiceDeleteHandler;
import com.northernwall.hadrian.handlers.service.ServiceGetHandler;
import com.northernwall.hadrian.handlers.service.ServiceNotUsesGetHandler;
import com.northernwall.hadrian.handlers.service.ServiceModifyHandler;
import com.northernwall.hadrian.handlers.service.ServiceRefCreateHandler;
import com.northernwall.hadrian.handlers.service.ServiceRefDeleteHandler;
import com.northernwall.hadrian.handlers.service.ServiceRefreshHandler;
import com.northernwall.hadrian.handlers.service.ServiceResetAllHandler;
import com.northernwall.hadrian.handlers.service.ServicesGetHandler;
import com.northernwall.hadrian.handlers.service.SmokeTestExecHandler;
import com.northernwall.hadrian.handlers.service.VersionsGetHandler;
import com.northernwall.hadrian.handlers.vip.EndpointGetHandler;
import com.northernwall.hadrian.handlers.vip.VipCreateHandler;
import com.northernwall.hadrian.handlers.vip.VipDeleteHandler;
import com.northernwall.hadrian.handlers.vip.VipMigrateHandler;
import com.northernwall.hadrian.handlers.vip.VipModifyHandler;
import com.northernwall.hadrian.handlers.vip.VipGetDetailsHandler;
import com.northernwall.hadrian.handlers.service.helper.FolderHelper;
import com.northernwall.hadrian.handlers.service.helper.InfoHelper;
import com.northernwall.hadrian.handlers.report.ReportHandler;
import com.northernwall.hadrian.handlers.routing.MethodRule;
import com.northernwall.hadrian.handlers.routing.TargetRule;
import com.northernwall.hadrian.handlers.routing.RoutingHandler;
import com.northernwall.hadrian.handlers.service.ServiceTransferHandler;
import com.northernwall.hadrian.handlers.team.TeamCreateHandler;
import com.northernwall.hadrian.handlers.team.TeamGetHandler;
import com.northernwall.hadrian.handlers.team.TeamModifyHandler;
import com.northernwall.hadrian.handlers.team.TeamsGetHandler;
import com.northernwall.hadrian.handlers.tree.CatalogHandler;
import com.northernwall.hadrian.handlers.tree.TreeHandler;
import com.northernwall.hadrian.handlers.utility.AvailabilityHandler;
import com.northernwall.hadrian.handlers.utility.ConvertHandler;
import com.northernwall.hadrian.handlers.utility.FaviconHandler;
import com.northernwall.hadrian.handlers.utility.HealthHandler;
import com.northernwall.hadrian.handlers.utility.MetricHandler;
import com.northernwall.hadrian.handlers.utility.RedirectHandler;
import com.northernwall.hadrian.handlers.utility.VersionHandler;
import com.northernwall.hadrian.handlers.vip.EndpointsGetHandler;
import com.northernwall.hadrian.handlers.vip.VipBackfillHandler;
import com.northernwall.hadrian.handlers.workitem.WorkItemGetHandler;
import com.northernwall.hadrian.module.ModuleArtifactHelper;
import com.northernwall.hadrian.messaging.MessageSendHandler;
import com.northernwall.hadrian.messaging.MessagingCoodinator;
import com.northernwall.hadrian.module.ModuleConfigHelper;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.schedule.Scheduler;
import com.northernwall.hadrian.workItem.WorkItemCallbackHandler;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import com.northernwall.hadrian.workItem.helper.SmokeTestHelper;
import com.squareup.okhttp.OkHttpClient;
import java.net.BindException;
import org.dshops.metrics.MetricRegistry;
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

    private final static Logger LOGGER = LoggerFactory.getLogger(Hadrian.class);

    private final Parameters parameters;
    private final OkHttpClient client;
    private final ConfigHelper configHelper;
    private final DataAccess dataAccess;
    private final ModuleArtifactHelper moduleArtifactHelper;
    private final ModuleConfigHelper moduleConfigHelper;
    private final AccessHelper accessHelper;
    private final Handler accessHandler;
    private final HostDetailsHelper hostDetailsHelper;
    private final VipDetailsHelper vipDetailsHelper;
    private final WorkItemProcessor workItemProcessor;
    private final Scheduler scheduler;
    private final FolderHelper folderHelper;
    private final SmokeTestHelper smokeTestHelper;
    private final MetricRegistry metricRegistry;
    private final MessagingCoodinator messagingCoodinator;
    private final Gson gson;
    private final InfoHelper infoHelper;
    private int port;
    private Server server;

    Hadrian(Parameters parameters, OkHttpClient client, ConfigHelper configHelper, DataAccess dataAccess, ModuleArtifactHelper moduleArtifactHelper, ModuleConfigHelper moduleConfigHelper, AccessHelper accessHelper, Handler accessHandler, HostDetailsHelper hostDetailsHelper, VipDetailsHelper vipDetailsHelper, WorkItemProcessor workItemProcessor, Scheduler scheduler, FolderHelper folderHelper, SmokeTestHelper smokeTestHelper, MetricRegistry metricRegistry, MessagingCoodinator messagingCoodinator, Gson gson) {
        this.parameters = parameters;
        this.client = client;
        this.configHelper = configHelper;
        this.dataAccess = dataAccess;
        this.moduleArtifactHelper = moduleArtifactHelper;
        this.moduleConfigHelper = moduleConfigHelper;
        this.accessHelper = accessHelper;
        this.accessHandler = accessHandler;
        this.hostDetailsHelper = hostDetailsHelper;
        this.vipDetailsHelper = vipDetailsHelper;
        this.workItemProcessor = workItemProcessor;
        this.scheduler = scheduler;
        this.folderHelper = folderHelper;
        this.smokeTestHelper = smokeTestHelper;
        this.metricRegistry = metricRegistry;
        this.messagingCoodinator = messagingCoodinator;
        this.gson = gson;

        infoHelper = new InfoHelper(parameters, client, metricRegistry);

        setupJetty();
    }

    private void setupJetty() {
        port = parameters.getInt(Const.JETTY_PORT, Const.JETTY_PORT_DEFAULT);

        server = new Server(new QueuedThreadPool(50, 5));
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
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/health", new HealthHandler(accessHandler, dataAccess, moduleArtifactHelper, moduleConfigHelper, parameters, messagingCoodinator, scheduler), true);
        ContentHandler contentHandler = new ContentHandler("/webcontent");
        contentHandler.preload("/js/viz.js");
        contentHandler.preload("/js/angular.js");
        contentHandler.preload("/js/angular-animate.js");
        contentHandler.preload("/js/ace.js");
        contentHandler.preload("/js/ui-bootstrap-tpls-0.14.2.min.js");
        contentHandler.preload("/css/bootstrap.min.css");
        routingHandler.add(MethodRule.GET, TargetRule.STARTS_WITH, "/ui/", contentHandler, false);
        routingHandler.add(MethodRule.POST, TargetRule.STARTS_WITH, "/webhook/callback", new WorkItemCallbackHandler(gson, workItemProcessor), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/favicon.ico", new FaviconHandler(), false);
        //Accees Handler
        routingHandler.add(MethodRule.ANY, TargetRule.ANY, "/", accessHandler, false);
        //These urls require a login
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/config", new ConfigGetHandler(configHelper), true);
        routingHandler.add(MethodRule.GET, TargetRule.STARTS_WITH, "/ui/", new ContentHandler("/webapp"), false);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/tree", new TreeHandler(dataAccess, accessHelper, parameters), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/catalog", new CatalogHandler(dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/report", new ReportHandler(accessHelper, dataAccess, configHelper), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/team", new TeamGetHandler(dataAccess, gson, accessHelper), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/teams", new TeamsGetHandler(dataAccess, gson, accessHelper), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/team/create", new TeamCreateHandler(dataAccess, gson, accessHelper), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/team/modify", new TeamModifyHandler(dataAccess, gson, accessHelper), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/dashboard", new DashboardHandler(dataAccess, gson, infoHelper), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/dashboardAll", new DashboardAllHandler(dataAccess, gson, infoHelper), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/services", new ServicesGetHandler(dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/service", new ServiceGetHandler(dataAccess, gson, accessHelper, configHelper, infoHelper), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/service/refresh", new ServiceRefreshHandler(dataAccess, gson, accessHelper, configHelper, infoHelper), false);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/service/version", new VersionsGetHandler(dataAccess, gson, moduleArtifactHelper, moduleConfigHelper, configHelper), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/service/notuses", new ServiceNotUsesGetHandler(dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/service/audit", new AuditGetHandler(dataAccess, gson), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/service/auditOutput", new AuditOutputGetHandler(dataAccess, gson), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/service/create", new ServiceCreateHandler(dataAccess, gson, accessHelper, workItemProcessor), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/service/modify", new ServiceModifyHandler(dataAccess, gson, accessHelper, workItemProcessor), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/service/resetAll", new ServiceResetAllHandler(dataAccess, gson, accessHelper, workItemProcessor), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/service/delete", new ServiceDeleteHandler(dataAccess, gson, accessHelper, workItemProcessor), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/service/createRef", new ServiceRefCreateHandler(dataAccess, gson, accessHelper), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/service/deleteRef", new ServiceRefDeleteHandler(dataAccess, gson, accessHelper), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/service/build", new ServiceBuildHandler(dataAccess, gson, accessHelper, new ServiceBuildHelper(dataAccess, client, parameters, gson)), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/service/transfer", new ServiceTransferHandler(dataAccess, gson, accessHelper, workItemProcessor), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/vip/details", new VipGetDetailsHandler(dataAccess, gson, vipDetailsHelper), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/vip/create", new VipCreateHandler(dataAccess, gson, accessHelper, workItemProcessor), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/vip/backfill", new VipBackfillHandler(dataAccess, gson, accessHelper), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/vip/modify", new VipModifyHandler(dataAccess, gson, accessHelper, workItemProcessor), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/vip/delete", new VipDeleteHandler(dataAccess, gson, accessHelper, workItemProcessor), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/vip/migrate", new VipMigrateHandler(dataAccess, gson, accessHelper, workItemProcessor), true);
        routingHandler.add(MethodRule.GET, TargetRule.STARTS_WITH, "/v1/endpoint/", new EndpointGetHandler(dataAccess, gson), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/endpoint", new EndpointsGetHandler(dataAccess, gson), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/module/file", new ModuleFileGetHandler(dataAccess, gson, accessHelper), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/module/create", new ModuleCreateHandler(dataAccess, gson, accessHelper, configHelper, workItemProcessor, folderHelper), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/module/modify", new ModuleModifyHandler(dataAccess, gson, accessHelper, configHelper, workItemProcessor, folderHelper), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/module/file", new ModuleFileCreateHandler(dataAccess, gson, accessHelper), true);
        routingHandler.add(MethodRule.DELETE, TargetRule.EQUALS, "/v1/module/file", new ModuleFileDeleteHandler(dataAccess, gson, accessHelper), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/module/delete", new ModuleDeleteHandler(dataAccess, gson, accessHelper, workItemProcessor), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/host/details", new HostGetDetailsHandler(dataAccess, gson, hostDetailsHelper), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/host/find", new HostFindHandler(dataAccess, gson, infoHelper, hostDetailsHelper), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/host/create", new HostCreateHandler(dataAccess, gson, accessHelper, configHelper, workItemProcessor), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/host/deploy", new HostDeploySoftwareHandler(dataAccess, gson, accessHelper, configHelper, infoHelper, workItemProcessor), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/host/restart", new HostRestartHandler(dataAccess, gson, accessHelper, workItemProcessor), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/host/reboot", new HostRebootHandler(dataAccess, gson, accessHelper, workItemProcessor), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/host/delete", new HostDeleteHandler(dataAccess, gson, accessHelper, workItemProcessor), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/host/backfill", new HostBackfillHandler(dataAccess, gson, accessHelper, configHelper, parameters), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/hosts", new HostsGetHandler(dataAccess, gson), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/st/exec", new SmokeTestExecHandler(dataAccess, gson, accessHelper, smokeTestHelper), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/cf/exec", new CustomFuntionExecHandler(dataAccess, gson, accessHelper, client), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/cf/create", new CustomFuntionCreateHandler(dataAccess, gson, accessHelper), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/cf/modify", new CustomFuntionModifyHandler(dataAccess, gson, accessHelper), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/cf/delete", new CustomFuntionDeleteHandler(dataAccess, gson, accessHelper), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/document/create", new DocumentCreateHandler(dataAccess, gson, accessHelper), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/document/delete", new DocumentDeleteHandler(dataAccess, gson, accessHelper), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/document", new DocumentGetHandler(dataAccess, gson, client, parameters), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/workitems", new WorkItemGetHandler(dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/datastore", new DataStoreGetHandler(), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/graph/all", new GraphAllHandler(dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.MATCHES, "/v1/graph/fanin/\\w+-\\w+-\\w+-\\w+-\\w+", new GraphFanInHandler(dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.MATCHES, "/v1/graph/fanout/\\w+-\\w+-\\w+-\\w+-\\w+", new GraphFanOutHandler(dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/audit", new AuditCreateHandler(dataAccess, gson, accessHelper), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/sendMessage", new MessageSendHandler(dataAccess, gson, accessHelper, messagingCoodinator), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/convert", new ConvertHandler(dataAccess, gson, accessHelper, configHelper), true);
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
            LOGGER.info("Jetty server started on port {}, joining with server thread now", port);
            server.join();
        } catch (BindException be) {
            LOGGER.error("Can not bind to port {}, exiting", port);
            System.exit(0);
        } catch (Exception ex) {
            LOGGER.error("Exception {} occured", ex.getMessage());
        }
    }

}
