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
import com.northernwall.hadrian.maven.MavenHelper;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.service.AuditHandler;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import com.northernwall.hadrian.service.CalendarGetHandler;
import com.northernwall.hadrian.service.ConfigGetHandler;
import com.northernwall.hadrian.service.CustomFuntionCreateHandler;
import com.northernwall.hadrian.service.CustomFuntionGetHandler;
import com.northernwall.hadrian.service.CustomFuntionDeleteHandler;
import com.northernwall.hadrian.service.CustomFuntionModifyHandler;
import com.northernwall.hadrian.service.DataStoreGetHandler;
import com.northernwall.hadrian.service.HostBackfillHandler;
import com.northernwall.hadrian.service.HostCreateHandler;
import com.northernwall.hadrian.service.HostDeleteHandler;
import com.northernwall.hadrian.service.HostDeploySoftwareHandler;
import com.northernwall.hadrian.service.HostGetHandler;
import com.northernwall.hadrian.service.HostVipDeleteHandler;
import com.northernwall.hadrian.service.HostRestartHandler;
import com.northernwall.hadrian.service.HostVipCreateHandler;
import com.northernwall.hadrian.service.ModuleCreateHandler;
import com.northernwall.hadrian.service.ModuleDeleteHandler;
import com.northernwall.hadrian.service.ModuleModifyHandler;
import com.northernwall.hadrian.service.ServiceCreateHandler;
import com.northernwall.hadrian.service.ServiceAuditGetHandler;
import com.northernwall.hadrian.service.ServiceGetHandler;
import com.northernwall.hadrian.service.ServiceNotUsesGetHandler;
import com.northernwall.hadrian.service.ServiceModifyHandler;
import com.northernwall.hadrian.service.ServiceRefCreateHandler;
import com.northernwall.hadrian.service.ServiceRefDeleteHandler;
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
        
        RoutingHandler routingHandler = new RoutingHandler();
        //These urls do not require a login
        routingHandler.addUtilityRoute(MethodRule.GET, TargetRule.equals, "/availability", new AvailabilityHandler(dataAccess));
        routingHandler.addRoute(MethodRule.GET, TargetRule.equals, "/version", new VersionHandler());
        routingHandler.addRoute(MethodRule.GET, TargetRule.equals, "/health", new HealthHandler(accessHandler, calendarHelper, dataAccess, mavenHelper, parameters, workItemProcess.getWorkItemSender()));
        routingHandler.addUtilityRoute(MethodRule.GET, TargetRule.startWith, "/ui/", new ContentHandler("/webcontent"));
        routingHandler.addRoute(MethodRule.POST, TargetRule.startWith, "/webhook/callback", new WorkItemCallbackHandler(workItemProcess));
        routingHandler.addUtilityRoute(MethodRule.GET, TargetRule.equals, "/favicon.ico", new FaviconHandler());
        //Accees Handler
        routingHandler.addRoute(MethodRule.ANY, TargetRule.any, "/", accessHandler);
        //These urls require a login
        routingHandler.addRoute(MethodRule.GET, TargetRule.equals, "/v1/config", new ConfigGetHandler(configHelper));
        routingHandler.addUtilityRoute(MethodRule.GET, TargetRule.startWith, "/ui/", new ContentHandler("/webapp"));
        routingHandler.addRoute(MethodRule.GET, TargetRule.equals, "/v1/tree", new TreeHandler(dataAccess));
        routingHandler.addRoute(MethodRule.GET, TargetRule.equals, "/v1/users", new UserGetHandler(dataAccess));
        routingHandler.addRoute(MethodRule.PUT, TargetRule.startWith, "/v1/user/", new UserModifyHandler(accessHelper, dataAccess));
        routingHandler.addRoute(MethodRule.GET, TargetRule.matches, "/v1/team/\\w+-\\w+-\\w+-\\w+-\\w+", new TeamGetHandler(accessHelper, dataAccess));
        routingHandler.addRoute(MethodRule.POST, TargetRule.equals, "/v1/team", new TeamCreateHandler(accessHelper, dataAccess));
        routingHandler.addRoute(MethodRule.PUT, TargetRule.matches, "/v1/team/\\w+-\\w+-\\w+-\\w+-\\w+", new TeamModifyHandler(accessHelper, dataAccess));
        routingHandler.addRoute(MethodRule.PUT, TargetRule.matches, "/v1/team/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+", new TeamAddUserHandler(accessHelper, dataAccess));
        routingHandler.addRoute(MethodRule.DELETE, TargetRule.matches, "/v1/team/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+", new TeamRemoveUserHandler(accessHelper, dataAccess));
        routingHandler.addRoute(MethodRule.GET, TargetRule.equals, "/v1/service", new ServicesGetHandler(dataAccess));
        routingHandler.addRoute(MethodRule.GET, TargetRule.matches, "/v1/service/\\w+-\\w+-\\w+-\\w+-\\w+/notuses", new ServiceNotUsesGetHandler(dataAccess));
        routingHandler.addRoute(MethodRule.GET, TargetRule.matches, "/v1/service/\\w+-\\w+-\\w+-\\w+-\\w+/audit", new ServiceAuditGetHandler(dataAccess));
        routingHandler.addRoute(MethodRule.GET, TargetRule.matches, "/v1/service/\\w+-\\w+-\\w+-\\w+-\\w+", new ServiceGetHandler(accessHelper, dataAccess, configHelper, mavenHelper, infoHelper));
        routingHandler.addRoute(MethodRule.POST, TargetRule.equals, "/v1/service/service", new ServiceCreateHandler(accessHelper, dataAccess));
        routingHandler.addRoute(MethodRule.POST, TargetRule.matches, "/v1/service/\\w+-\\w+-\\w+-\\w+-\\w+/ref", new ServiceRefCreateHandler(accessHelper, dataAccess));
        routingHandler.addRoute(MethodRule.PUT, TargetRule.matches, "/v1/service/\\w+-\\w+-\\w+-\\w+-\\w+", new ServiceModifyHandler(accessHelper, dataAccess));
        routingHandler.addRoute(MethodRule.DELETE, TargetRule.matches, "/v1/service/\\w+-\\w+-\\w+-\\w+-\\w+/uses/\\w+-\\w+-\\w+-\\w+-\\w+", new ServiceRefDeleteHandler(accessHelper, dataAccess));
        routingHandler.addRoute(MethodRule.POST, TargetRule.equals, "/v1/vip/vip", new VipCreateHandler(accessHelper, dataAccess, workItemProcess));
        routingHandler.addRoute(MethodRule.PUT, TargetRule.matches, "/v1/vip/\\w+-\\w+-\\w+-\\w+-\\w+", new VipModifyHandler(accessHelper, dataAccess, workItemProcess));
        routingHandler.addRoute(MethodRule.DELETE, TargetRule.matches, "/v1/vip/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+-\\w+-\\w+-\\w+-\\w+", new VipDeleteHandler(accessHelper, dataAccess, workItemProcess));
        routingHandler.addRoute(MethodRule.POST, TargetRule.equals, "/v1/module", new ModuleCreateHandler(accessHelper, configHelper, dataAccess, workItemProcess));
        routingHandler.addRoute(MethodRule.PUT, TargetRule.matches, "/v1/module/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+-\\w+-\\w+-\\w+-\\w+", new ModuleModifyHandler(accessHelper, dataAccess, workItemProcess));
        routingHandler.addRoute(MethodRule.DELETE, TargetRule.matches, "/v1/module/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+-\\w+-\\w+-\\w+-\\w+", new ModuleDeleteHandler(accessHelper, dataAccess, workItemProcess));
        routingHandler.addRoute(MethodRule.GET, TargetRule.matches, "/v1/host/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+-\\w+-\\w+-\\w+-\\w+/details", new HostGetHandler(dataAccess, hostDetailsHelper));
        routingHandler.addRoute(MethodRule.POST, TargetRule.equals, "/v1/host/host", new HostCreateHandler(accessHelper, configHelper, dataAccess, workItemProcess));
        routingHandler.addRoute(MethodRule.PUTPOST, TargetRule.equals, "/v1/host/deploy", new HostDeploySoftwareHandler(accessHelper, configHelper, dataAccess, workItemProcess));
        routingHandler.addRoute(MethodRule.PUTPOST, TargetRule.equals, "/v1/host/restart", new HostRestartHandler(accessHelper, dataAccess, workItemProcess));
        routingHandler.addRoute(MethodRule.DELETE, TargetRule.matches, "/v1/host/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+-\\w+-\\w+-\\w+-\\w+", new HostDeleteHandler(accessHelper, dataAccess, workItemProcess));
        routingHandler.addRoute(MethodRule.POST, TargetRule.equals, "/v1/host/backfill", new HostBackfillHandler(accessHelper, configHelper, dataAccess));
        routingHandler.addRoute(MethodRule.POST, TargetRule.equals, "/v1/host/vips", new HostVipCreateHandler(accessHelper, dataAccess, workItemProcess));
        routingHandler.addRoute(MethodRule.DELETE, TargetRule.matches, "/v1/host/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+-\\w+-\\w+-\\w+-\\w+/", new HostVipDeleteHandler(accessHelper, dataAccess, workItemProcess));
        routingHandler.addRoute(MethodRule.GET, TargetRule.matches, "/v1/cf/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+-\\w+-\\w+-\\w+-\\w+", new CustomFuntionGetHandler(accessHelper, dataAccess, client));
        routingHandler.addRoute(MethodRule.POST, TargetRule.equals, "/v1/cf/cf", new CustomFuntionCreateHandler(accessHelper, dataAccess));
        routingHandler.addRoute(MethodRule.PUT, TargetRule.matches, "/v1/cf/\\w+-\\w+-\\w+-\\w+-\\w+", new CustomFuntionModifyHandler(accessHelper, dataAccess));
        routingHandler.addRoute(MethodRule.DELETE, TargetRule.matches, "/v1/cf/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+-\\w+-\\w+-\\w+-\\w+", new CustomFuntionDeleteHandler(accessHelper, dataAccess));
        routingHandler.addRoute(MethodRule.GET, TargetRule.equals, "/v1/workitems", new WorkItemGetHandler(dataAccess));
        routingHandler.addRoute(MethodRule.GET, TargetRule.equals, "/v1/datastore", new DataStoreGetHandler());
        routingHandler.addRoute(MethodRule.GET, TargetRule.equals, "/v1/calendar", new CalendarGetHandler(dataAccess, calendarHelper));
        routingHandler.addRoute(MethodRule.GET, TargetRule.equals, "/v1/graph/all", new GraphAllHandler(dataAccess));
        routingHandler.addRoute(MethodRule.GET, TargetRule.matches, "/v1/graph/fanin/\\w+-\\w+-\\w+-\\w+-\\w+", new GraphFanInHandler(dataAccess));
        routingHandler.addRoute(MethodRule.GET, TargetRule.matches, "/v1/graph/fanout/\\w+-\\w+-\\w+-\\w+-\\w+", new GraphFanOutHandler(dataAccess));
        routingHandler.addRoute(MethodRule.POST, TargetRule.equals, "/v1/audit", new AuditHandler(dataAccess, accessHelper));
        //Catch all handler
        routingHandler.addRoute(MethodRule.ANY, TargetRule.any, "/", new RedirectHandler());
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
