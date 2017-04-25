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
import com.northernwall.hadrian.config.Const;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.db.SearchResult;
import com.northernwall.hadrian.db.SearchSpace;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.handlers.service.dao.PutServiceData;
import com.northernwall.hadrian.handlers.routing.Http400BadRequestException;
import com.northernwall.hadrian.handlers.routing.Http405NotAllowedException;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import java.io.IOException;
import java.util.List;
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
    private final WorkItemProcessor workItemProcessor;

    public ServiceModifyHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper, WorkItemProcessor workItemProcessor) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
        this.workItemProcessor = workItemProcessor;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PutServiceData data = fromJson(request, PutServiceData.class);
        if (data.serviceId == null) {
            throw new Http400BadRequestException("Illegal call, missing serviceId");
        }
        Service service = getService(data.serviceId, null);

        Team team = getTeam(service.getTeamId(), null);
        User user = accessHelper.checkIfUserCanModify(request, team, "modify a service");

        for (Service temp : getDataAccess().getActiveServices()) {
            if (temp.getServiceName().equalsIgnoreCase(data.serviceName)
                    && !temp.getServiceId().equals(data.serviceId)) {
                throw new Http405NotAllowedException("A service already exists with this name");
            }
        }

        SearchResult searchResult = getDataAccess().doSearch(
                SearchSpace.serviceName,
                data.serviceName);
        if (searchResult != null && !searchResult.serviceId.equals(data.serviceId)) {
            throw new Http405NotAllowedException("A service already exists with this name");
        }

        if (data.doBuilds) {
            if (data.gitProject == null || data.gitProject.isEmpty()) {
                throw new Http400BadRequestException("Git Project is mising or empty");
            }
            if (data.gitProject.length() > 30) {
                throw new Http400BadRequestException("Git Project is to long, max is 30");
            }
            searchResult = getDataAccess().doSearch(
                    SearchSpace.gitProject,
                    data.gitProject);
            if (searchResult != null && !searchResult.serviceId.equals(data.serviceId)) {
                throw new Http405NotAllowedException("A service already exists at this Git Project location");
            }
        } else {
            data.gitProject = null;
        }

        List<Module> modules = getDataAccess().getModules(data.serviceId);
        if (data.doDeploys || data.doBuilds) {
            if (data.mavenGroupId == null || data.mavenGroupId.isEmpty()) {
                throw new Http400BadRequestException("Maven Group is mising or empty");
            }
            if (modules != null && !modules.isEmpty()) {
                for (Module module : modules) {
                    if (module.getMavenArtifactId() != null
                            && !module.getMavenArtifactId().isEmpty()) {
                        searchResult = getDataAccess().doSearch(
                                SearchSpace.mavenGroupArtifact,
                                data.mavenGroupId + "." + module.getMavenArtifactId());
                        if (searchResult != null
                                && !searchResult.moduleId.equals(module.getModuleId())) {
                            throw new Http405NotAllowedException(
                                    "A service and module already exists with this Maven group '"
                                    + data.mavenGroupId
                                    + "' and artifact '"
                                    + module.getMavenArtifactId()
                                    + "'");
                        }
                    }
                }
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

        getDataAccess().deleteSearch(
                SearchSpace.serviceName,
                service.getServiceName());
        if (service.getGitProject() != null
                && !service.getGitProject().isEmpty()) {
            getDataAccess().deleteSearch(
                    SearchSpace.gitProject,
                    service.getGitProject());
        }

        //TODO: improve by not doing delete and insert if maven group is not changing
        if (service.getMavenGroupId() != null
                && !service.getMavenGroupId().isEmpty()
                && modules != null
                && !modules.isEmpty()) {
            for (Module module : modules) {
                if (module.getMavenArtifactId() != null
                        && !module.getMavenArtifactId().isEmpty()) {
                    getDataAccess().deleteSearch(
                            SearchSpace.mavenGroupArtifact,
                            service.getMavenGroupId() + "." + module.getMavenArtifactId());
                }
            }
        }

        service.setServiceName(data.serviceName);
        service.setDescription(data.description);
        service.setScope(data.scope);
        service.setHaFunctionality(data.haFunctionality);
        service.setHaPerformance(data.haPerformance);
        service.setHaData(data.haData);
        service.setHaNotes(data.haNotes);

        service.setDoBuilds(data.doBuilds);
        service.setDoDeploys(data.doDeploys);
        service.setDoManageVip(data.doManageVip);
        service.setDoCheckJar(data.doCheckJar);
        service.setDoFindBugsLevel(data.doFindBugsLevel);

        service.setGitProject(data.gitProject);
        service.setMavenGroupId(data.mavenGroupId);

        service.setTestStyle(data.testStyle);
        service.setTestHostname(data.testHostname);
        service.setTestRunAs(data.testRunAs);
        service.setTestDeploymentFolder(data.testDeploymentFolder);
        service.setTestCmdLine(data.testCmdLine);
        service.setTestTimeOut(data.testTimeOut);

        getDataAccess().updateService(service);
        getDataAccess().insertSearch(
                SearchSpace.serviceName,
                data.serviceName,
                service.getTeamId(),
                service.getServiceId(),
                null,
                null,
                null);
        if (service.getGitProject() != null
                && !service.getGitProject().isEmpty()) {
            getDataAccess().insertSearch(
                    SearchSpace.gitProject,
                    data.gitProject,
                    service.getTeamId(),
                    service.getServiceId(),
                    null,
                    null,
                    null);
        }
        if (service.getMavenGroupId() != null
                && !service.getMavenGroupId().isEmpty()
                && modules != null
                && !modules.isEmpty()) {
            for (Module module : modules) {
                if (module.getMavenArtifactId() != null
                        && !module.getMavenArtifactId().isEmpty()) {
                    getDataAccess().insertSearch(
                            SearchSpace.mavenGroupArtifact,
                            service.getMavenGroupId() + "." + module.getMavenArtifactId(),
                            service.getTeamId(),
                            service.getServiceId(),
                            module.getModuleId(),
                            null,
                            null);
                }
            }
        }

        WorkItem workItem = new WorkItem(Type.service, Operation.update, user, team, service, null, null, null);
        for (Module temp : modules) {
            workItem.addModule(temp);
        }
        workItemProcessor.processWorkItem(workItem);

        response.setStatus(200);
        request.setHandled(true);
    }

}
