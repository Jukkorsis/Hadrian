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

import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.details.ServiceBuildHelper;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.handlers.service.dao.BuildServiceData;
import com.northernwall.hadrian.handlers.utility.routingHandler.Http400BadRequestException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard Thurston
 */
public class ServiceBuildHandler extends BasicHandler {

    private final AccessHelper accessHelper;
    private final ServiceBuildHelper serviceBuildHelper;

    public ServiceBuildHandler(AccessHelper accessHelper, DataAccess dataAccess, ServiceBuildHelper serviceBuildHelper) {
        super(dataAccess);
        this.accessHelper = accessHelper;
        this.serviceBuildHelper = serviceBuildHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        BuildServiceData data = fromJson(request, BuildServiceData.class);
        Service service = getService(data.serviceId, null);
        Team team = getDataAccess().getTeam(service.getTeamId());
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "trigger service build");
        
        if (!service.isDoBuilds()) {
            throw new Http400BadRequestException("Service not configured to do builds");
        }

        serviceBuildHelper.triggerBuild(team, service, data.branch, user);

        response.setStatus(200);
        request.setHandled(true);
    }

}
