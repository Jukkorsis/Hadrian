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
import com.northernwall.hadrian.service.dao.DeleteModuleData;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http400BadRequestException;
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
    private final WorkItemProcessor workItemProcess;

    public ModuleDeleteHandler(AccessHelper accessHelper, DataAccess dataAccess, WorkItemProcessor workItemProcess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
        this.workItemProcess = workItemProcess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        DeleteModuleData deleteModuleData = fromJson(request, DeleteModuleData.class);
        Service service = getService(deleteModuleData.serviceId, null, null);
        Module module = getModule(deleteModuleData.moduleId, null, service);
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "deleting a module");
        Team team = getDataAccess().getTeam(service.getTeamId());

        for (Host host : getDataAccess().getHosts(deleteModuleData.serviceId)) {
            if (host.getModuleId().equals(deleteModuleData.moduleId)) {
                throw new Http400BadRequestException("Can not delete module with an active host");
            }
        }
        for (Vip vip : getDataAccess().getVips(deleteModuleData.serviceId)) {
            if (vip.getModuleId().equals(deleteModuleData.moduleId)) {
                throw new Http400BadRequestException("Can not delete module with an active vip");
            }
        }

        List<Module> modules = getDataAccess().getModules(deleteModuleData.serviceId);
        Collections.sort(modules);

        modules.remove(module.getOrder() - 1);
        int i = 1;
        for (Module temp : modules) {
            if (temp.getOrder() != i) {
                temp.setOrder(i);
                getDataAccess().saveModule(temp);
            }
            i++;
        }
        getDataAccess().deleteModule(deleteModuleData.serviceId, deleteModuleData.moduleId);

        WorkItem workItem = new WorkItem(Type.module, Operation.delete, user, team, service, module, null, null);
        for (Module temp : modules) {
            workItem.addModule(temp);
        }
        getDataAccess().saveWorkItem(workItem);
        workItemProcess.sendWorkItem(workItem);

        response.setStatus(200);
        request.setHandled(true);
    }

}
