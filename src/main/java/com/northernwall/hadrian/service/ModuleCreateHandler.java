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

import com.northernwall.hadrian.ConfigHelper;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Config;
import com.northernwall.hadrian.domain.GitMode;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleType;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import com.northernwall.hadrian.service.dao.PostModuleData;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http400BadRequestException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Thurston
 */
public class ModuleCreateHandler extends BasicHandler {

    private final static Logger logger = LoggerFactory.getLogger(ModuleCreateHandler.class);

    private final AccessHelper accessHelper;
    private final ConfigHelper configHelper;
    private final WorkItemProcessor workItemProcess;

    public ModuleCreateHandler(AccessHelper accessHelper, ConfigHelper configHelper, DataAccess dataAccess, WorkItemProcessor workItemProcess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
        this.configHelper = configHelper;
        this.workItemProcess = workItemProcess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostModuleData postModuleData = fromJson(request, PostModuleData.class);
        Service service = getService(postModuleData.serviceId, null, null);
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "add a module");
        Team team = getDataAccess().getTeam(service.getTeamId());

        Config config = configHelper.getConfig();
        if (!config.moduleTypes.contains(postModuleData.moduleType)) {
            throw new Http400BadRequestException("Unknown module type");
        }
        String template = null;
        switch (postModuleData.moduleType) {
            case Deployable:
                if (!config.deployableTemplates.contains(postModuleData.deployableTemplate)) {
                    throw new Http400BadRequestException("Unknown deployable template");
                }
                template = postModuleData.deployableTemplate;
                break;
            case Library:
                if (!config.libraryTemplates.contains(postModuleData.libraryTemplate)) {
                    throw new Http400BadRequestException("Unknown library template");
                }
                template = postModuleData.libraryTemplate;
                break;
            case Test:
                if (!config.testTemplates.contains(postModuleData.testTemplate)) {
                    throw new Http400BadRequestException("Unknown test template");
                }
                template = postModuleData.testTemplate;
                break;
        }
        if (!config.artifactTypes.contains(postModuleData.artifactType)) {
            throw new Http400BadRequestException("Unknown artifact");
        }

        if (service.getServiceType().equals(Const.SERVICE_TYPE_SHARED_LIBRARY)) {
            postModuleData.moduleType = ModuleType.Library;
        }

        if (!postModuleData.moduleType.equals(ModuleType.Deployable)) {
            postModuleData.hostAbbr = "";
            postModuleData.versionUrl = "";
            postModuleData.availabilityUrl = "";
            postModuleData.runAs = "";
            postModuleData.deploymentFolder = "";
            postModuleData.startCmdLine = "";
            postModuleData.startTimeOut = 0;
            postModuleData.stopCmdLine = "";
            postModuleData.stopTimeOut = 0;
        } else {
            if (postModuleData.hostAbbr.contains("-")) {
                throw new Http400BadRequestException("Can not have '-' in host abbr");
            }
        }

        if (service.getGitMode().equals(GitMode.Consolidated)) {
            postModuleData.gitProject = service.getGitProject();
            if (postModuleData.gitFolder.startsWith("/")) {
                postModuleData.gitFolder = postModuleData.gitFolder.substring(1);
            }
        } else {
            postModuleData.gitFolder = "";
        }

        List<Module> modules = getDataAccess().getModules(postModuleData.serviceId);
        List<Module> zeroModules = new LinkedList<>();
        for (Module temp : modules) {
            if (postModuleData.moduleName.equalsIgnoreCase(temp.getModuleName())) {
                logger.warn("Error there already exists a module named {} on service {}", postModuleData.moduleName, postModuleData.serviceId);
                return;
            }
            if (postModuleData.gitProject.equalsIgnoreCase(temp.getGitProject()) && postModuleData.gitFolder.equalsIgnoreCase(temp.getGitFolder())) {
                logger.warn("Error there already exists a module with git project {} and folder {} on service {}", postModuleData.gitProject, postModuleData.gitFolder, postModuleData.serviceId);
                return;
            }
            if (temp.getOrder() == 0) {
                zeroModules.add(temp);
            }
        }
        modules.removeAll(zeroModules);
        Collections.sort(modules);
        if (postModuleData.order < 0) {
            postModuleData.order = 0;
        }
        if (postModuleData.order > 0) {
            if (postModuleData.order > (modules.size() + 1)) {
                postModuleData.order = modules.size() + 1;
            }
            for (Module temp : modules) {
                if (temp.getOrder() >= postModuleData.order) {
                    temp.setOrder(temp.getOrder() + 1);
                    getDataAccess().updateModule(temp);
                }
            }
        }

        Module module = new Module(
                postModuleData.moduleName,
                postModuleData.serviceId,
                postModuleData.order,
                postModuleData.moduleType,
                postModuleData.gitProject,
                postModuleData.gitFolder,
                postModuleData.mavenGroupId,
                postModuleData.mavenArtifactId,
                postModuleData.artifactType,
                postModuleData.artifactSuffix,
                postModuleData.hostAbbr.toLowerCase(),
                postModuleData.versionUrl,
                postModuleData.availabilityUrl,
                postModuleData.runAs,
                postModuleData.deploymentFolder,
                postModuleData.startCmdLine,
                postModuleData.startTimeOut,
                postModuleData.stopCmdLine,
                postModuleData.stopTimeOut);
        getDataAccess().saveModule(module);
        if (module.getOrder() > 0) {
            modules.add(module.getOrder() - 1, module);
        } else {
            zeroModules.add(module);
        }

        WorkItem workItem = new WorkItem(Type.module, Operation.create, user, team, service, module, null, null);
        workItem.getMainModule().template = template;
        for (Module temp : zeroModules) {
            workItem.addModule(temp);
        }
        for (Module temp : modules) {
            workItem.addModule(temp);
        }

        getDataAccess().saveWorkItem(workItem);

        workItemProcess.sendWorkItem(workItem);
        response.setStatus(200);
        request.setHandled(true);
    }

}
