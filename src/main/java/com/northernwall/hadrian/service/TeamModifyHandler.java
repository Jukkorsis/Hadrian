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
import com.northernwall.hadrian.service.dao.PutTeamData;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http404NotFoundException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

public class TeamModifyHandler extends BasicHandler {

    private final AccessHelper accessHelper;

    public TeamModifyHandler(AccessHelper accessHelper, DataAccess dataAccess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String teamId = target.substring(9, target.length());
        accessHelper.checkIfUserCanModify(request, teamId, "update team");
        Team team = getDataAccess().getTeam(teamId);
        if (team == null) {
            throw new Http404NotFoundException("Can not find team " + teamId + ", could not update team");
        }

        PutTeamData putTeamData = fromJson(request, PutTeamData.class);

        team.setTeamName(putTeamData.teamName);
        team.setTeamEmail(putTeamData.teamEmail);
        team.setTeamIrc(putTeamData.teamIrc);
        team.setGitGroup(putTeamData.gitGroup);
        team.setCalendarId(putTeamData.calendarId);

        getDataAccess().saveTeam(team);
        response.setStatus(200);
        request.setHandled(true);
    }

}
