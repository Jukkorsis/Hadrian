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

import com.google.gson.Gson;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.db.SearchSpace;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.handlers.service.dao.PostServiceData;
import com.northernwall.hadrian.handlers.routing.Http400BadRequestException;
import com.northernwall.hadrian.handlers.routing.Http405NotAllowedException;
import com.northernwall.hadrian.handlers.service.dao.GetServiceData;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import java.io.IOException;
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
    private final WorkItemProcessor workItemProcessor;

    public ServiceCreateHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper, WorkItemProcessor workItemProcessor) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
        this.workItemProcessor = workItemProcessor;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostServiceData data = fromJson(request, PostServiceData.class);
        if (data.teamId == null || data.teamId.isEmpty()) {
            throw new Http400BadRequestException("teamId attribute is mising");
        }
        Team team = getTeam(data.teamId, null);
        User user = accessHelper.checkIfUserCanModify(request, team, "create a service");

        if (data.serviceName == null || data.serviceName.isEmpty()) {
            throw new Http400BadRequestException("Service Name is mising or empty");
        }
        if (data.serviceName.length() > 30) {
            throw new Http400BadRequestException("Service Name is to long, max is 30");
        }
        if (getDataAccess().doSearch(SearchSpace.serviceName, data.serviceName) != null) {
            throw new Http405NotAllowedException("A service already exists with this name");
        }

        if (data.description.length() > 500) {
            throw new Http400BadRequestException("Description is to long, max is 500");
        }

        if (data.doBuilds) {
            if (data.gitProject == null
                    || data.gitProject.isEmpty()
                    || data.gitProject.equalsIgnoreCase(".git")) {
                throw new Http400BadRequestException("Git Project is mising or empty");
            }
            if (data.gitProject.toLowerCase().endsWith(".git")) {
                data.gitProject = data.gitProject.substring(0, data.gitProject.length() - 4);
            }
            if (data.gitProject.length() > 30) {
                throw new Http400BadRequestException("Git Project is to long, max is 30");
            }
            if (getDataAccess().doSearch(SearchSpace.gitProject, data.gitProject) != null) {
                throw new Http405NotAllowedException("A service already exists at this Git Project location");
            }
        } else {
            data.gitProject = null;
        }

        if (data.doDeploys || data.doBuilds) {
            if (data.mavenGroupId == null || data.mavenGroupId.isEmpty()) {
                throw new Http400BadRequestException("Maven Group is mising or empty");
            }
        } else {
            data.mavenGroupId = null;
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
                data.haFunctionality,
                data.haPerformance,
                data.haData,
                data.haNotes,
                data.mavenGroupId,
                data.doBuilds,
                data.doDeploys,
                data.doManageVip,
                data.doCheckJar,
                data.doFindBugsLevel,
                data.testStyle,
                data.testHostname,
                data.testRunAs,
                data.testDeploymentFolder,
                data.testCmdLine,
                data.testTimeOut,
                true);

        getDataAccess().saveService(service);
        getDataAccess().insertSearch(
                SearchSpace.serviceName,
                data.serviceName,
                service.getTeamId(),
                service.getServiceId(),
                null,
                null,
                null);
        if (data.gitProject != null
                && !data.gitProject.isEmpty()) {
            getDataAccess().insertSearch(
                    SearchSpace.gitProject,
                    data.gitProject,
                    service.getTeamId(),
                    service.getServiceId(),
                    null,
                    null,
                    null);
        }

        WorkItem workItem = new WorkItem(Type.service, Operation.create, user, team, service, null, null, null);
        workItemProcessor.processWorkItem(workItem);

        GetServiceData getServiceData = GetServiceData.create(service);
        toJson(response, getServiceData);
        response.setStatus(200);
        request.setHandled(true);
    }

}
