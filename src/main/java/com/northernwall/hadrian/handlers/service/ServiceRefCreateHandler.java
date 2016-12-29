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
package com.northernwall.hadrian.handlers.service;

import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.GMT;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Audit;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.ModuleRef;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.handlers.service.dao.PostModuleRefData;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard Thurston
 */
public class ServiceRefCreateHandler extends BasicHandler {

    private final AccessHelper accessHelper;

    public ServiceRefCreateHandler(AccessHelper accessHelper, DataAccess dataAccess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostModuleRefData data = fromJson(request, PostModuleRefData.class);

        Service clientService = getService(data.clientServiceId, null);
        Team team = getTeam(clientService.getTeamId(), null);
        User user = accessHelper.checkIfUserCanModify(request, team, "add a service ref");

        Module clientModule = getModule(data.clientModuleId, null, clientService);
        Service serverService = getService(data.serverServiceId, null);
        Module serverModule = getModule(data.serverModuleId, null, serverService);

        ModuleRef ref = new ModuleRef(data.clientServiceId, data.clientModuleId, data.serverServiceId, data.serverModuleId);
        getDataAccess().saveModuleRef(ref);
        Map<String, String> notes = new HashMap<>();
        notes.put("Uses", serverService.getServiceName() + " " + serverModule.getModuleName());
        createAudit(data.clientServiceId, clientModule.getModuleName(), user.getUsername(), notes);
        notes = new HashMap<>();
        notes.put("Use_By", clientService.getServiceName() + " " + clientModule.getModuleName());
        createAudit(data.serverServiceId, serverModule.getModuleName(), user.getUsername(), notes);

        response.setStatus(200);
        request.setHandled(true);
    }

    private void createAudit(String serviceId, String moduleName, String requestor, Map<String, String> notes) {
        Audit audit = new Audit();
        audit.serviceId = serviceId;
        audit.moduleName = moduleName;
        audit.setTimePerformed(GMT.getGmtAsDate());
        audit.timeRequested = GMT.getGmtAsDate();
        audit.requestor = requestor;
        audit.type = Type.serviceRef;
        audit.operation = Operation.create;
        audit.notes = getGson().toJson(notes);
        getDataAccess().saveAudit(audit, null);
    }

}
