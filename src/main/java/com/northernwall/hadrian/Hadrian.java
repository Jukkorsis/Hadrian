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

import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.calendar.CalendarHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.details.HostDetailsHelper;
import com.northernwall.hadrian.details.ServiceBuildHelper;
import com.northernwall.hadrian.details.VipDetailsHelper;
import com.northernwall.hadrian.handlers.graph.GraphAllHandler;
import com.northernwall.hadrian.handlers.graph.GraphFanInHandler;
import com.northernwall.hadrian.handlers.graph.GraphFanOutHandler;
import com.northernwall.hadrian.handlers.service.AuditCreateHandler;
import com.northernwall.hadrian.handlers.service.AuditGetHandler;
import com.northernwall.hadrian.handlers.service.AuditOutputGetHandler;
import com.northernwall.hadrian.handlers.service.CalendarGetHandler;
import com.northernwall.hadrian.handlers.service.ConfigGetHandler;
import com.northernwall.hadrian.handlers.service.CustomFuntionCreateHandler;
import com.northernwall.hadrian.handlers.service.CustomFuntionExecHandler;
import com.northernwall.hadrian.handlers.service.CustomFuntionDeleteHandler;
import com.northernwall.hadrian.handlers.service.CustomFuntionModifyHandler;
import com.northernwall.hadrian.handlers.service.DataStoreGetHandler;
import com.northernwall.hadrian.handlers.service.DocumentCreateHandler;
import com.northernwall.hadrian.handlers.service.DocumentDeleteHandler;
import com.northernwall.hadrian.handlers.service.DocumentGetHandler;
import com.northernwall.hadrian.handlers.service.HostBackfillHandler;
import com.northernwall.hadrian.handlers.service.HostCreateHandler;
import com.northernwall.hadrian.handlers.service.HostDeleteHandler;
import com.northernwall.hadrian.handlers.service.HostDeploySoftwareHandler;
import com.northernwall.hadrian.handlers.service.HostFindHandler;
import com.northernwall.hadrian.handlers.service.HostGetDetailsHandler;
import com.northernwall.hadrian.handlers.service.HostRestartHandler;
import com.northernwall.hadrian.handlers.service.ModuleCreateHandler;
import com.northernwall.hadrian.handlers.service.ModuleDeleteHandler;
import com.northernwall.hadrian.handlers.service.ModuleFileDeleteHandler;
import com.northernwall.hadrian.handlers.service.ModuleModifyHandler;
import com.northernwall.hadrian.handlers.service.ModuleFileCreateHandler;
import com.northernwall.hadrian.handlers.service.ModuleFileGetHandler;
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
import com.northernwall.hadrian.handlers.service.VipCreateHandler;
import com.northernwall.hadrian.handlers.service.VipDeleteHandler;
import com.northernwall.hadrian.handlers.service.VipFixHandler;
import com.northernwall.hadrian.handlers.service.VipModifyHandler;
import com.northernwall.hadrian.handlers.service.VipGetDetailsHandler;
import com.northernwall.hadrian.handlers.service.helper.FolderHelper;
import com.northernwall.hadrian.handlers.service.helper.InfoHelper;
import com.northernwall.hadrian.handlers.team.TeamAddUserHandler;
import com.northernwall.hadrian.handlers.team.TeamCreateHandler;
import com.northernwall.hadrian.handlers.team.TeamGetHandler;
import com.northernwall.hadrian.handlers.team.TeamModifyHandler;
import com.northernwall.hadrian.handlers.team.TeamRemoveUserHandler;
import com.northernwall.hadrian.handlers.tree.CatalogHandler;
import com.northernwall.hadrian.handlers.tree.TreeHandler;
import com.northernwall.hadrian.handlers.user.UserGetHandler;
import com.northernwall.hadrian.handlers.user.UserModifyHandler;
import com.northernwall.hadrian.handlers.utility.AvailabilityHandler;
import com.northernwall.hadrian.handlers.utility.ContentHandler;
import com.northernwall.hadrian.handlers.utility.ConvertHandler;
import com.northernwall.hadrian.handlers.utility.FaviconHandler;
import com.northernwall.hadrian.handlers.utility.HealthHandler;
import com.northernwall.hadrian.handlers.utility.MetricHandler;
import com.northernwall.hadrian.handlers.utility.RedirectHandler;
import com.northernwall.hadrian.handlers.utility.VersionHandler;
import com.northernwall.hadrian.handlers.utility.routingHandler.MethodRule;
import com.northernwall.hadrian.handlers.utility.routingHandler.TargetRule;
import com.northernwall.hadrian.handlers.utility.routingHandler.RoutingHandler;
import com.northernwall.hadrian.handlers.workitem.WorkItemGetHandler;
import com.northernwall.hadrian.module.ModuleArtifactHelper;
import com.northernwall.hadrian.messaging.MessageSendHandler;
import com.northernwall.hadrian.messaging.MessagingCoodinator;
import com.northernwall.hadrian.module.ModuleConfigHelper;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.schedule.Scheduler;
import com.northernwall.hadrian.workItem.WorkItemCallbackHandler;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import com.squareup.okhttp.OkHttpClient;
import java.net.BindException;
import org.dsh.metrics.MetricRegistry;
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
    private final CalendarHelper calendarHelper;
    private final WorkItemProcessor workItemProcessor;
    private final Scheduler scheduler;
    private final FolderHelper folderHelper;
    private final MetricRegistry metricRegistry;
    private final MessagingCoodinator messagingCoodinator;
    private final InfoHelper infoHelper;
    private int port;
    private Server server;

    Hadrian(Parameters parameters, OkHttpClient client, ConfigHelper configHelper, DataAccess dataAccess, ModuleArtifactHelper moduleArtifactHelper, ModuleConfigHelper moduleConfigHelper, AccessHelper accessHelper, Handler accessHandler, HostDetailsHelper hostDetailsHelper, VipDetailsHelper vipDetailsHelper, CalendarHelper calendarHelper, WorkItemProcessor workItemProcessor, Scheduler scheduler, FolderHelper folderHelper, MetricRegistry metricRegistry, MessagingCoodinator messagingCoodinator) {
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
        this.calendarHelper = calendarHelper;
        this.workItemProcessor = workItemProcessor;
        this.scheduler = scheduler;
        this.folderHelper = folderHelper;
        this.metricRegistry = metricRegistry;
        this.messagingCoodinator = messagingCoodinator;

        infoHelper = new InfoHelper(parameters, client);

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
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/health", new HealthHandler(accessHandler, calendarHelper, dataAccess, moduleArtifactHelper, moduleConfigHelper, parameters, messagingCoodinator, scheduler), true);
        routingHandler.add(MethodRule.GET, TargetRule.STARTS_WITH, "/ui/", new ContentHandler("/webcontent"), false);
        routingHandler.add(MethodRule.POST, TargetRule.STARTS_WITH, "/webhook/callback", new WorkItemCallbackHandler(workItemProcessor), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/favicon.ico", new FaviconHandler(), false);
        //Accees Handler
        routingHandler.add(MethodRule.ANY, TargetRule.ANY, "/", accessHandler, false);
        //These urls require a login
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/config", new ConfigGetHandler(configHelper), true);
        routingHandler.add(MethodRule.GET, TargetRule.STARTS_WITH, "/ui/", new ContentHandler("/webapp"), false);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/tree", new TreeHandler(dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/catalog", new CatalogHandler(dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/users", new UserGetHandler(dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/user/modify", new UserModifyHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/team", new TeamGetHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/team/create", new TeamCreateHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/team/modify", new TeamModifyHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/team/addUser", new TeamAddUserHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/team/removeUser", new TeamRemoveUserHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/services", new ServicesGetHandler(dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/service", new ServiceGetHandler(accessHelper, dataAccess, configHelper, infoHelper), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/service/refresh", new ServiceRefreshHandler(accessHelper, dataAccess, configHelper, infoHelper), false);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/service/version", new VersionsGetHandler(dataAccess, moduleArtifactHelper, moduleConfigHelper), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/service/notuses", new ServiceNotUsesGetHandler(dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/service/audit", new AuditGetHandler(dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/service/auditOutput", new AuditOutputGetHandler(dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/service/create", new ServiceCreateHandler(accessHelper, dataAccess, workItemProcessor), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/service/modify", new ServiceModifyHandler(accessHelper, dataAccess, workItemProcessor), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/service/resetAll", new ServiceResetAllHandler(accessHelper, dataAccess, workItemProcessor), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/service/delete", new ServiceDeleteHandler(accessHelper, dataAccess, workItemProcessor), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/service/createRef", new ServiceRefCreateHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/service/deleteRef", new ServiceRefDeleteHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/service/build", new ServiceBuildHandler(accessHelper, dataAccess, new ServiceBuildHelper(dataAccess, client, parameters)), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/vip/details", new VipGetDetailsHandler(dataAccess, vipDetailsHelper), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/vip/create", new VipCreateHandler(accessHelper, dataAccess, workItemProcessor), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/vip/modify", new VipModifyHandler(accessHelper, dataAccess, workItemProcessor), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/vip/delete", new VipDeleteHandler(accessHelper, dataAccess, workItemProcessor), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/vip/fix", new VipFixHandler(accessHelper, dataAccess, workItemProcessor), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/module/file", new ModuleFileGetHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/module/create", new ModuleCreateHandler(accessHelper, configHelper, dataAccess, workItemProcessor, folderHelper), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/module/modify", new ModuleModifyHandler(accessHelper, dataAccess, workItemProcessor, folderHelper), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/module/file", new ModuleFileCreateHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.DELETE, TargetRule.EQUALS, "/v1/module/file", new ModuleFileDeleteHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/module/delete", new ModuleDeleteHandler(accessHelper, dataAccess, workItemProcessor), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/host/details", new HostGetDetailsHandler(dataAccess, hostDetailsHelper), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/host/find", new HostFindHandler(dataAccess, infoHelper, hostDetailsHelper), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/host/create", new HostCreateHandler(accessHelper, configHelper, dataAccess, workItemProcessor), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/host/deploy", new HostDeploySoftwareHandler(accessHelper, configHelper, infoHelper, dataAccess, workItemProcessor), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/host/restart", new HostRestartHandler(accessHelper, dataAccess, workItemProcessor), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/host/delete", new HostDeleteHandler(accessHelper, dataAccess, workItemProcessor), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/host/backfill", new HostBackfillHandler(accessHelper, configHelper, dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/st/exec", new SmokeTestExecHandler(accessHelper, dataAccess, client, parameters), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/cf/exec", new CustomFuntionExecHandler(accessHelper, dataAccess, client), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/cf/create", new CustomFuntionCreateHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/cf/modify", new CustomFuntionModifyHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/cf/delete", new CustomFuntionDeleteHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/document/create", new DocumentCreateHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/document/delete", new DocumentDeleteHandler(accessHelper, dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/document", new DocumentGetHandler(dataAccess, client, parameters), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/workitems", new WorkItemGetHandler(dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/datastore", new DataStoreGetHandler(), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/calendar", new CalendarGetHandler(dataAccess, calendarHelper), true);
        routingHandler.add(MethodRule.GET, TargetRule.EQUALS, "/v1/graph/all", new GraphAllHandler(dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.MATCHES, "/v1/graph/fanin/\\w+-\\w+-\\w+-\\w+-\\w+", new GraphFanInHandler(dataAccess), true);
        routingHandler.add(MethodRule.GET, TargetRule.MATCHES, "/v1/graph/fanout/\\w+-\\w+-\\w+-\\w+-\\w+", new GraphFanOutHandler(dataAccess), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/audit", new AuditCreateHandler(dataAccess, accessHelper), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/sendMessage", new MessageSendHandler(dataAccess, accessHelper, messagingCoodinator), true);
        routingHandler.add(MethodRule.PUTPOST, TargetRule.EQUALS, "/v1/convert", new ConvertHandler(accessHelper, dataAccess, configHelper), true);
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
