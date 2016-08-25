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
import com.northernwall.hadrian.domain.ModuleFile;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.service.dao.PostModuleFileData;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http400BadRequestException;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http404NotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

import static com.northernwall.hadrian.service.BasicHandler.getGson;

/**
 *
 * @author Richard Thurston
 */
public class ModuleFileCreateHandler extends BasicHandler {

    private final AccessHelper accessHelper;

    public ModuleFileCreateHandler(AccessHelper accessHelper, DataAccess dataAccess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostModuleFileData data = fromJson(request, PostModuleFileData.class);
        Service service = getService(data.serviceId, null);
        Module module = getModule(data.moduleId, null, service);
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "manage file for module");
        if (data.network == null || data.network.isEmpty()) {
            throw new Http400BadRequestException("attribute network is missing");
        }

        data.name = data.name.trim();
        if (data.name.contains(" ")) {
            throw new Http400BadRequestException("attribute name is illegal");
        }

        if (data.originalName == null || data.originalName.isEmpty()) {
            ModuleFile moduleFile = new ModuleFile(service.getServiceId(), module.getModuleId(), data.network, data.name, data.contents);
            getDataAccess().saveModuleFile(moduleFile);
            createAudit(service.getServiceId(), module.getModuleName(), user.getUsername(), "Created file " + data.name + " on " + data.network);
        } else {
            ModuleFile moduleFile = getDataAccess().getModuleFile(
                service.getServiceId(),
                module.getModuleId(),
                data.network,
                data.originalName);
            if (moduleFile == null) {
                throw new Http404NotFoundException("Could not find existing module file");
            }
            if (data.name.equalsIgnoreCase(data.originalName)) {
                if (!data.contents.equals(moduleFile.getContents())) {
                    moduleFile.setContents(data.contents);
                    getDataAccess().updateModuleFile(moduleFile);
                    createAudit(service.getServiceId(), module.getModuleName(), user.getUsername(), "Updated file " + data.name + " on " + data.network);
                }
            } else {
                getDataAccess().deleteModuleFile(service.getServiceId(), module.getModuleId(), data.network, data.originalName);
                moduleFile = new ModuleFile(service.getServiceId(), module.getModuleId(), data.network, data.name, data.contents);
                getDataAccess().saveModuleFile(moduleFile);
                createAudit(service.getServiceId(), module.getModuleName(), user.getUsername(), "Rename file " + data.originalName + " to " + data.name + " on " + data.network);
            }
        }

        response.setStatus(200);
        request.setHandled(true);
    }

    private void createAudit(String serviceId, String moduleName, String requestor, String action) {
        Map<String, String> notes = new HashMap<>();
        notes.put("action", action);
        Audit audit = new Audit();
        audit.serviceId = serviceId;
        audit.moduleName = moduleName;
        audit.timePerformed = GMT.getGmtAsDate();
        audit.timeRequested = GMT.getGmtAsDate();
        audit.requestor = requestor;
        audit.type = Type.module;
        audit.operation = Operation.update;
        audit.successfull = true;
        audit.notes = getGson().toJson(notes);
        getDataAccess().saveAudit(audit, null);
    }

}
