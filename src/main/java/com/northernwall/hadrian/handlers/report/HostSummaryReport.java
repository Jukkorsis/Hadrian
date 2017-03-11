/*
 * Copyright 2016 Richard Thurston.
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
package com.northernwall.hadrian.handlers.report;

import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.config.Config;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleType;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Richard
 */
public class HostSummaryReport extends Report {
    
    protected final Config config;

    public HostSummaryReport(DataAccess dataAccess, Config config, PrintWriter writer) {
        super(dataAccess, writer);
        this.config = config;
    }

    @Override
    public void runReport() throws IOException {
        List<Team> teams = dataAccess.getTeams();
        List<Service> services = dataAccess.getActiveServices();
        Collections.sort(teams);

        //Header row
        outputServiceHeader();
        outputModuleHeader();
        outputListHeader(config.dataCenters);
        outputListHeader(config.environmentNames);
        writer.println();

        //Data rows
        for (Team team : teams) {
            List<Service> teamServices = Service.filterTeam(team.getTeamId(), services);
            Collections.sort(teamServices);
            for (Service service : teamServices) {
                List<Module> modules = dataAccess.getModules(service.getServiceId());
                if (modules == null || modules.isEmpty()) {
                    outputServiceRow(team, service);
                    outputModuleRow(null);
                    outputListRows(config.dataCenters, null, null);
                    outputListRows(config.environmentNames, null, null);
                    writer.println();
                } else {
                    List<Host> hosts = dataAccess.getHosts(service.getServiceId());
                    for (Module module : modules) {
                        outputServiceRow(team, service);
                        outputModuleRow(module);

                        if (module.getModuleType() != ModuleType.Library) {
                            List<Host> moduleHosts = Host.filterModule(module.getModuleId(), hosts);

                            HashMap<String, Integer> dcCounts = new HashMap<>();
                            HashMap<String, Integer> envCounts = new HashMap<>();
                            if (moduleHosts != null && !moduleHosts.isEmpty()) {
                                for (Host host : moduleHosts) {
                                    //DataCenter counts
                                    if (dcCounts.containsKey(host.getDataCenter())) {
                                        int i = dcCounts.get(host.getDataCenter()) + 1;
                                        dcCounts.put(host.getDataCenter(), i);
                                    } else {
                                        dcCounts.put(host.getDataCenter(), 1);
                                    }
                                    //Env counts
                                    if (envCounts.containsKey(host.getEnvironment())) {
                                        int i = envCounts.get(host.getEnvironment()) + 1;
                                        envCounts.put(host.getEnvironment(), i);
                                    } else {
                                        envCounts.put(host.getEnvironment(), 1);
                                    }
                                }
                            }

                            outputListRows(config.dataCenters, dcCounts, "0");
                            outputListRows(config.environmentNames, envCounts, "0");
                        } else {
                            outputListRows(config.dataCenters, null, null);
                            outputListRows(config.environmentNames, null, null);
                        }
                        writer.println();
                    }
                }
            }
        }
    }

}
