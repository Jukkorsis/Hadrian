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

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ModuleRef;
import com.northernwall.hadrian.domain.ModuleType;
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
public class GraphAllHandler extends AbstractHandler {

    private final DataAccess dataAccess;

    public GraphAllHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType(Const.TEXT);
        Graph graph = new Graph(response.getOutputStream(), true);

        List<Service> services = dataAccess.getActiveServices();
        for (Service service : services) {
            Team team = dataAccess.getTeam(service.getTeamId());
            List<Module> modules = dataAccess.getModules(service.getServiceId());
            for (Module module : modules) {
                if (module.getModuleType() == ModuleType.Deployable) {
                    List<Module> libraries = new LinkedList<>();
                    processLibrary(module, module, libraries, graph);
                    graph.writeModuleStructure(module, libraries, team.getColour());
                }
            }
        }
        graph.close();

        request.setHandled(true);
        response.setStatus(200);
    }

    public void processLibrary(Module root, Module module, List<Module> libraries, Graph graph) throws IOException {
        List<ModuleRef> moduleRefs = dataAccess.getModuleRefsByClient(module.getServiceId(), module.getModuleId());
        for (ModuleRef moduleRef : moduleRefs) {
            Module serverModule = dataAccess.getModule(moduleRef.getServerServiceId(), moduleRef.getServerModuleId());
            if (serverModule.getModuleType() == ModuleType.Library) {
                if (!libraries.contains(serverModule)) {
                    libraries.add(serverModule);
                    processLibrary(root, serverModule, libraries, graph);
                }
            }
            if (serverModule.getModuleType() == ModuleType.Deployable) {
                graph.writeLink(root.getModuleName(), serverModule.getModuleName());
            }
        }
    }

}
