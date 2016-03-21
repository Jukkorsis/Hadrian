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
package com.northernwall.hadrian.service;

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.Util;
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
import com.northernwall.hadrian.service.dao.PutRestartHostData;
import java.io.IOException;
import java.util.ArrayList;
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
    private final DataAccess dataAccess;
    private final WorkItemProcessor workItemProcess;

    public HostRestartHandler(AccessHelper accessHelper, DataAccess dataAccess, WorkItemProcessor workItemProcess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
        this.dataAccess = dataAccess;
        this.workItemProcess = workItemProcess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PutRestartHostData putRestartHostData = Util.fromJson(request, PutRestartHostData.class);
        Service service = getService(putRestartHostData.serviceId, putRestartHostData.serviceName, putRestartHostData.serviceAbbr);
        User user = accessHelper.checkIfUserCanRestart(request, service.getTeamId());
        Team team = dataAccess.getTeam(service.getTeamId());

        Module module = getModule(putRestartHostData.moduleId, putRestartHostData.moduleName, service);

        List<Host> hosts = dataAccess.getHosts(service.getServiceId());
        if (hosts == null || hosts.isEmpty()) {
            return;
        }
        List<WorkItem> workItems = new ArrayList<>(hosts.size());
        for (Host host : hosts) {
            if (host.getModuleId().equals(module.getModuleId()) && host.getNetwork().equals(putRestartHostData.network)) {
                if (putRestartHostData.all || putRestartHostData.hostNames.contains(host.getHostName())) {
                    if (host.getStatus().equals(Const.NO_STATUS)) {
                        WorkItem workItem = new WorkItem(Type.host, Operation.restart, user, team, service, module, host, null);
                        workItem.getHost().reason = putRestartHostData.reason;
                        if (workItems.isEmpty()) {
                            host.setStatus("Restarting...");
                        } else {
                            host.setStatus("Restart Queued");
                        }
                        dataAccess.updateHost(host);
                        workItems.add(workItem);
                    }
                }
            }
        }

        String prevId = null;
        int size = workItems.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                WorkItem workItem = workItems.get(size - i - 1);
                workItem.setNextId(prevId);
                prevId = workItem.getId();
                dataAccess.saveWorkItem(workItem);
            }
            workItemProcess.sendWorkItem(workItems.get(0));
            if (putRestartHostData.wait) {
                String lastId = workItems.get(size - 1).getId();
                for (int i = 0; i < 30; i++) {
                    try {
                        Thread.sleep(20_000);
                    } catch (InterruptedException ex) {
                    }
                    WorkItem workItem = dataAccess.getWorkItem(lastId);
                    if (workItem == null) {
                        response.setStatus(200);
                        request.setHandled(true);
                        return;
                    }
                }
            }
        }

        response.setStatus(200);
        request.setHandled(true);
    }

}
