/*
 * Copyright 2017 Richard Thurston.
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
package com.northernwall.hadrian.handlers.vip;

import com.google.gson.Gson;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.handlers.routing.Http400BadRequestException;
import com.northernwall.hadrian.handlers.vip.dao.DoVipHostData;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard
 */
public class VipHostHandler extends BasicHandler {

    private final AccessHelper accessHelper;
    private final WorkItemProcessor workItemProcessor;

    public VipHostHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper, WorkItemProcessor workItemProcessor) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
        this.workItemProcessor = workItemProcessor;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        DoVipHostData data = fromJson(request, DoVipHostData.class);
        Service service = getService(data.serviceId, null);
        Team team = getTeam(service.getTeamId(), null);

        if (data.action == null || data.action.isEmpty()) {
            throw new Http400BadRequestException("Action is missing");
        }

        User user = accessHelper.checkIfUserCanModify(request, team, data.action + " host - vip");

        if (data.hostName == null || data.hostName.isEmpty()) {
            throw new Http400BadRequestException("Hostname is null");
        }

        Vip vip = getVip(data.vipId, service);
        Module module = getDataAccess().getModule(service.getServiceId(), vip.getModuleId());
        if (module == null) {
            throw new Http400BadRequestException("Module could not be found");
        }

        List<Host> hosts = getDataAccess().getHosts(service.getServiceId());
        Host host = null;
        if (hosts != null && !hosts.isEmpty()) {
            for (Host tempHost : hosts) {
                if (tempHost.getHostName().equals(data.hostName)) {
                    host = tempHost;
                }
            }
        }
        if (host == null) {
            throw new Http400BadRequestException("Host could not be found");
        }
        
        if (!vip.getModuleId().equals(host.getModuleId())) {
            throw new Http400BadRequestException("Host and VIP don't belong to the same module");
        }
        if (!vip.getEnvironment().equals(host.getEnvironment())) {
            throw new Http400BadRequestException("Host and VIP don't belong to the same environment");
        }

        switch (data.action) {
            case "add":
                addHostToVip(vip, host, service, user, team, module);
                break;
            case "remove":
                removeHostFromVip(vip, host, service, user, team, module);
                break;
            case "enable":
                enableHostInVip(vip, host, service, user, team, module);
                break;
            case "disable":
                disableHostInVip(vip, host, service, user, team, module);
                break;
        }

        response.setStatus(200);
        request.setHandled(true);
    }

    private void addHostToVip(Vip vip, Host host, Service service, User user, Team team, Module module) throws IOException {
        if (vip.getBlackListHosts().contains(host.getHostName())) {
            vip.getBlackListHosts().remove(host.getHostName());
            getDataAccess().saveVip(vip);
        }

        WorkItem workItem = new WorkItem(Type.host, Operation.addVips, user, team, service, module, host, vip);
        workItemProcessor.processWorkItem(workItem);
    }

    private void removeHostFromVip(Vip vip, Host host, Service service, User user, Team team, Module module) throws IOException {
        if (!vip.getBlackListHosts().contains(host.getHostName())) {
            vip.getBlackListHosts().add(host.getHostName());
            getDataAccess().saveVip(vip);
        }

        WorkItem workItem = new WorkItem(Type.host, Operation.removeVips, user, team, service, module, host, vip);
        workItemProcessor.processWorkItem(workItem);
    }

    private void enableHostInVip(Vip vip, Host host, Service service, User user, Team team, Module module) throws IOException {
        WorkItem workItem = new WorkItem(Type.host, Operation.enableVips, user, team, service, module, host, vip);
        workItemProcessor.processWorkItem(workItem);
    }

    private void disableHostInVip(Vip vip, Host host, Service service, User user, Team team, Module module) throws IOException {
        WorkItem workItem = new WorkItem(Type.host, Operation.disableVips, user, team, service, module, host, vip);
        workItemProcessor.processWorkItem(workItem);
    }

}
