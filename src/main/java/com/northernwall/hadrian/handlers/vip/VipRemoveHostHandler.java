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
import com.northernwall.hadrian.handlers.vip.dao.AddVipHostData;
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
public class VipRemoveHostHandler extends BasicHandler {
    private final AccessHelper accessHelper;
    private final WorkItemProcessor workItemProcessor;
    
    public VipRemoveHostHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper, WorkItemProcessor workItemProcessor) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
        this.workItemProcessor = workItemProcessor;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        AddVipHostData data = fromJson(request, AddVipHostData.class);
        Service service = getService(data.serviceId, null);
        Team team = getTeam(service.getTeamId(), null);
        User user = accessHelper.checkIfUserCanModify(request, team, "remove host from vip");
        
        if (data.hostname == null || data.hostname.isEmpty()) {
            throw new Http400BadRequestException("Hostname is null");
        }
        
        Vip vip = getVip(data.vipId, service);
        if (!vip.getDisabledHosts().contains(data.hostname)) {
            vip.getDisabledHosts().add(data.hostname);
            getDataAccess().saveVip(vip);
        }
        
        Module module = getDataAccess().getModule(service.getServiceId(), vip.getModuleId());
        
        List<Host> hosts = getDataAccess().getHosts(service.getServiceId());
        if (hosts != null && !hosts.isEmpty()) {
            for (Host host : hosts) {
                if (host.getHostName().equals(data.hostname)) {
                    WorkItem workItem = new WorkItem(Type.host, Operation.removeVips, user, team, service, module, host, vip);
                    workItemProcessor.processWorkItem(workItem);
                }
            }
        }

        response.setStatus(200);
        request.setHandled(true);
    }

}
