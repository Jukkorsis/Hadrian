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

import com.google.gson.Gson;
import com.northernwall.hadrian.config.Const;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.handlers.team.dao.PutTeamData;
import com.northernwall.hadrian.handlers.routing.Http400BadRequestException;
import com.northernwall.hadrian.handlers.routing.Http405NotAllowedException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeamModifyHandler extends BasicHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(TeamModifyHandler.class);
    private final AccessHelper accessHelper;

    public TeamModifyHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PutTeamData data = fromJson(request, PutTeamData.class);
        Team team = getTeam(data.teamId, null);
        accessHelper.checkIfUserCanModify(request, team, "update team");
        data.teamName = data.teamName.trim();
        if (data.teamName.isEmpty()) {
            throw new Http400BadRequestException("Team Name is mising or empty");
        }
        if (data.teamName.length() > 30) {
            throw new Http400BadRequestException("Team Name is to long, max is 30");
        }

        if (data.gitGroup == null || data.gitGroup.isEmpty()) {
            throw new Http400BadRequestException("Git Group is mising or empty");
        }
        if (data.gitGroup.length() > 30) {
            throw new Http400BadRequestException("Git Group is to long, max is 30");
        }

        for (Team temp : getDataAccess().getTeams()) {
            if (!temp.getTeamId().equals(data.teamId)) {
                if (temp.getTeamName().equals(data.teamName)) {
                    throw new Http405NotAllowedException("Can not change team name, as a team with name " + data.teamName + " already exists");
                }
                if (temp.getGitGroup().equals(data.gitGroup)) {
                    LOGGER.warn("Modifying team with name " + data.teamName + ", but it reuses another team's (" + temp.getTeamName() + ") GIT Group, " + data.gitGroup);
                }
            }
        }

        if (data.teamPage != null && data.teamPage.isEmpty()) {
            if (!data.teamPage.toLowerCase().startsWith(Const.HTTP)
                    && !data.teamPage.toLowerCase().startsWith(Const.HTTPS)) {
                data.teamPage = Const.HTTP + data.teamPage;
            }
        }

        team.setTeamName(data.teamName);
        team.setTeamEmail(data.teamEmail);
        team.setTeamSlack(data.teamSlack);
        team.setGitGroup(data.gitGroup);
        team.setTeamPage(data.teamPage);
        team.setCalendarId(data.calendarId);
        team.setColour(data.colour);
        team.setSecurityGroupName(data.securityGroupName);

        getDataAccess().saveTeam(team);
        response.setStatus(200);
        request.setHandled(true);
    }

}
