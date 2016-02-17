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
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import com.northernwall.hadrian.service.dao.PostModuleData;
import com.northernwall.hadrian.service.dao.PutDeploySoftwareData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
            if (target.startsWith("/v1/module/")) {
                switch (request.getMethod()) {
                    case "POST":
                        if (target.equalsIgnoreCase("/v1/module/module")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            createModule(request);
                        } else {
                            throw new RuntimeException("Unknown host operation");
                        }
                        break;
                    case "PUT":
                        if (target.equalsIgnoreCase("/v1/module/module")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            updateModule(request);
                        } else {
                            throw new RuntimeException("Unknown host operation");
                        }
                        break;
                    case "DELETE":
                        if (target.matches("/v1/module/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            String serviceId = target.substring(9, 45);
                            String moduleId = target.substring(46);
                            deleteModule(request, serviceId, moduleId);
                        } else {
                            throw new RuntimeException("Unknown host operation");
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

        WorkItem workItem = new WorkItem(Const.TYPE_MODULE, Const.OPERATION_CREATE, user, team, service, module, null, null, null);
        workItem.getModule().template = postModuleData.template;
        for (Module temp : modules) {
            workItem.addModule(temp);
        }

        dataAccess.saveWorkItem(workItem);

        workItemProcess.sendWorkItem(workItem);
    }

    private void updateModule(Request request) throws IOException {
        PutDeploySoftwareData putHostData = Util.fromJson(request, PutDeploySoftwareData.class);
        Service service = null;
        List<WorkItem> workItems = new ArrayList<>(putHostData.hosts.size());
        User user = null;

        Team team = null;

        for (Map.Entry<String, String> entry : putHostData.hosts.entrySet()) {
            if (entry.getValue().equalsIgnoreCase("true")) {
                Host host = dataAccess.getHost(putHostData.serviceId, entry.getKey());
                if (host != null && host.getServiceId().equals(putHostData.serviceId) && host.getStatus().equals(Const.NO_STATUS)) {
                    if (service == null) {
                        service = dataAccess.getService(host.getServiceId());
                        if (service == null) {
                            throw new RuntimeException("Could not find service");
                        }
                        user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "update a host");
                        team = dataAccess.getTeam(service.getTeamId());
                    }
                    WorkItem workItem = new WorkItem(Const.TYPE_HOST, Const.OPERATION_DEPLOY, user, team, service, null, host, null, null);
                    workItem.getHost().version = putHostData.version;
                    if (workItems.isEmpty()) {
                        host.setStatus("Deploying...");
                    } else {
                        host.setStatus("Deploy Queued");
                    }
                    dataAccess.updateHost(host);
                    workItems.add(workItem);
                }
            }
        }
        String prevId = null;
        int size = workItems.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                WorkItem workItem = workItems.get(size - i - 1);
                workItem.setNextId(prevId);
                prevId = workItem.getId();
                dataAccess.saveWorkItem(workItem);
            }
            workItemProcess.sendWorkItem(workItems.get(0));
        }
    }

    private void deleteModule(Request request, String serviceId, String hostId) throws IOException {
        Host host = dataAccess.getHost(serviceId, hostId);
        if (host == null) {
            logger.info("Could not find host with id {}", hostId);
            return;
        }
        Service service = dataAccess.getService(host.getServiceId());
        if (service == null) {
            throw new RuntimeException("Could not find service");
        }
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "deleting a host");
        Team team = dataAccess.getTeam(service.getTeamId());
        host.setStatus("Deleting...");
        dataAccess.updateHost(host);
        WorkItem workItem = new WorkItem(Const.TYPE_HOST, Const.OPERATION_DELETE, user, team, service, null, host, null, null);
        dataAccess.saveWorkItem(workItem);
        workItemProcess.sendWorkItem(workItem);
    }

}
