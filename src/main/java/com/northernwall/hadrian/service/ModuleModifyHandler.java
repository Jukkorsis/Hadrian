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

import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import com.northernwall.hadrian.service.dao.PutModuleData;
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
public class ModuleModifyHandler extends BasicHandler {

    private final static Logger logger = LoggerFactory.getLogger(ModuleModifyHandler.class);

    private final AccessHelper accessHelper;
    private final WorkItemProcessor workItemProcess;

    public ModuleModifyHandler(AccessHelper accessHelper, DataAccess dataAccess, WorkItemProcessor workItemProcess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
        this.workItemProcess = workItemProcess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PutModuleData data = fromJson(request, PutModuleData.class);
        Service service = getService(data.serviceId, null, null);
        
        Team team = getTeam(service.getTeamId(), null);
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "update module");
        
        List<Host> hosts = getDataAccess().getHosts(data.serviceId);
        for (Host host : hosts) {
            if (host.getModuleId().equals(data.moduleId)) {
                Boolean temp = data.networkNames.get(host.getNetwork());
                if (temp == null || !temp.booleanValue()) {
                    throw new Http400BadRequestException("Can not remove a network from a module with an active host");
                }
            }
        }

        List<Module> modules = getDataAccess().getModules(data.serviceId);
        List<Module> zeroModules = new LinkedList<>();
        Module module = null;
        for (Module temp : modules) {
            if (temp.getModuleId().equals(data.moduleId)) {
                module = temp;
            }
            if (temp.getOrder() == 0) {
                zeroModules.add(temp);
            }
        }
        if (module == null) {
            logger.warn("Could not find module with id {} in service {}", data.moduleId, data.serviceId);
            return;
        }
        modules.removeAll(zeroModules);
        Collections.sort(modules);
        if (data.order < 0) {
            data.order = 0;
        }
        if (data.order > modules.size()) {
            data.order = modules.size();
        }
        
        module.setModuleName(data.moduleName);
        module.setMavenGroupId(data.mavenGroupId);
        module.setMavenArtifactId(data.mavenArtifactId);
        module.setArtifactType(data.artifactType);
        module.setArtifactSuffix(data.artifactSuffix);
        module.setHostAbbr(data.hostAbbr.toLowerCase());
        module.setVersionUrl(data.versionUrl);
        module.setAvailabilityUrl(data.availabilityUrl);
        module.setRunAs(data.runAs);
        module.setDeploymentFolder(data.deploymentFolder);
        module.setStartCmdLine(data.startCmdLine);
        module.setStartTimeOut(data.startTimeOut);
        module.setStopCmdLine(data.stopCmdLine);
        module.setStopTimeOut(data.stopTimeOut);
        module.setConfigName(data.configName);
        module.setNetworkNames(data.networkNames);

        if (module.getOrder() != data.order) {
            if (module.getOrder() > 0) {
                modules.remove(module);
            } else {
                zeroModules.remove(module);
            }
            module.setOrder(data.order);
            if (data.order > 0) {
                modules.add(data.order - 1, module);
            } else {
                zeroModules.add(module);
            }
            int i = 1;
            for (Module temp : modules) {
                if (temp.getOrder() != i) {
                    temp.setOrder(i);
                    getDataAccess().saveModule(temp);
                }
                i++;
            }
        }
        getDataAccess().saveModule(module);

        WorkItem workItem = new WorkItem(Type.module, Operation.update, user, team, service, module, null, null);
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
