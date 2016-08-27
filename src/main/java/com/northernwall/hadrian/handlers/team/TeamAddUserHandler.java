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
package com.northernwall.hadrian.handlers.team;

import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.handlers.team.dao.PostTeamAddUserData;
import com.northernwall.hadrian.handlers.utility.routingHandler.Http404NotFoundException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

public class TeamAddUserHandler extends BasicHandler {

    private final AccessHelper accessHelper;

    public TeamAddUserHandler(AccessHelper accessHelper, DataAccess dataAccess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostTeamAddUserData data = fromJson(request, PostTeamAddUserData.class);
        Team team = getTeam(data.teamId, null);
        accessHelper.checkIfUserCanModify(request, data.teamId, "add user to team");
        
        if (getDataAccess().getUser(data.username) == null) {
            throw new Http404NotFoundException("Failed to add user " + data.username + " to team " + data.teamId + ", could not find user");
        }

        if (!team.getUsernames().contains(data.username)) {
            team.getUsernames().add(data.username);
            getDataAccess().updateTeam(team);
        }
        
        response.setStatus(200);
        request.setHandled(true);
    }

}
