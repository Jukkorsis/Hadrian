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
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.config.ConfigHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.db.SearchSpace;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleFile;
import com.northernwall.hadrian.domain.ModuleRef;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.handlers.module.dao.DeleteModuleData;
import com.northernwall.hadrian.handlers.routing.Http400BadRequestException;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard Thurston
 */
public class ModuleDeleteHandler extends BasicHandler {

    private final AccessHelper accessHelper;
    private final WorkItemProcessor workItemProcessor;
    private final ConfigHelper configHelper;

    public ModuleDeleteHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper, WorkItemProcessor workItemProcessor, ConfigHelper configHelper) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
        this.workItemProcessor = workItemProcessor;
        this.configHelper = configHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        DeleteModuleData data = fromJson(request, DeleteModuleData.class);
        Service service = getService(data.serviceId, null);
        Module module = getModule(data.moduleId, null, service);
        Team team = getTeam(service.getTeamId(), null);
        User user = accessHelper.checkIfUserCanModify(request, team, "deleting a module");

        for (Host host : getDataAccess().getHosts(data.serviceId)) {
            if (host.getModuleId().equals(data.moduleId)) {
                throw new Http400BadRequestException("Can not delete module with an active host");
            }
        }
        for (Vip vip : getDataAccess().getVips(data.serviceId)) {
            if (vip.getModuleId().equals(data.moduleId)) {
                throw new Http400BadRequestException("Can not delete module with an active vip");
            }
        }

        List<ModuleRef> refs;
        refs = getDataAccess().getModuleRefsByClient(data.serviceId, data.moduleId);
        if (refs != null && !refs.isEmpty()) {
            throw new Http400BadRequestException("Can not delete a module which uses another module");
        }

        refs = getDataAccess().getModuleRefsByServer(data.serviceId, data.moduleId);
        if (refs != null && !refs.isEmpty()) {
            throw new Http400BadRequestException("Can not delete a module which is being used by anoter module");
        }

        List<Module> modules = getDataAccess().getModules(data.serviceId);
        Collections.sort(modules);

        getDataAccess().deleteModule(data.serviceId, data.moduleId);

        for (String env : configHelper.getConfig().environmentNames) {
            List<ModuleFile> moduleFiles = getDataAccess().getModuleFiles(
                    service.getServiceId(),
                    module.getModuleId(),
                    env);
            if (moduleFiles != null && !moduleFiles.isEmpty()) {
                for (ModuleFile moduleFile : moduleFiles) {
                    getDataAccess().deleteModuleFile(
                            service.getServiceId(),
                            module.getModuleId(),
                            env,
                            moduleFile.getName());
                }
            }
        }

        if (service.getMavenGroupId() != null
                && !service.getMavenGroupId().isEmpty()
                && module.getMavenArtifactId() != null
                && !module.getMavenArtifactId().isEmpty()) {
            getDataAccess().deleteSearch(
                    SearchSpace.mavenGroupArtifact,
                    service.getMavenGroupId() + "." + module.getMavenArtifactId());
        }

        WorkItem workItem = new WorkItem(Type.module, Operation.delete, user, team, service, module, null, null);
        for (Module temp : modules) {
            workItem.addModule(temp);
        }
        workItemProcessor.processWorkItem(workItem);

        response.setStatus(200);
        request.setHandled(true);
    }

}
