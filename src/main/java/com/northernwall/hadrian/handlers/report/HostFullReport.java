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
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Richard
 */
public class HostFullReport extends Report {

    public HostFullReport(DataAccess dataAccess, PrintWriter writer) {
        super(dataAccess, writer);
    }

    @Override
    public void runReport() throws IOException {
        List<Team> teams = dataAccess.getTeams();
        List<Service> services = dataAccess.getActiveServices();
        Collections.sort(teams);

        //Header row
        outputServiceHeader();
        outputModuleHeader();
        outputHostHeader();
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
                    outputHostRow(null);
                    writer.println();
                } else {
                    List<Host> hosts = dataAccess.getHosts(service.getServiceId());
                    for (Module module : modules) {
                        List<Host> moduleHosts = Host.filterModule(module.getModuleId(), hosts);
                        if (moduleHosts == null || moduleHosts.isEmpty()) {
                            outputServiceRow(team, service);
                            outputModuleRow(module);
                            outputHostRow(null);
                            writer.println();
                        } else {
                            for (Host host : moduleHosts) {
                                outputServiceRow(team, service);
                                outputModuleRow(module);
                                outputHostRow(host);
                                writer.println();
                            }
                        }
                    }
                }
            }
        }
    }

}
