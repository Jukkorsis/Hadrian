/*
 * Copyright 2015 Richard Thurston.
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
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.service.dao.PostTeamAddUser;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http404NotFoundException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

public class TeamAddUserHandler extends BasicHandler {

    private final AccessHelper accessHelper;
    private final DataAccess dataAccess;

    public TeamAddUserHandler(AccessHelper accessHelper, DataAccess dataAccess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
        this.dataAccess = dataAccess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostTeamAddUser postTeamAddUser = fromJson(request, PostTeamAddUser.class);

        Team team = dataAccess.getTeam(postTeamAddUser.teamId);
        if (team == null) {
            throw new Http404NotFoundException("Failed to add user " + postTeamAddUser.username + " to team " + postTeamAddUser.teamId + ", could not find team");
        }
        
        accessHelper.checkIfUserCanModify(request, postTeamAddUser.teamId, "add user to team");
        
        if (dataAccess.getUser(postTeamAddUser.username) == null) {
            throw new Http404NotFoundException("Failed to add user " + postTeamAddUser.username + " to team " + postTeamAddUser.teamId + ", could not find user");
        }

        if (!team.getUsernames().contains(postTeamAddUser.username)) {
            team.getUsernames().add(postTeamAddUser.username);
            dataAccess.updateTeam(team);
        }
        
        response.setStatus(200);
        request.setHandled(true);
    }

}
