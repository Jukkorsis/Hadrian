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
import com.northernwall.hadrian.handlers.service.dao.PostModuleData;
import com.northernwall.hadrian.handlers.utility.routingHandler.Http400BadRequestException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
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
            case Test:
                if (!config.testTemplates.contains(data.testTemplate)) {
                    throw new Http400BadRequestException("Unknown test template");
                }
                template = data.testTemplate;
                break;
        }
        if (!config.artifactTypes.contains(data.artifactType)) {
            throw new Http400BadRequestException("Unknown artifact");
        }

        if (service.getServiceType().equals(Const.SERVICE_TYPE_SHARED_LIBRARY)) {
            data.moduleType = ModuleType.Library;
        }

        if (data.moduleType.equals(ModuleType.Library)) {
            data.hostAbbr = "";
            data.hostname = "";
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
        } else if (data.moduleType.equals(ModuleType.Test)) {
            data.hostAbbr = "";
            data.mavenGroupId = "";
            data.mavenArtifactId = "";
            data.artifactSuffix = "";
            data.versionUrl = "";
            data.availabilityUrl = "";
            data.deploymentFolder = scrubFolder(data.deploymentFolder, "deploy", false);
            data.dataFolder = "";
            data.logsFolder = "";
            data.stopCmdLine = "";
            data.stopTimeOut = 0;
            if (data.hostname == null || data.hostname.isEmpty()) {
                throw new Http400BadRequestException("Can not have an empty hostname");
            }
        } else {
            data.hostname = "";
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
        }

        if (service.getGitMode().equals(GitMode.Consolidated)) {
            data.gitProject = service.getGitProject();
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
        } else {
            data.gitFolder = "";
        }

        List<Module> modules = getDataAccess().getModules(data.serviceId);
        List<Module> zeroModules = new LinkedList<>();
        for (Module temp : modules) {
            if (data.moduleName.equalsIgnoreCase(temp.getModuleName())) {
                throw new Http400BadRequestException("Error there already exists a module named " + data.moduleName + " on service " + data.serviceId);
            }
            if (data.gitProject.equalsIgnoreCase(temp.getGitProject())) {
                String gitFolder = "/" + data.gitFolder.toUpperCase() + "/";
                String tempGitFolder = "/" + temp.getGitFolder().toUpperCase() + "/";
                if (gitFolder.equals(tempGitFolder)) {
                    throw new Http400BadRequestException("Error there already exists a module with git project " + data.gitProject + " and folder " + data.gitFolder + " on service " + data.serviceId);
                }
                if (gitFolder.startsWith(tempGitFolder)) {
                    throw new Http400BadRequestException("A Module's git folder may not be a sub folder of another module");
                }
                if (tempGitFolder.startsWith(gitFolder)) {
                    throw new Http400BadRequestException("A Module's git folder may not be a sub folder of another module");
                }
            }
            if (temp.getOrder() == 0) {
                zeroModules.add(temp);
            }
        }
        modules.removeAll(zeroModules);
        Collections.sort(modules);
        if (data.order < 0) {
            data.order = 0;
        }
        if (data.order > 0) {
            if (data.order > (modules.size() + 1)) {
                data.order = modules.size() + 1;
            }
            for (Module temp : modules) {
                if (temp.getOrder() >= data.order) {
                    temp.setOrder(temp.getOrder() + 1);
                    getDataAccess().updateModule(temp);
                }
            }
        }

        Module module = new Module(
                data.moduleName,
                data.serviceId,
                data.order,
                data.moduleType,
                data.gitProject,
                data.gitFolder,
                data.mavenGroupId,
                data.mavenArtifactId,
                data.artifactType,
                data.artifactSuffix,
                data.hostAbbr.toLowerCase(),
                data.hostname,
                data.versionUrl,
                data.availabilityUrl,
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
     * This metho assumes that both folder parameters have already been scrubbed.
     * @param subFolder
     * @param mainFolder
     * @return 
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
