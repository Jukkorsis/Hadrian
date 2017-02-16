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
package com.northernwall.hadrian.handlers.module;

import com.google.gson.Gson;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.db.SearchResult;
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
import com.northernwall.hadrian.handlers.module.dao.PutModuleData;
import com.northernwall.hadrian.handlers.service.helper.FolderHelper;
import com.northernwall.hadrian.handlers.routing.Http400BadRequestException;
import com.northernwall.hadrian.handlers.routing.Http405NotAllowedException;
import com.northernwall.hadrian.schedule.ScheduleRunner;
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

        if (environmentNames.isEmpty()) {
            throw new Http400BadRequestException("At least one environment must be selected");
        }
    }

    public static void checkHostAbbr(String hostAbbr) throws Http400BadRequestException {
        if (hostAbbr == null
                || hostAbbr.isEmpty()) {
            throw new Http400BadRequestException("Host abbr is missing");
        }
        if (!hostAbbr.matches("^[a-zA-Z0-9]+$")) {
            throw new Http400BadRequestException("Host abbr contains an illegal character");
        }
        if (hostAbbr.length() < 3) {
            throw new Http400BadRequestException("Host abbr is to short, minimum is 3");
        }
        if (hostAbbr.length() > 8) {
            throw new Http400BadRequestException("Host abbr is to long, maximum is 8");
        }
    }

    private final AccessHelper accessHelper;
    private final WorkItemProcessor workItemProcessor;
    private final FolderHelper folderHelper;

    public ModuleModifyHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper, WorkItemProcessor workItemProcessor, FolderHelper folderHelper) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
        this.workItemProcessor = workItemProcessor;
        this.folderHelper = folderHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PutModuleData data = fromJson(request, PutModuleData.class);
        Service service = getService(data.serviceId, null);

        Team team = getTeam(service.getTeamId(), null);
        User user = accessHelper.checkIfUserCanModify(request, team, "update module");

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
                data.smokeTestCron = "";
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
                data.smokeTestCron = "";
            case Deployable:
                ModuleModifyHandler.checkHostAbbr(data.hostAbbr);

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

                if (service.getMavenGroupId() != null
                        && !service.getMavenGroupId().isEmpty()
                        && data.mavenArtifactId != null
                        && !data.mavenArtifactId.isEmpty()) {
                    SearchResult searchResult = getDataAccess().doSearch(
                            Const.SEARCH_SPACE_MAVEN_GROUP_ARTIFACT,
                            service.getMavenGroupId() + "." + data.mavenArtifactId);
                    if (searchResult != null
                            && !searchResult.moduleId.equals(data.moduleId)) {
                        throw new Http405NotAllowedException("A service and module already exists with this maven group and artifact");
                    }
                }

                try {
                    if (data.smokeTestCron != null
                            && !data.smokeTestCron.isEmpty()) {
                        ScheduleRunner.parseCron(data.smokeTestCron);
                    }
                } catch (Exception e) {
                    throw new Http400BadRequestException("Illegal cron, " + e.getMessage());
                }

                checkEnvironmentNames(data.environmentNames);
                break;
        }

        if (service.getMavenGroupId() != null
                && !service.getMavenGroupId().isEmpty()
                && module.getMavenArtifactId() != null
                && !module.getMavenArtifactId().isEmpty()) {
            getDataAccess().deleteSearch(
                    Const.SEARCH_SPACE_MAVEN_GROUP_ARTIFACT,
                    service.getMavenGroupId() + "." + module.getMavenArtifactId());
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
        module.setSmokeTestCron(data.smokeTestCron);
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
        if (service.getMavenGroupId() != null
                && !service.getMavenGroupId().isEmpty()
                && module.getMavenArtifactId() != null
                && !module.getMavenArtifactId().isEmpty()) {
            getDataAccess().insertSearch(
                    Const.SEARCH_SPACE_MAVEN_GROUP_ARTIFACT,
                    service.getMavenGroupId() + "." + module.getMavenArtifactId(),
                    service.getServiceId(),
                    module.getModuleId(),
                    null);
        }

        WorkItem workItem = new WorkItem(Type.module, Operation.update, user, team, service, module, null, null);
        for (Module temp : modules) {
            workItem.addModule(temp);
        }
        workItemProcessor.processWorkItem(workItem);

        response.setStatus(200);
        request.setHandled(true);
    }

}
