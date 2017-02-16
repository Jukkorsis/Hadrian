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
package com.northernwall.hadrian.handlers.service;

import com.google.gson.Gson;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.handlers.routing.Http400BadRequestException;
import com.northernwall.hadrian.handlers.service.dao.TransferServiceData;
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
public class ServiceTransferHandler extends BasicHandler {

    private final AccessHelper accessHelper;
    private final WorkItemProcessor workItemProcessor;

    public ServiceTransferHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper, WorkItemProcessor workItemProcessor) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
        this.workItemProcessor = workItemProcessor;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        TransferServiceData data = fromJson(request, TransferServiceData.class);
        if (data.serviceId == null 
                || data.serviceId.isEmpty()) {
            throw new Http400BadRequestException("Missing serviceId");
        }
        if (data.teamId == null 
                || data.teamId.isEmpty()) {
            throw new Http400BadRequestException("Missing teamId");
        }
        if (data.reason == null 
                || data.reason.isEmpty()) {
            throw new Http400BadRequestException("Reason is missing");
        }
        Service service = getService(data.serviceId, null);

        Team team = getTeam(service.getTeamId(), null);
        User user = accessHelper.checkIfUserCanModify(request, team, "transfer a service");

        Team newTeam = getTeam(data.teamId, null);
        if (newTeam == null) {
            throw new Http400BadRequestException("Could not find Team");
        }
        
        service.setTeamId(data.teamId);

        getDataAccess().updateService(service);

        WorkItem workItem = new WorkItem(Type.service, Operation.transfer, user, team, service, null, null, null);
        workItem.setReason(data.reason);
        List<Module> modules = getDataAccess().getModules(data.serviceId);
        for (Module temp : modules) {
            workItem.addModule(temp);
        }
        workItemProcessor.processWorkItem(workItem);

        response.setStatus(200);
        request.setHandled(true);
    }

}
