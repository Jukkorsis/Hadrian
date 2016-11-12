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
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import com.northernwall.hadrian.handlers.service.dao.PutModuleData;
import com.northernwall.hadrian.handlers.service.helper.FolderHelper;
import com.northernwall.hadrian.handlers.utility.routingHandler.Http400BadRequestException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
public class ModuleModifyHandler extends BasicHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(ModuleModifyHandler.class);

    private final AccessHelper accessHelper;
    private final WorkItemProcessor workItemProcessor;
    private final FolderHelper folderHelper;

    public ModuleModifyHandler(AccessHelper accessHelper, DataAccess dataAccess, WorkItemProcessor workItemProcessor, FolderHelper folderHelper) {
        super(dataAccess);
        this.accessHelper = accessHelper;
        this.workItemProcessor = workItemProcessor;
        this.folderHelper = folderHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PutModuleData data = fromJson(request, PutModuleData.class);
        Service service = getService(data.serviceId, null);

        Team team = getTeam(service.getTeamId(), null);
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "update module");

        List<Host> hosts = getDataAccess().getHosts(data.serviceId);
        for (Host host : hosts) {
            if (host.getModuleId().equals(data.moduleId)) {
                Boolean temp = data.environmentNames.get(host.getEnvironment());
                if (temp == null || !temp.booleanValue()) {
                    throw new Http400BadRequestException("Can not remove a environment from a module with an active host");
                }
            }
        }

        List<Vip> vips = getDataAccess().getVips(data.serviceId);
        for (Vip vip : vips) {
            if (vip.getModuleId().equals(data.moduleId)) {
                Boolean temp = data.environmentNames.get(vip.getEnvironment());
                if (temp == null || !temp.booleanValue()) {
                    throw new Http400BadRequestException("Can not remove a environment from a module with an active VIP");
                }
            }
        }

        List<Module> modules = getDataAccess().getModules(data.serviceId);
        Module module = null;
        for (Module temp : modules) {
            if (temp.getModuleId().equals(data.moduleId)) {
                module = temp;
            }
        }
        if (module == null) {
            LOGGER.warn("Could not find module with id {} in service {}", data.moduleId, data.serviceId);
            return;
        }

        switch (module.getModuleType()) {
            case Library:
                data.outbound = "No";
                data.hostAbbr = "";
                data.versionUrl = "";
                data.availabilityUrl = "";
                data.smokeTestUrl = "";
                data.runAs = "";
                data.deploymentFolder = "";
                data.dataFolder = "";
                data.logsFolder = "";
                data.startCmdLine = "";
                data.startTimeOut = 0;
                data.stopCmdLine = "";
                data.stopTimeOut = 0;
                break;
            case Simulator:
                data.outbound = "No";
                data.smokeTestUrl = "";
            case Deployable:
                if (data.hostAbbr.contains("-")) {
                    throw new Http400BadRequestException("Can not have '-' in host abbr");
                }

                if (service.isDoDeploys()) {
                    data.deploymentFolder = folderHelper.scrubFolder(data.deploymentFolder, "Deployment", false);
                    data.logsFolder = folderHelper.scrubFolder(data.logsFolder, "Logs", false);
                    data.dataFolder = folderHelper.scrubFolder(data.dataFolder, "Data", true);

                    folderHelper.isWhiteListed(data.deploymentFolder, "Deployment", data.runAs);

                    folderHelper.isWhiteListed(data.logsFolder, "Logs", data.runAs);
                    folderHelper.isSubFolder(data.logsFolder, "Logs", data.deploymentFolder, "Deployment");

                    if (data.dataFolder != null
                            && !data.dataFolder.isEmpty()) {
                        folderHelper.isWhiteListed(data.dataFolder, "Data", data.runAs);
                        folderHelper.isSubFolder(data.dataFolder, "Data", data.deploymentFolder, "Deployment");
                    }
                } else {
                    data.deploymentFolder = null;
                    data.logsFolder = null;
                    data.dataFolder = null;
                }

                checkEnvironmentNames(data.environmentNames);
                break;
        }

        module.setModuleName(data.moduleName);
        module.setGitFolder(data.gitFolder);
        module.setMavenArtifactId(data.mavenArtifactId);
        module.setArtifactType(data.artifactType);
        module.setArtifactSuffix(data.artifactSuffix);
        module.setOutbound(data.outbound);
        module.setHostAbbr(data.hostAbbr.toLowerCase());
        module.setVersionUrl(data.versionUrl);
        module.setAvailabilityUrl(data.availabilityUrl);
        module.setSmokeTestUrl(data.smokeTestUrl);
        module.setRunAs(data.runAs);
        module.setDeploymentFolder(data.deploymentFolder);
        module.setDataFolder(data.dataFolder);
        module.setLogsFolder(data.logsFolder);
        module.setLogsRetention(data.logsRetention);
        module.setStartCmdLine(data.startCmdLine);
        module.setStartTimeOut(data.startTimeOut);
        module.setStopCmdLine(data.stopCmdLine);
        module.setStopTimeOut(data.stopTimeOut);
        module.setConfigName(data.configName);
        module.setEnvironmentNames(data.environmentNames);
        module.cleanEnvironmentNames(null);

        getDataAccess().saveModule(module);

        WorkItem workItem = new WorkItem(Type.module, Operation.update, user, team, service, module, null, null, null);
        for (Module temp : modules) {
            workItem.addModule(temp);
        }
        workItemProcessor.processWorkItem(workItem);

        response.setStatus(200);
        request.setHandled(true);
    }

    public static void checkEnvironmentNames(Map<String, Boolean> environmentNames) throws Http400BadRequestException {
        if (environmentNames == null || environmentNames.isEmpty()) {
            throw new Http400BadRequestException("At least one environment must be selected");
        }
        
        List<String> falseKeys = null;
        for (Map.Entry<String, Boolean> entry : environmentNames.entrySet()) {
            if (!entry.getValue()) {
                if (falseKeys == null) {
                    falseKeys = new LinkedList<>();
                }
                falseKeys.add(entry.getKey());
            }
        }
        
        if (falseKeys != null) {
            for (String key : falseKeys) {
                environmentNames.remove(key);
            }
        }
        
        if (environmentNames == null || environmentNames.isEmpty()) {
            throw new Http400BadRequestException("At least one environment must be selected");
        }
    }

}
