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

import com.northernwall.hadrian.GMT;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Audit;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.service.dao.DeleteServiceRefData;
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
public class ServiceRefDeleteHandler extends BasicHandler {

    private final AccessHelper accessHelper;

    public ServiceRefDeleteHandler(AccessHelper accessHelper, DataAccess dataAccess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        DeleteServiceRefData data = fromJson(request, DeleteServiceRefData.class);
        
        Service clientService = getService(data.clientServiceId, null, null);
        User user = accessHelper.checkIfUserCanModify(request, clientService.getTeamId(), "delete a module ref");

        Module clientModule = getModule(data.clientModuleId, null, clientService);
        Service serverService = getService(data.serverServiceId, null, null);
        Module serverModule = getModule(data.serverModuleId, null, serverService);
        
        getDataAccess().deleteModuleRef(data.clientServiceId, data.clientModuleId, data.serverServiceId, data.serverModuleId);
        
        Map<String, String> notes = new HashMap<>();
        notes.put("uses", serverService.getServiceAbbr() + " " + serverModule.getModuleName());
        createAudit(data.clientServiceId, clientModule.getModuleName(), user.getUsername(), notes);
        notes = new HashMap<>();
        notes.put("use_by", clientService.getServiceAbbr() + " " + clientModule.getModuleName());
        createAudit(data.serverServiceId, serverModule.getModuleName(), user.getUsername(), notes);
        response.setStatus(200);
        request.setHandled(true);
    }

    private void createAudit(String serviceId, String moduleName, String requestor, Map<String, String> notes) {
        Audit audit = new Audit();
        audit.serviceId = serviceId;
        audit.moduleName = moduleName;
        audit.timePerformed = GMT.getGmtAsDate();
        audit.timeRequested = GMT.getGmtAsDate();
        audit.requestor = requestor;
        audit.type = Type.serviceRef;
        audit.operation = Operation.delete;
        audit.successfull = true;
        audit.notes = getGson().toJson(notes);
        getDataAccess().saveAudit(audit, null);
    }

}
