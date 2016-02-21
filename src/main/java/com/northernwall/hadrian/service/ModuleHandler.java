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

import com.google.gson.Gson;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.Util;
import com.northernwall.hadrian.access.AccessException;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Config;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import com.northernwall.hadrian.service.dao.PostModuleData;
import com.northernwall.hadrian.service.dao.PutModuleData;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Thurston
 */
public class ModuleHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(ModuleHandler.class);

    private final AccessHelper accessHelper;
    private final Config config;
    private final DataAccess dataAccess;
    private final WorkItemProcessor workItemProcess;
    private final Gson gson;

    public ModuleHandler(AccessHelper accessHelper, Config config, DataAccess dataAccess, WorkItemProcessor workItemProcess) {
        this.accessHelper = accessHelper;
        this.config = config;
        this.dataAccess = dataAccess;
        this.workItemProcess = workItemProcess;
        gson = new Gson();
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.startsWith("/v1/module")) {
                switch (request.getMethod()) {
                    case "POST":
                        if (target.equalsIgnoreCase("/v1/module")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            createModule(request);
                        } else {
                            throw new RuntimeException("Unknown module operation");
                        }
                        break;
                    case "PUT":
                        if (target.matches("/v1/module/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            String serviceId = target.substring(11, 47);
                            String moduleId = target.substring(48);
                            updateModule(request, serviceId, moduleId);
                        } else {
                            throw new RuntimeException("Unknown module operation");
                        }
                        break;
                    case "DELETE":
                        if (target.matches("/v1/module/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            String serviceId = target.substring(11, 47);
                            String moduleId = target.substring(48);
                            deleteModule(request, serviceId, moduleId);
                        } else {
                            throw new RuntimeException("Unknown module operation");
                        }
                        break;
                    default:
                        throw new RuntimeException("Unknown host operation");
                }
                response.setStatus(200);
                request.setHandled(true);
            }
        } catch (AccessException e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target);
            response.setStatus(401);
            request.setHandled(true);
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
            request.setHandled(true);
        }
    }

    private void createModule(Request request) throws IOException {
        PostModuleData postModuleData = Util.fromJson(request, PostModuleData.class);
        Service service = dataAccess.getService(postModuleData.serviceId);
        if (service == null) {
            throw new RuntimeException("Could not find service");
        }
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "add a module");
        Team team = dataAccess.getTeam(service.getTeamId());

        if (!config.moduleTypes.contains(postModuleData.moduleType)) {
            throw new RuntimeException("Unknown module type");
        }
        if (!config.templates.contains(postModuleData.template)) {
            throw new RuntimeException("Unknown template");
        }
        if (!config.artifactTypes.contains(postModuleData.artifactType)) {
            throw new RuntimeException("Unknown artifact");
        }

        if (service.getServiceType().equals(Const.SERVICE_TYPE_SHARED_LIBRARY)) {
            postModuleData.moduleType = Const.MODULE_TYPE_LIBRARY;
        }
        
        if (!postModuleData.moduleType.equals(Const.MODULE_TYPE_DEPLOYABLE)) {
                postModuleData.hostAbbr = "";
                postModuleData.versionUrl = "";
                postModuleData.availabilityUrl = "";
                postModuleData.runAs = "";
                postModuleData.startCmdLine = "";
                postModuleData.startTimeOut = 0;
                postModuleData.stopCmdLine = "";
                postModuleData.stopTimeOut = 0;
        } else {
            if (postModuleData.hostAbbr.contains("-")) {
                throw new RuntimeException("Can not have '-' in host abbr");
            }
        }
        
        if (service.getGitMode().equals(Const.GIT_MODE_CONSOLIDATED)) {
            postModuleData.gitPath = service.getGitPath();
        } else {
            postModuleData.gitFolder = "";
        }
        
        List<Module> modules = dataAccess.getModules(postModuleData.serviceId);
        Collections.sort(modules);
        if (postModuleData.order < 1) {
            postModuleData.order = 1;
        }
        if (postModuleData.order > (modules.size() + 1)) {
            postModuleData.order = modules.size() + 1;
        }
        for (Module temp : modules) {
            if (postModuleData.moduleName.equalsIgnoreCase(temp.getModuleName())) {
                logger.warn("Error there already exists a module named {} on service {}", postModuleData.moduleName, postModuleData.serviceId);
                return;
            }
            if (postModuleData.gitPath.equalsIgnoreCase(temp.getGitPath()) && postModuleData.gitFolder.equalsIgnoreCase(temp.getGitFolder())) {
                logger.warn("Error there already exists a module with git path {} and folder {} on service {}", postModuleData.gitPath, postModuleData.gitFolder, postModuleData.serviceId);
                return;
            }
        }
        for (Module temp : modules) {
            if (temp.getOrder() >= postModuleData.order) {
                temp.setOrder(temp.getOrder() + 1);
                dataAccess.updateModule(temp);
            }
        }

        Module module = new Module(
                postModuleData.moduleName,
                postModuleData.serviceId,
                postModuleData.order,
                postModuleData.moduleType,
                postModuleData.gitPath,
                postModuleData.gitFolder,
                postModuleData.mavenGroupId,
                postModuleData.mavenArtifactId,
                postModuleData.artifactType,
                postModuleData.artifactSuffix,
                postModuleData.hostAbbr,
                postModuleData.versionUrl,
                postModuleData.availabilityUrl,
                postModuleData.runAs,
                postModuleData.startCmdLine,
                postModuleData.startTimeOut,
                postModuleData.stopCmdLine,
                postModuleData.stopTimeOut);
        dataAccess.saveModule(module);
        modules.add(module.getOrder()-1, module);

        WorkItem workItem = new WorkItem(Const.TYPE_MODULE, Const.OPERATION_CREATE, user, team, service, module, null, null, null);
        workItem.getMainModule().template = postModuleData.template;
        for (Module temp : modules) {
            workItem.addModule(temp);
        }

        dataAccess.saveWorkItem(workItem);

        workItemProcess.sendWorkItem(workItem);
    }

    private void updateModule(Request request, String serviceId, String moduleId) throws IOException {
        Service service = dataAccess.getService(serviceId);
        if (service == null) {
            throw new RuntimeException("Could not find service");
        }
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "update module");
        Team team = dataAccess.getTeam(service.getTeamId());
        
        PutModuleData putModuleData = Util.fromJson(request, PutModuleData.class);
        List<Module> modules = dataAccess.getModules(serviceId);
        Collections.sort(modules);
        if (putModuleData.order < 1) {
            putModuleData.order = 1;
        }
        if (putModuleData.order > modules.size()) {
            putModuleData.order = modules.size();
        }
        Module module = null;
        for (Module temp : modules) {
            if (temp.getModuleId().equals(moduleId)) {
                module = temp;
            }
        }
        if (module == null) {
            logger.warn("Could not find module with id {} in service {}", moduleId, serviceId);
            return;
        }
        
        module.setModuleName(putModuleData.moduleName);
        module.setMavenGroupId(putModuleData.mavenGroupId);
        module.setMavenArtifactId(putModuleData.mavenArtifactId);
        module.setArtifactType(putModuleData.artifactType);
        module.setArtifactSuffix(putModuleData.artifactSuffix);
        module.setHostAbbr(putModuleData.hostAbbr);
        module.setVersionUrl(putModuleData.versionUrl);
        module.setAvailabilityUrl(putModuleData.availabilityUrl);
        module.setRunAs(putModuleData.runAs);
        module.setStartCmdLine(putModuleData.startCmdLine);
        module.setStartTimeOut(putModuleData.startTimeOut);
        module.setStopCmdLine(putModuleData.stopCmdLine);
        module.setStopTimeOut(putModuleData.stopTimeOut);
        
        if (module.getOrder() != putModuleData.order) {
            modules.remove(module);
            module.setOrder(putModuleData.order);
            modules.add(putModuleData.order - 1, module);
            int i = 1;
            for (Module temp : modules) {
                if (temp.getOrder() != i) {
                    temp.setOrder(i);
                    dataAccess.saveModule(temp);
                }
                i++;
            }
        }
        dataAccess.saveModule(module);
        
        WorkItem workItem = new WorkItem(Const.TYPE_MODULE, Const.OPERATION_UPDATE, user, team, service, module, null, null, null);
        for (Module temp : modules) {
            workItem.addModule(temp);
        }
        dataAccess.saveWorkItem(workItem);
        workItemProcess.sendWorkItem(workItem);
    }

    private void deleteModule(Request request, String serviceId, String moduleId) throws IOException {
        Module module = dataAccess.getModule(serviceId, moduleId);
        if (module == null) {
            logger.warn("Could not find module with id {}", moduleId);
            return;
        }
        Service service = dataAccess.getService(serviceId);
        if (service == null) {
            throw new RuntimeException("Could not find service");
        }
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "deleting a module");
        Team team = dataAccess.getTeam(service.getTeamId());
        
        for (Host host : dataAccess.getHosts(serviceId)) {
            if (host.getModuleId().equals(moduleId)) {
                throw new RuntimeException("Can not delete module with an active host");
            }
        }
        for (Vip vip : dataAccess.getVips(serviceId)) {
            if (vip.getModuleId().equals(moduleId)) {
                throw new RuntimeException("Can not delete module with an active vip");
            }
        }

        List<Module> modules = dataAccess.getModules(serviceId);
        Collections.sort(modules);

        modules.remove(module.getOrder() - 1);
        int i = 1;
        for (Module temp : modules) {
            if (temp.getOrder() != i) {
                temp.setOrder(i);
                dataAccess.saveModule(temp);
            }
            i++;
        }
        dataAccess.deleteModule(serviceId, moduleId);
        
        WorkItem workItem = new WorkItem(Const.TYPE_MODULE, Const.OPERATION_DELETE, user, team, service, module, null, null, null);
        for (Module temp : modules) {
            workItem.addModule(temp);
        }
        dataAccess.saveWorkItem(workItem);
        workItemProcess.sendWorkItem(workItem);
    }

}
