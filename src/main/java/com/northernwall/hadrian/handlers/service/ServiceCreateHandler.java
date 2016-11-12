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
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.handlers.service.dao.PostServiceData;
import com.northernwall.hadrian.handlers.utility.routingHandler.Http400BadRequestException;
import com.northernwall.hadrian.handlers.utility.routingHandler.Http405NotAllowedException;
import com.northernwall.hadrian.schedule.ScheduleRunner;
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

    public ServiceCreateHandler(AccessHelper accessHelper, DataAccess dataAccess, WorkItemProcessor workItemProcessor) {
        super(dataAccess);
        this.accessHelper = accessHelper;
        this.workItemProcessor = workItemProcessor;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostServiceData data = fromJson(request, PostServiceData.class);
        if (data.teamId == null || data.teamId.isEmpty()) {
            throw new Http400BadRequestException("teamId attribute is mising");
        }
        User user = accessHelper.checkIfUserCanModify(request, data.teamId, "create a service");
        Team team = getTeam(data.teamId, null);
        
        if (data.serviceName == null || data.serviceName.isEmpty()) {
            throw new Http400BadRequestException("Service Name is mising or empty");
        }
        if (data.serviceName.length() > 30) {
            throw new Http400BadRequestException("Service Name is to long, max is 30");
        }
        if (getDataAccess().getServiceByServiceName(data.serviceName) != null) {
             throw new Http405NotAllowedException("A service already exists with this name");
        }

        if (data.description.length() > 500) {
            throw new Http400BadRequestException("Description is to long, max is 500");
        }

        if (data.doBuilds) {
            if (data.gitProject == null || data.gitProject.isEmpty()) {
                throw new Http400BadRequestException("Git Project is mising or empty");
            }
            if (data.gitProject.length() > 30) {
                throw new Http400BadRequestException("Git Project is to long, max is 30");
            }
            if (getDataAccess().getServiceByGitProject(data.gitProject) != null) {
                 throw new Http405NotAllowedException("A service already exists at this Git Project location");
            }
        } else {
            data.gitProject = null;
        }

        if (data.doDeploys || data.doBuilds) {
            if (data.mavenGroupId == null || data.mavenGroupId.isEmpty()) {
                throw new Http400BadRequestException("Maven Group is mising or empty");
            }
            if (getDataAccess().getServiceByMavenGroup(data.mavenGroupId) != null) {
                 throw new Http405NotAllowedException("A service already exists with this Maven group");
            }
        } else {
            data.mavenGroupId = null;
        }
        
        try {
            if (data.smokeTestCron != null
                    && !data.smokeTestCron.isEmpty()) {
                ScheduleRunner.parseCron(data.smokeTestCron);
            }
        } catch (Exception e) {
            throw new Http400BadRequestException("Illegal cron, " + e.getMessage());
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
                data.smokeTestCron,
                true);

        getDataAccess().saveService(service);
        getDataAccess().insertServiceSearch(service);

        WorkItem workItem = new WorkItem(Type.service, Operation.create, user, team, service, null, null, null, null);
        workItemProcessor.processWorkItem(workItem);
        
        response.setStatus(200);
        request.setHandled(true);

    }

}
