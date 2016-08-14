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
package com.northernwall.hadrian.tree;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.tree.dao.TreeData;
import com.northernwall.hadrian.tree.dao.TreeServiceData;
import com.northernwall.hadrian.tree.dao.TreeTeamData;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
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
public class TreeHandler extends AbstractHandler {

    private final DataAccess dataAccess;
    private final Gson gson;

    public TreeHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        gson = new Gson();
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        TreeData treeData = new TreeData();
        
        User user = (User) request.getAttribute(Const.ATTR_USER);
        treeData.isAdmin = user.isAdmin();

        List<Team> teams = dataAccess.getTeams();
        List<Service> services = dataAccess.getActiveServices();
        Collections.sort(teams);
        for (Team team : teams) {
            TreeTeamData teamData = new TreeTeamData();
            teamData.teamId = team.getTeamId();
            teamData.teamName = team.getTeamName();
            List<Service> teamServices = Service.filterTeam(team.getTeamId(), services);
            Collections.sort(teamServices);
            for (Service service : teamServices) {
                TreeServiceData serviceData = new TreeServiceData();
                serviceData.serviceId = service.getServiceId();
                serviceData.serviceName = service.getServiceName();
                teamData.services.add(serviceData);
            }
            treeData.teams.add(teamData);
        }
        
        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            gson.toJson(treeData, TreeData.class, jw);
        }
        response.setStatus(200);
        request.setHandled(true);
    }
    
}
