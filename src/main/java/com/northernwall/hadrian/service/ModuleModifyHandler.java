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

import com.northernwall.hadrian.Util;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import com.northernwall.hadrian.service.dao.PutModuleData;
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
    private final DataAccess dataAccess;
    private final WorkItemProcessor workItemProcess;

    public ModuleModifyHandler(AccessHelper accessHelper, DataAccess dataAccess, WorkItemProcessor workItemProcess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
        this.dataAccess = dataAccess;
        this.workItemProcess = workItemProcess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String serviceId = target.substring(11, 47);
        String moduleId = target.substring(48);
        Service service = getService(serviceId, null);
        
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "update module");
        Team team = dataAccess.getTeam(service.getTeamId());

        PutModuleData putModuleData = Util.fromJson(request, PutModuleData.class);
        List<Module> modules = dataAccess.getModules(serviceId);
        List<Module> zeroModules = new LinkedList<>();
        Module module = null;
        for (Module temp : modules) {
            if (temp.getModuleId().equals(moduleId)) {
                module = temp;
            }
            if (temp.getOrder() == 0) {
                zeroModules.add(temp);
            }
        }
        if (module == null) {
            logger.warn("Could not find module with id {} in service {}", moduleId, serviceId);
            return;
        }
        modules.removeAll(zeroModules);
        Collections.sort(modules);
        if (putModuleData.order < 0) {
            putModuleData.order = 0;
        }
        if (putModuleData.order > modules.size()) {
            putModuleData.order = modules.size();
        }

        module.setModuleName(putModuleData.moduleName);
        module.setMavenGroupId(putModuleData.mavenGroupId);
        module.setMavenArtifactId(putModuleData.mavenArtifactId);
        module.setArtifactType(putModuleData.artifactType);
        module.setArtifactSuffix(putModuleData.artifactSuffix);
        module.setHostAbbr(putModuleData.hostAbbr.toLowerCase());
        module.setVersionUrl(putModuleData.versionUrl);
        module.setAvailabilityUrl(putModuleData.availabilityUrl);
        module.setRunAs(putModuleData.runAs);
        module.setDeploymentFolder(putModuleData.deploymentFolder);
        module.setStartCmdLine(putModuleData.startCmdLine);
        module.setStartTimeOut(putModuleData.startTimeOut);
        module.setStopCmdLine(putModuleData.stopCmdLine);
        module.setStopTimeOut(putModuleData.stopTimeOut);

        if (module.getOrder() != putModuleData.order) {
            if (module.getOrder() > 0) {
                modules.remove(module);
            } else {
                zeroModules.remove(module);
            }
            module.setOrder(putModuleData.order);
            if (putModuleData.order > 0) {
                modules.add(putModuleData.order - 1, module);
            } else {
                zeroModules.add(module);
            }
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

        WorkItem workItem = new WorkItem(Type.module, Operation.update, user, team, service, module, null, null);
        for (Module temp : zeroModules) {
            workItem.addModule(temp);
        }
        for (Module temp : modules) {
            workItem.addModule(temp);
        }
        dataAccess.saveWorkItem(workItem);
        workItemProcess.sendWorkItem(workItem);

        response.setStatus(200);
        request.setHandled(true);
    }

}
