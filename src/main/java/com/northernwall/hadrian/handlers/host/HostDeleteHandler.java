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
import com.northernwall.hadrian.Const;
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
import com.northernwall.hadrian.handlers.host.dao.DeleteHostData;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
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
public class HostDeleteHandler extends BasicHandler {

    private final AccessHelper accessHelper;
    private final WorkItemProcessor workItemProcessor;

    public HostDeleteHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper, WorkItemProcessor workItemProcessor) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
        this.workItemProcessor = workItemProcessor;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        DeleteHostData data = fromJson(request, DeleteHostData.class);
        Service service = getService(data.serviceId, data.serviceName);
        Team team = getTeam(service.getTeamId(), null);
        User user = accessHelper.checkIfUserCanModify(request, team, "delete host");

        Module module = getModule(data.moduleId, data.moduleName, service);

        List<Host> hosts = getDataAccess().getHosts(service.getServiceId());
        if (hosts == null || hosts.isEmpty()) {
            return;
        }
        for (Host host : hosts) {
            if (host.getModuleId().equals(module.getModuleId()) && host.getEnvironment().equals(data.environment)) {
                if (data.hostNames.contains(host.getHostName())) {
                    if (!host.isBusy()) {
                        getDataAccess().updateStatus(
                                host.getHostId(),
                                true,
                                "Deleting...",
                                Const.STATUS_WIP);

                        List<WorkItem> workItems = new ArrayList<>(2);

                        if (service.isDoManageVip()) {
                            WorkItem workItemDisable = new WorkItem(Type.host, Operation.removeVips, user, team, service, module, host, null);
                            workItems.add(workItemDisable);
                        }

                        WorkItem workItemDelete = new WorkItem(Type.host, Operation.delete, user, team, service, module, host, null);
                        workItemDelete.setReason(data.reason);
                        workItems.add(workItemDelete);

                        workItemProcessor.processWorkItems(workItems);
                    }
                }
            }
        }

        response.setStatus(200);
        request.setHandled(true);
    }

}
