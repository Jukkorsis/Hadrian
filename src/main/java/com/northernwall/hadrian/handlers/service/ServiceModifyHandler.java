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
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.handlers.service.dao.PutServiceData;
import com.northernwall.hadrian.handlers.utility.routingHandler.Http405NotAllowedException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard Thurston
 */
public class ServiceModifyHandler extends BasicHandler {

    private final AccessHelper accessHelper;

    public ServiceModifyHandler(AccessHelper accessHelper, DataAccess dataAccess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PutServiceData data = fromJson(request, PutServiceData.class);
        Service service = getService(data.serviceId, null);
        
        accessHelper.checkIfUserCanModify(request, service.getTeamId(), "modify a service");

        for (Service temp : getDataAccess().getActiveServices()) {
            if (temp.getServiceName().equalsIgnoreCase(data.serviceName)
                    && !temp.getServiceId().equals(data.serviceId)) {
                throw new Http405NotAllowedException("A service already exists with this name");
            }
        }

        Service tempService = getDataAccess().getServiceByServiceName(data.serviceName);
        if (tempService != null && !tempService.getServiceId().equals(data.serviceId)) {
             throw new Http405NotAllowedException("A service already exists with this name");
        }

        if (data.testStyle.equals("Maven")) {
            data.testHostname = null;
            data.testRunAs = null;
            data.testDeploymentFolder = null;
            data.testCmdLine = null;
        }
        
        service.setServiceName(data.serviceName);
        service.setDescription(data.description);
        service.setScope(data.scope);
        service.setTestStyle(data.testStyle);
        service.setTestHostname(data.testHostname);
        service.setTestRunAs(data.testRunAs);
        service.setTestDeploymentFolder(data.testDeploymentFolder);
        service.setTestCmdLine(data.testCmdLine);
        service.setTestTimeOut(data.testTimeOut);

        getDataAccess().updateService(service);
        
        response.setStatus(200);
        request.setHandled(true);
    }

}
