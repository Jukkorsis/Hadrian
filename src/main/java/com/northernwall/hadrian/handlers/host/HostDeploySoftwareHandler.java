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
package com.northernwall.hadrian.handlers.host;

import com.google.gson.Gson;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.config.ConfigHelper;
import com.northernwall.hadrian.config.Const;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Environment;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import com.northernwall.hadrian.handlers.host.dao.PutDeploySoftwareData;
import com.northernwall.hadrian.handlers.service.helper.InfoHelper;
import com.northernwall.hadrian.handlers.routing.Http400BadRequestException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard Thurston
 */
public class HostDeploySoftwareHandler extends BasicHandler {

    private final AccessHelper accessHelper;
    private final ConfigHelper configHelper;
    private final InfoHelper infoHelper;
    private final WorkItemProcessor workItemProcessor;

    public HostDeploySoftwareHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper, ConfigHelper configHelper, InfoHelper infoHelper, WorkItemProcessor workItemProcessor) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
        this.configHelper = configHelper;
        this.infoHelper = infoHelper;
        this.workItemProcessor = workItemProcessor;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PutDeploySoftwareData data = fromJson(request, PutDeploySoftwareData.class);
        Service service = getService(data.serviceId, data.serviceName);
        Team team = getTeam(service.getTeamId(), null);
        User user = accessHelper.checkIfUserCanDeploy(request, team);

        if (!service.isDoDeploys()) {
            throw new Http400BadRequestException("Service is configurationed to not allow deployments");
        }

        Module module = getModule(data.moduleId, data.moduleName, service);

        Environment environment = null;
        for (Environment temp : configHelper.getConfig().environments) {
            if (temp.name.equals(data.environment)) {
                environment = temp;
            }
        }
        if (environment == null) {
            throw new Http400BadRequestException("Unknown environment " + data.environment);
        }
        if (!environment.allowUrl && data.versionUrl != null) {
            throw new Http400BadRequestException(("Environment " + environment.name + " does not allow versionUrl"));
        }
        if (data.version != null && data.versionUrl != null) {
            throw new Http400BadRequestException("Only one of version and versionUrl can be specified");
        }
        if ((data.version == null || data.version.isEmpty())
                && (data.versionUrl == null || data.versionUrl.isEmpty())) {
            throw new Http400BadRequestException("One of version and versionUrl must be specified");
        }

        List<Host> hosts = getDataAccess().getHosts(service.getServiceId());
        if (hosts == null || hosts.isEmpty()) {
            response.setStatus(200);
            request.setHandled(true);
            return;
        }
        List<WorkItem> workItems = new LinkedList<>();
        for (Host host : hosts) {
            if (host.getModuleId().equals(module.getModuleId()) && host.getEnvironment().equals(data.environment)) {
                if (data.all || data.hostNames.contains(host.getHostName())) {
                    if (!host.isBusy()) {
                        if (workItems.isEmpty()) {
                            getDataAccess().updateStatus(
                                    host.getHostId(),
                                    true,
                                    "Deploying...",
                                    Const.STATUS_WIP);
                        } else {
                            getDataAccess().updateStatus(
                                    host.getHostId(),
                                    true,
                                    "Deploy Queued",
                                    Const.STATUS_WIP);
                        }

                        WorkItem workItem;
                        if (service.isDoManageVip()) {
                            workItem = new WorkItem(Type.host, Operation.disableVips, user, team, service, module, host, null);
                            workItems.add(workItem);
                        }

                        workItem = new WorkItem(Type.host, Operation.deploy, user, team, service, module, host, null);
                        workItem.setReason(data.reason);
                        workItem.getHost().version = data.version;
                        workItem.getHost().prevVersion = infoHelper.readVersion(host.getHostName(), module.getVersionUrl());
                        workItem.getHost().versionUrl = data.versionUrl;
                        workItem.getHost().configVersion = data.configVersion;
                        workItems.add(workItem);

                        if (module.getSmokeTestUrl() != null && !module.getSmokeTestUrl().isEmpty()) {
                            workItem = new WorkItem(Type.host, Operation.smokeTest, user, team, service, module, host, null);
                            workItems.add(workItem);
                        }

                        if (service.isDoManageVip()) {
                            workItem = new WorkItem(Type.host, Operation.enableVips, user, team, service, module, host, null);
                            workItems.add(workItem);
                        }

                        workItem = new WorkItem(Type.host, Operation.status, user, team, service, module, host, null);
                        workItem.setReason("Deployed %% ago");
                        workItems.add(workItem);
                    }
                }
            }
        }

        if (workItems.isEmpty()) {
            response.setStatus(200);
            request.setHandled(true);
            return;
        }

        workItemProcessor.processWorkItems(workItems);

        if (!data.wait) {
            response.setStatus(200);
            request.setHandled(true);
            return;
        }

        response.setStatus(workItemProcessor.waitForProcess(
                workItems.get(workItems.size() - 1).getId(),
                module.getStartTimeOut() * 100,
                workItems.size() * module.getStartTimeOut() * 1_500,
                service.getServiceName() + " " + module.getModuleName() + " " + data.version));
        request.setHandled(true);
    }

}
