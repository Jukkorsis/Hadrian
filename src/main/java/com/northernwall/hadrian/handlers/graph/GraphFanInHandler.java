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
package com.northernwall.hadrian.handlers.graph;

import com.northernwall.hadrian.config.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleRef;
import com.northernwall.hadrian.domain.ModuleType;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 *
 * @author Richard Thurston
 */
public class GraphFanInHandler extends AbstractHandler {

    private final DataAccess dataAccess;

    public GraphFanInHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String serviceId = target.substring(16);

        response.setContentType(Const.TEXT);
        Graph graph = new Graph(response.getOutputStream(), false);

        List<Module> modules = new LinkedList<>();
        List<String> foundIds = new LinkedList<>();
        Service service = dataAccess.getService(serviceId);
        Team team = dataAccess.getTeam(service.getTeamId());
        for (Module module : dataAccess.getModules(serviceId)) {
            if (module.getModuleType() != ModuleType.Test) {
                fanIn(module, graph, modules, foundIds, false);
                graph.writeModule(module, team.getColour());
            }
            foundIds.add(module.getModuleId());
        }
        while (!modules.isEmpty()) {
            Module module = modules.remove(0);
            if (module.getModuleType() != ModuleType.Test) {
                fanIn(module, graph, modules, foundIds, true);
                service = dataAccess.getService(module.getServiceId());
                team = dataAccess.getTeam(service.getTeamId());
                graph.writeModule(module, team.getColour());
            }
            foundIds.add(module.getModuleId());
        }
        graph.close();

        request.setHandled(true);
        response.setStatus(200);
    }

    private void fanIn(Module module, Graph graph, List<Module> modules, List<String> foundIds, boolean checkFound) throws IOException {
        List<ModuleRef> moduleRefs;

        moduleRefs = dataAccess.getModuleRefsByServer(module.getServiceId(), module.getModuleId());
        if (moduleRefs != null && !moduleRefs.isEmpty()) {
            for (ModuleRef moduleRef : moduleRefs) {
                if (!checkFound || !foundIds.contains(moduleRef.getClientModuleId())) {
                    Module temp = dataAccess.getModule(moduleRef.getClientServiceId(), moduleRef.getClientModuleId());
                    graph.writeLink(temp.getModuleName(), module.getModuleName());
                    if (checkFound || !temp.getServiceId().equals(module.getServiceId())) {
                        modules.add(temp);
                    }
                }
            }
        }
    }

}
