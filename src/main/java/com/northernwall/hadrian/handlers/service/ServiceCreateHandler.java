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
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.handlers.service.dao.PostServiceData;
import com.northernwall.hadrian.handlers.utility.routingHandler.Http400BadRequestException;
import com.northernwall.hadrian.handlers.utility.routingHandler.Http405NotAllowedException;
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
public class ServiceCreateHandler extends BasicHandler {

    private final AccessHelper accessHelper;

    public ServiceCreateHandler(AccessHelper accessHelper, DataAccess dataAccess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostServiceData data = fromJson(request, PostServiceData.class);
        if (data.teamId == null || data.teamId.isEmpty()) {
            throw new Http400BadRequestException("teamId attribute is mising");
        }
        User user = accessHelper.checkIfUserCanModify(request, data.teamId, "create a service");
        if (data.serviceName == null || data.serviceName.isEmpty()) {
            throw new Http400BadRequestException("Service Name is mising or empty");
        }
        if (data.serviceName.length() > 30) {
            throw new Http400BadRequestException("Service Name is to long, max is 30");
        }
        if (data.description.length() > 500) {
            throw new Http400BadRequestException("Description is to long, max is 500");
        }

        for (Service temp : getDataAccess().getActiveServices()) {
            if (temp.getServiceName().equalsIgnoreCase(data.serviceName)) {
                throw new Http405NotAllowedException("A service already exists with this name");
            }
            if (temp.getGitProject().equalsIgnoreCase(data.gitProject)) {
                throw new Http405NotAllowedException("A service already exists at this Git Project location");
            }
            if (temp.getMavenGroupId().equalsIgnoreCase(data.mavenGroupId)) {
                throw new Http405NotAllowedException("A service already exists with this Maven group");
            }
        }

        if (data.gitProject == null || data.gitProject.isEmpty()) {
            throw new Http400BadRequestException("Git Project is mising or empty");
        }
        if (data.gitProject.length() > 30) {
            throw new Http400BadRequestException("Git Project is to long, max is 30");
        }
        
        if (data.testStyle.equals("Maven")) {
            data.testHostname = null;
            data.testRunAs = null;
            data.testDeploymentFolder = null;
            data.testCmdLine = null;
        }
        
        Service service = new Service(
                data.serviceName,
                data.teamId,
                data.description,
                data.serviceType,
                data.gitProject,
                data.scope,
                data.mavenGroupId,
                data.testStyle,
                data.testHostname,
                data.testRunAs,
                data.testDeploymentFolder,
                data.testCmdLine,
                true);

        getDataAccess().saveService(service);

        Map<String, String> notes = new HashMap<>();
        notes.put("Name", service.getServiceName());
        createAudit(service.getServiceId(), user.getUsername(), Type.service, Operation.create, notes);
        response.setStatus(200);
        request.setHandled(true);
    }

    private void createAudit(String serviceId, String requestor, Type type, Operation operation, Map<String, String> notes) {
        Audit audit = new Audit();
        audit.serviceId = serviceId;
        audit.setTimePerformed(GMT.getGmtAsDate());
        audit.timeRequested = GMT.getGmtAsDate();
        audit.requestor = requestor;
        audit.type = type;
        audit.operation = operation;
        audit.successfull = true;
        audit.notes = getGson().toJson(notes);
        getDataAccess().saveAudit(audit, null);
    }

}
