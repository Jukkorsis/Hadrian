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
import com.northernwall.hadrian.service.dao.DeleteHostData;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import java.io.IOException;
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
    private final DataAccess dataAccess;
    private final WorkItemProcessor workItemProcess;

    public HostDeleteHandler(AccessHelper accessHelper, DataAccess dataAccess, WorkItemProcessor workItemProcess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
        this.dataAccess = dataAccess;
        this.workItemProcess = workItemProcess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        DeleteHostData deleteHostData = fromJson(request, DeleteHostData.class);
        Service service = getService(deleteHostData.serviceId, deleteHostData.serviceName, deleteHostData.serviceAbbr);
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "delete host");
        Team team = dataAccess.getTeam(service.getTeamId());

        Module module = getModule(deleteHostData.moduleId, deleteHostData.moduleName, service);

        List<Host> hosts = dataAccess.getHosts(service.getServiceId());
        if (hosts == null || hosts.isEmpty()) {
            return;
        }
        for (Host host : hosts) {
            if (host.getModuleId().equals(module.getModuleId()) && host.getNetwork().equals(deleteHostData.network)) {
                if (deleteHostData.hostNames.contains(host.getHostName())) {
                    if (host.getStatus().equals(Const.NO_STATUS)) {
                        host.setStatus("Deleting...");
                        dataAccess.updateHost(host);
                        WorkItem workItem = new WorkItem(Type.host, Operation.delete, user, team, service, module, host, null);
                        workItem.getHost().reason = deleteHostData.reason;
                        dataAccess.saveWorkItem(workItem);
                        workItemProcess.sendWorkItem(workItem);
                    }
                }
            }
        }

        response.setStatus(200);
        request.setHandled(true);
    }

}
