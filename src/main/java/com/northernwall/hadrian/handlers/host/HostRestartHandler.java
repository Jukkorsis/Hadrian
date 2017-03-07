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
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import com.northernwall.hadrian.handlers.host.dao.PutRestartHostData;
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
public class HostRestartHandler extends BasicHandler {

    private final AccessHelper accessHelper;
    private final WorkItemProcessor workItemProcessor;

    public HostRestartHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper, WorkItemProcessor workItemProcessor) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
        this.workItemProcessor = workItemProcessor;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PutRestartHostData data = fromJson(request, PutRestartHostData.class);
        Service service = getService(data.serviceId, data.serviceName);
        Team team = getTeam(service.getTeamId(), null);
        User user = accessHelper.checkIfUserCanRestart(request, team);

        if (!service.isDoDeploys()) {
            throw new Http400BadRequestException("Service is configurationed to not allow deployments");
        }

        Module module = getModule(data.moduleId, data.moduleName, service);

        List<Host> hosts = getDataAccess().getHosts(service.getServiceId());
        if (hosts == null || hosts.isEmpty()) {
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
                                    "Restarting...");
                        } else {
                            getDataAccess().updateStatus(
                                    host.getHostId(),
                                    true,
                                    "Restart Queued");
                        }

                        WorkItem workItem;
                        if (service.isDoManageVip()) {
                            workItem = new WorkItem(Type.host, Operation.disableVips, user, team, service, module, host, null);
                            workItems.add(workItem);
                        }

                        workItem = new WorkItem(Type.host, Operation.restart, user, team, service, module, host, null);
                        workItem.setReason(data.reason);
                        workItem.getHost().doOsUpgrade = data.doOsUpgrade;
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
                        workItem.setReason("Last restarted %% ago");
                        workItems.add(workItem);
                    }
                }
            }
        }

        workItemProcessor.processWorkItems(workItems);

        int status = 200;
        if (data.wait) {
            status = workItemProcessor.waitForProcess(
                    workItems.get(workItems.size() - 1).getId(),
                    (module.getStartTimeOut() + module.getStopTimeOut()) * 100,
                    workItems.size() * (module.getStartTimeOut() + module.getStopTimeOut()) * 1_500,
                    service.getServiceName() + " " + module.getModuleName());
        }

        response.setStatus(status);
        request.setHandled(true);
    }

}
