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
package com.northernwall.hadrian.handlers.tree;

import com.northernwall.hadrian.ConfigHelper;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Config;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleType;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
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
public class CatalogReportHandler extends AbstractHandler {

    private final AccessHelper accessHelper;
    private final DataAccess dataAccess;
    private final ConfigHelper configHelper;

    public CatalogReportHandler(AccessHelper accessHelper, DataAccess dataAccess, ConfigHelper configHelper) {
        this.accessHelper = accessHelper;
        this.dataAccess = dataAccess;
        this.configHelper = configHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        accessHelper.checkIfUserIsAdmin(request, "Catalog Report");
        
        Config config = configHelper.getConfig();
        List<Team> teams = dataAccess.getTeams();
        List<Service> services = dataAccess.getActiveServices();
        Collections.sort(teams);

        //Header row
        response.getWriter().print("Team,Service,Scope,DoBuilds,DoDeploys,DoManageVip,DoCheckJar,Module,Type,HostAbbr,Outbound,RunAs");
        for (String dc : config.dataCenters) {
            response.getWriter().print(",");
            response.getWriter().print(dc);
        }
        for (String environmentName : config.environmentNames) {
            response.getWriter().print(",");
            response.getWriter().print(environmentName);
        }
        response.getWriter().println();

        //Data rows
        for (Team team : teams) {
            List<Service> teamServices = Service.filterTeam(team.getTeamId(), services);
            Collections.sort(teamServices);
            for (Service service : teamServices) {
                List<Module> modules = dataAccess.getModules(service.getServiceId());
                if (modules == null || modules.isEmpty()) {
                    outputService(response, team, service);
                    response.getWriter().print(",,");
                    for (String dc : config.dataCenters) {
                        response.getWriter().print(",");
                    }
                    for (String environmentName : config.environmentNames) {
                        response.getWriter().print(",");
                    }
                    response.getWriter().println();
                } else {
                    List<Host> hosts = dataAccess.getHosts(service.getServiceId());
                    for (Module module : modules) {
                        outputService(response, team, service);
                        response.getWriter().print(",");
                        response.getWriter().print(module.getModuleName());
                        response.getWriter().print(",");
                        response.getWriter().print(module.getModuleType());
                        
                        if (module.getModuleType() != ModuleType.Library) {
                            response.getWriter().print(",");
                            response.getWriter().print(module.getHostAbbr());
                            response.getWriter().print(",");
                            response.getWriter().print(module.getOutbound());
                            response.getWriter().print(",");
                            response.getWriter().print(module.getRunAs());

                            HashMap<String, Integer> counts = new HashMap<>();
                            if (hosts != null && !hosts.isEmpty()) {
                                for (Host host : hosts) {
                                    if (host.getModuleId().equals(module.getModuleId())) {
                                        if (counts.containsKey(host.getDataCenter())) {
                                            int i = counts.get(host.getDataCenter()) + 1;
                                            counts.put(host.getDataCenter(), i);
                                        } else {
                                            counts.put(host.getDataCenter(), 1);
                                        }
                                    }
                                }
                            }
                            for (String dataCenter : config.dataCenters) {
                                response.getWriter().print(",");
                                if (counts.containsKey(dataCenter)) {
                                    response.getWriter().print(counts.get(dataCenter));
                                } else {
                                    response.getWriter().print("0");
                                }
                            }

                            counts.clear();
                            if (hosts != null && !hosts.isEmpty()) {
                                for (Host host : hosts) {
                                    if (host.getModuleId().equals(module.getModuleId())) {
                                        if (counts.containsKey(host.getEnvironment())) {
                                            int i = counts.get(host.getEnvironment()) + 1;
                                            counts.put(host.getEnvironment(), i);
                                        } else {
                                            counts.put(host.getEnvironment(), 1);
                                        }
                                    }
                                }
                            }
                            for (String environmentName : config.environmentNames) {
                                response.getWriter().print(",");
                                if (counts.containsKey(environmentName)) {
                                    response.getWriter().print(counts.get(environmentName));
                                } else {
                                    response.getWriter().print("0");
                                }
                            }
                        } else {
                            response.getWriter().print(",,,");
                            for (String dataCenter : config.dataCenters) {
                                response.getWriter().print(",");
                            }
                            for (String environmentName : config.environmentNames) {
                                response.getWriter().print(",");
                            }
                        }
                        response.getWriter().println();
                    }
                }
            }
        }

        response.setStatus(200);
        request.setHandled(true);
    }

    private void outputService(HttpServletResponse response, Team team, Service service) throws IOException {
        response.getWriter().print(team.getTeamName());
        response.getWriter().print(",");
        response.getWriter().print(service.getServiceName());
        response.getWriter().print(",");
        response.getWriter().print(service.getScope());
        response.getWriter().print(",");
        response.getWriter().print(service.isDoBuilds());
        response.getWriter().print(",");
        response.getWriter().print(service.isDoDeploys());
        response.getWriter().print(",");
        response.getWriter().print(service.isDoManageVip());
        response.getWriter().print(",");
        response.getWriter().print(service.isDoCheckJar());
    }

}
