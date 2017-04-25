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
import com.northernwall.hadrian.GMT;
import com.northernwall.hadrian.config.Const;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.db.SearchResult;
import com.northernwall.hadrian.db.SearchSpace;
import com.northernwall.hadrian.domain.Audit;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Thurston
 */
public class HostDeleteHandler extends BasicHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(HostDeleteHandler.class);

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
        if (hosts != null && !hosts.isEmpty()) {
            for (Host host : hosts) {
                if (host.getModuleId().equals(module.getModuleId())
                        && host.getEnvironment().equals(data.environment)
                        && data.hostNames.contains(host.getHostName())
                        && !host.isBusy()) {
                    if (checkIfInventoryOnly(host, data)) {
                        inventoryOnly(data, user, team, service, module, host);
                    } else {
                        decommissionHost(data, user, team, service, module, host);
                    }
                }
            }
        }

        response.setStatus(200);
        request.setHandled(true);
    }

    private boolean checkIfInventoryOnly(Host host, DeleteHostData data) {
        List<SearchResult> searchResults = getDataAccess().doSearchList(
                SearchSpace.hostName,
                host.getHostName());
        if (searchResults == null || searchResults.isEmpty()) {
            LOGGER.warn("Deleting host {} ({}) that can not be found in search",
                    host.getHostName(),
                    host.getHostId());
            return data.inventoryOnly;
        } else if (searchResults.size() > 1) {
            return true;
        }
        return data.inventoryOnly;
    }

    private void inventoryOnly(DeleteHostData data, User user, Team team, Service service, Module module, Host host) throws IOException {
        Audit audit = new Audit();
        audit.serviceId = data.serviceId;
        audit.setTimePerformed(GMT.getGmtAsDate());
        audit.timeRequested = GMT.getGmtAsDate();
        audit.requestor = user.getUsername();
        audit.type = Type.host;
        audit.operation = Operation.delete;
        audit.successfull = true;
        audit.moduleName = module.getModuleName();
        audit.hostName = host.getHostName();

        Map<String, String> notes = new HashMap<>();
        notes.put("Inventory_Only", "true");
        notes.put("Reason", data.reason);
        audit.notes = getGson().toJson(notes);

        getDataAccess().saveAudit(audit, null);

        getDataAccess().deleteHost(host);
        getDataAccess().deleteSearch(
                SearchSpace.hostName,
                host.getHostName(),
                host.getHostId());

        if (service.isDoManageVip()) {
            List<WorkItem> workItems = new ArrayList<>(1);
            WorkItem workItemDisable = new WorkItem(Type.host, Operation.removeVips, user, team, service, module, host, null);
            workItems.add(workItemDisable);
            workItemProcessor.processWorkItems(workItems);
        }

    }

    private void decommissionHost(DeleteHostData data, User user, Team team, Service service, Module module, Host host) throws IOException {
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
