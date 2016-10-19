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

    public ModuleCreateHandler(AccessHelper accessHelper, ConfigHelper configHelper, DataAccess dataAccess, WorkItemProcessor workItemProcessor) {
        super(dataAccess);
        this.accessHelper = accessHelper;
        this.configHelper = configHelper;
        this.workItemProcessor = workItemProcessor;
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
                data.deploymentFolder = scrubFolder(data.deploymentFolder, "deploy", false);
                data.logsFolder = scrubFolder(data.logsFolder, "logs", false);
                if (isSubFolder(data.logsFolder, data.deploymentFolder)) {
                    throw new Http400BadRequestException("Log folder can not be a sub folder of the deployment folder");
                }  
                data.dataFolder = scrubFolder(data.dataFolder, "data", true);
                if (data.dataFolder != null && isSubFolder(data.dataFolder, data.deploymentFolder)) {
                    throw new Http400BadRequestException("Data folder can not be a sub folder of the deployment folder");
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
                data.networkNames);
        module.cleanNetworkNames(null);
        getDataAccess().saveModule(module);

        WorkItem workItem = new WorkItem(Type.module, Operation.create, user, team, service, module, null, null);
        workItem.getMainModule().template = template;
        for (Module temp : modules) {
            workItem.addModule(temp);
        }

        workItemProcessor.processWorkItem(workItem);
        response.setStatus(200);
        request.setHandled(true);
    }

    public static String scrubFolder(String folder, String name, boolean nullAllowed) {
        if (folder == null || folder.isEmpty()) {
            if (nullAllowed) {
                return null;
            } else {
                throw new Http400BadRequestException("Folder " + name + " can not be null or empty");
            }
        }
        String temp = folder.trim();
        if (temp == null || temp.isEmpty()) {
           if (nullAllowed) {
                return null;
            } else {
                throw new Http400BadRequestException("Folder " + name + " can not be null or empty");
            }
        }
        if (temp.equals("/")) {
            throw new Http400BadRequestException("Folder " + name + " can not be root");
        }
        if (!folder.startsWith("/")) {
            temp = "/" + temp;
        }
        if (temp.endsWith("/") && temp.length() > 1) {
            temp = temp.substring(0, temp.length()-1);
        }
        return temp;
    }

    /**
     * This method assumes that both folder parameters have already been scrubbed.
     * @param subFolder The sub folder.
     * @param mainFolder The main folder.
     * @return True if the sub folder is within the main folder.
     */
    public static boolean isSubFolder(String subFolder, String mainFolder) {
        String tempSubFolder = subFolder;
        if (tempSubFolder.length() > 1) {
            tempSubFolder = tempSubFolder + "/";
        }
        String tempMainFolder = mainFolder;
        if (tempMainFolder.length() > 1) {
            tempMainFolder = tempMainFolder + "/";
        }
        return (tempSubFolder.equals(tempMainFolder) || tempSubFolder.startsWith(tempMainFolder));
    }

}
