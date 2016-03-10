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
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http400BadRequestException;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import java.io.IOException;
import java.util.Collections;
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
public class ModuleDeleteHandler extends BasicHandler {

    private final static Logger logger = LoggerFactory.getLogger(ModuleDeleteHandler.class);

    private final AccessHelper accessHelper;
    private final DataAccess dataAccess;
    private final WorkItemProcessor workItemProcess;

    public ModuleDeleteHandler(AccessHelper accessHelper, DataAccess dataAccess, WorkItemProcessor workItemProcess) {
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
        Module module = getModule(moduleId, null, service);
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "deleting a module");
        Team team = dataAccess.getTeam(service.getTeamId());

        for (Host host : dataAccess.getHosts(serviceId)) {
            if (host.getModuleId().equals(moduleId)) {
                throw new Http400BadRequestException("Can not delete module with an active host");
            }
        }
        for (Vip vip : dataAccess.getVips(serviceId)) {
            if (vip.getModuleId().equals(moduleId)) {
                throw new Http400BadRequestException("Can not delete module with an active vip");
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

        WorkItem workItem = new WorkItem(Type.module, Operation.delete, user, team, service, module, null, null);
        for (Module temp : modules) {
            workItem.addModule(temp);
        }
        dataAccess.saveWorkItem(workItem);
        workItemProcess.sendWorkItem(workItem);

        response.setStatus(200);
        request.setHandled(true);
    }

}
