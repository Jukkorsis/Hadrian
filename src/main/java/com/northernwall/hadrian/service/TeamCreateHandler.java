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
import com.northernwall.hadrian.service.dao.PostTeamData;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http400BadRequestException;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http405NotAllowedException;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http404NotFoundException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

public class TeamCreateHandler extends BasicHandler {

    private final AccessHelper accessHelper;

    public TeamCreateHandler(AccessHelper accessHelper, DataAccess dataAccess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        accessHelper.checkIfUserIsAdmin(request, "create team");

        PostTeamData postTeamData = fromJson(request, PostTeamData.class);

        if (postTeamData.user == null) {
            throw new Http400BadRequestException("Failed to create new team, as user is null");
        }
        if (postTeamData.teamName == null) {
            throw new Http400BadRequestException("Failed to create new team, as team name is null");
        }
        postTeamData.teamName = postTeamData.teamName.trim();
        if (postTeamData.teamName.isEmpty()) {
            throw new Http400BadRequestException("Failed to create new team, as team name is empty");
        }
        for (Team temp : getDataAccess().getTeams()) {
            if (temp.getTeamName().equals(postTeamData.teamName)) {
                throw new Http405NotAllowedException("Failed to create new team, as team with name " + postTeamData.teamName + " already exists");
            }
        }

        Team team = new Team(postTeamData.teamName, postTeamData.teamEmail, postTeamData.teamIrc, postTeamData.gitGroup, postTeamData.calendarId);
        if (getDataAccess().getUser(postTeamData.user.getUsername()) == null) {
            throw new Http404NotFoundException("Failed to create new team, could not find initial user " + postTeamData.user.getUsername());
        }
        team.getUsernames().add(postTeamData.user.getUsername());

        getDataAccess().saveTeam(team);
        response.setStatus(200);
        request.setHandled(true);
    }

}
