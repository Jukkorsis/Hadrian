/*
 * Copyright 2017 Richard Thurston.
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
package com.northernwall.hadrian.handlers.ssh;

import com.google.gson.Gson;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.handlers.ssh.dao.GetSshModuleData;
import com.northernwall.hadrian.sshAccess.SshAccess;
import com.northernwall.hadrian.sshAccess.SshEntry;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard
 */
public class SshModuleGetHandler extends BasicHandler {

    private final SshAccess sshAccess;

    public SshModuleGetHandler(DataAccess dataAccess, Gson gson, SshAccess sshAccess) {
        super(dataAccess, gson);
        this.sshAccess = sshAccess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        List<GetSshModuleData> sshModuleDatas = new LinkedList<>();
        List<SshEntry> sshEntries = sshAccess.getSshEntries();
        
        getAll(sshEntries, sshModuleDatas);

        toJson(response, sshModuleDatas);
        response.setStatus(200);
        request.setHandled(true);
    }

    private void getAll(List<SshEntry> sshEntries, List<GetSshModuleData> sshModuleDatas) {
        List<Team> teams = getDataAccess().getTeams();
        List<Service> services = getDataAccess().getActiveServices();
        Collections.sort(teams);
        if (teams != null && !teams.isEmpty() && services != null && !services.isEmpty()) {
            for (Team team : teams) {
                List<Service> teamServices = Service.filterTeam(team.getTeamId(), services);
                if (teamServices != null && !teamServices.isEmpty()) {
                    Collections.sort(teamServices);
                    for (Service service : teamServices) {
                        List<Host> hosts = getDataAccess().getHosts(service.getServiceId());
                        List<Module> modules = getDataAccess().getModules(service.getServiceId());
                        if (modules != null && !modules.isEmpty() && hosts != null && !hosts.isEmpty()) {
                            for (Module module : modules) {
                                GetSshModuleData sshModuleData = new GetSshModuleData();
                                sshModuleData.team = team.getTeamName();
                                sshModuleData.service = service.getServiceName();
                                sshModuleData.module = module.getModuleName();
                                sshModuleData.runas = module.getRunAs();
                                for (Host host : hosts) {
                                    sshModuleData.hostnames.add(host.getHostName());
                                }
                                sshModuleData.access = mergeSshEntries(team, sshEntries);
                                sshModuleDatas.add(sshModuleData);
                            }
                        }
                    }
                }
            }
        }
    }

    private List<SshEntry> mergeSshEntries(Team team, List<SshEntry> sshEntries) {
        return team.getSshEntries();
    }

}
