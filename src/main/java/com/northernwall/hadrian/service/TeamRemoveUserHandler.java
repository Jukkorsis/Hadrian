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
import com.northernwall.hadrian.service.dao.DeleteTeamRemoveUser;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http405NotAllowedException;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http404NotFoundException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

public class TeamRemoveUserHandler extends BasicHandler {

    private final AccessHelper accessHelper;

    public TeamRemoveUserHandler(AccessHelper accessHelper, DataAccess dataAccess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        DeleteTeamRemoveUser deleteTeamAddUser = fromJson(request, DeleteTeamRemoveUser.class);

        Team team = getDataAccess().getTeam(deleteTeamAddUser.teamId);
        if (team == null) {
            throw new Http404NotFoundException("Failed to add user " + deleteTeamAddUser.username + " to team " + deleteTeamAddUser.teamId + ", could not find team");
        }

        accessHelper.checkIfUserCanModify(request, deleteTeamAddUser.teamId, "remove user from team");

        if (team.getUsernames().size() < 2) {
            throw new Http405NotAllowedException("Can not remove the last user from team " + team.getTeamName());
        }

        if (team.getUsernames().contains(deleteTeamAddUser.username)) {
            team.getUsernames().remove(deleteTeamAddUser.username);
            getDataAccess().updateTeam(team);
        }

        response.setStatus(200);
        request.setHandled(true);
    }

}
