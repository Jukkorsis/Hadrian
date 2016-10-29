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
import com.northernwall.hadrian.ConfigHelper;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Config;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleType;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import com.northernwall.hadrian.handlers.service.dao.PostModuleData;
import com.northernwall.hadrian.handlers.service.helper.FolderHelper;
import com.northernwall.hadrian.handlers.utility.routingHandler.Http400BadRequestException;
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
public class ModuleCreateHandler extends BasicHandler {
    private final AccessHelper accessHelper;
    private final ConfigHelper configHelper;
    private final WorkItemProcessor workItemProcessor;
    private final FolderHelper folderHelper;

    public ModuleCreateHandler(AccessHelper accessHelper, ConfigHelper configHelper, DataAccess dataAccess, WorkItemProcessor workItemProcessor, FolderHelper folderHelper) {
        super(dataAccess);
        this.accessHelper = accessHelper;
        this.configHelper = configHelper;
        this.workItemProcessor = workItemProcessor;
        this.folderHelper = folderHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostModuleData data = fromJson(request, PostModuleData.class);
        Service service = getService(data.serviceId, null);
        Team team = getTeam(service.getTeamId(), null);
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "add a module");

        Config config = configHelper.getConfig();
        String template = null;
        switch (data.moduleType) {
            case Deployable:
                if (!config.deployableTemplates.contains(data.deployableTemplate)) {
                    throw new Http400BadRequestException("Unknown deployable template");
                }
                template = data.deployableTemplate;
                break;
            case Library:
                if (!config.libraryTemplates.contains(data.libraryTemplate)) {
                    throw new Http400BadRequestException("Unknown library template");
                }
                template = data.libraryTemplate;
                break;
            case Simulator:
                if (!config.deployableTemplates.contains(data.deployableTemplate)) {
                    throw new Http400BadRequestException("Unknown Simulator template");
                }
                template = data.deployableTemplate;
                break;
        }
        if (!config.artifactTypes.contains(data.artifactType)) {
            throw new Http400BadRequestException("Unknown artifact");
        }

        if (service.getServiceType().equals(Const.SERVICE_TYPE_SHARED_LIBRARY)) {
            data.moduleType = ModuleType.Library;
        }

        switch (data.moduleType) {
            case Library:
                data.hostAbbr = "";
                data.versionUrl = "";
                data.availabilityUrl = "";
                data.runAs = "";
                data.deploymentFolder = "";
                data.dataFolder = "";
                data.logsFolder = "";
                data.startCmdLine = "";
                data.startTimeOut = 0;
                data.stopCmdLine = "";
                data.stopTimeOut = 0;
                break;
            case Deployable:
            case Simulator:
                if (data.hostAbbr.contains("-")) {
                    throw new Http400BadRequestException("Can not have '-' in host abbr");
                }  
                
                data.deploymentFolder = folderHelper.scrubFolder(data.deploymentFolder, "Deployment", false);
                data.logsFolder = folderHelper.scrubFolder(data.logsFolder, "Logs", true);
                data.dataFolder = folderHelper.scrubFolder(data.dataFolder, "Data", true);

                folderHelper.isWhiteListed(data.deploymentFolder, "Deployment", data.runAs);
                
                if (data.logsFolder != null
                        && !data.logsFolder.isEmpty()) { 
                    folderHelper.isWhiteListed(data.logsFolder, "Logs", data.runAs);
                    folderHelper.isSubFolder(data.logsFolder, "Logs", data.deploymentFolder, "Deployment");
                }

                if (data.dataFolder != null
                        && !data.dataFolder.isEmpty()) { 
                    folderHelper.isWhiteListed(data.dataFolder, "Data", data.runAs);
                    folderHelper.isSubFolder(data.dataFolder, "Data", data.deploymentFolder, "Deployment");
                }   
                break;
        }

        List<Module> modules = getDataAccess().getModules(data.serviceId);
        if (modules != null && !modules.isEmpty()){
            for (Module temp : modules) {
                if (temp.getGitFolder() == null || temp.getGitFolder().isEmpty()) {
                    throw new Http400BadRequestException("Can not create new module while module " + temp.getModuleName() + " is at the git folder root.");
                }
                if (data.moduleName.equalsIgnoreCase(temp.getModuleName())) {
                    throw new Http400BadRequestException("Error there already exists a module named " + data.moduleName);
                }
                String gitFolder = "/" + data.gitFolder.toUpperCase() + "/";
                String tempGitFolder = "/" + temp.getGitFolder().toUpperCase() + "/";
                if (gitFolder.equals(tempGitFolder)) {
                    throw new Http400BadRequestException("Error there already exists a module in folder " + data.gitFolder);
                }
                if (gitFolder.startsWith(tempGitFolder)) {
                    throw new Http400BadRequestException("A Module's git folder may not be a sub folder of another module");
                }
                if (tempGitFolder.startsWith(gitFolder)) {
                    throw new Http400BadRequestException("A Module's git folder may not be a sub folder of another module");
                }
            }
        }

        if (data.gitFolder == null) {
            data.gitFolder = "";
        }
        if (data.gitFolder.startsWith("/")) {
            data.gitFolder = data.gitFolder.substring(1);
        }
        if (data.gitFolder.endsWith(".")) {
            data.gitFolder = data.gitFolder.substring(0, data.gitFolder.length() - 1);
        }
        if (data.gitFolder.endsWith("/")) {
            data.gitFolder = data.gitFolder.substring(0, data.gitFolder.length() - 1);
        }
        if (data.gitFolder.isEmpty() && modules != null && !modules.isEmpty()) {
            throw new Http400BadRequestException("This module can not be at the git folder root, if there is another module");
        }

        Module module = new Module(
                data.moduleName,
                data.serviceId,
                data.moduleType,
                data.gitFolder,
                data.mavenArtifactId,
                data.artifactType,
                data.artifactSuffix,
                data.outbound,
                data.hostAbbr.toLowerCase(),
                data.versionUrl,
                data.availabilityUrl,
                data.smokeTestUrl,
                data.runAs,
                data.deploymentFolder,
                data.dataFolder,
                data.logsFolder,
                data.logsRetention,
                data.startCmdLine,
                data.startTimeOut,
                data.stopCmdLine,
                data.stopTimeOut,
                data.configName,
                data.environmentNames);
        module.cleanEnvironmentNames(null);
        getDataAccess().saveModule(module);

        WorkItem workItem = new WorkItem(Type.module, Operation.create, user, team, service, module, null, null, null);
        workItem.getMainModule().template = template;
        for (Module temp : modules) {
            workItem.addModule(temp);
        }
        workItemProcessor.processWorkItem(workItem);
        
        response.setStatus(200);
        request.setHandled(true);
    }


}
