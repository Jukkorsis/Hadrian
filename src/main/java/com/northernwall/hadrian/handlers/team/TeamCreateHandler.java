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
import com.northernwall.hadrian.handlers.team.dao.PostTeamData;
import com.northernwall.hadrian.handlers.routing.Http400BadRequestException;
import com.northernwall.hadrian.handlers.routing.Http405NotAllowedException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeamCreateHandler extends BasicHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(TeamCreateHandler.class);
    private final AccessHelper accessHelper;

    public TeamCreateHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        accessHelper.checkIfUserIsAdmin(request, "create team");

        PostTeamData data = fromJson(request, PostTeamData.class);

        if (data.teamName == null) {
            throw new Http400BadRequestException("Team Name is mising or empty");
        }
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
        if (data.securityGroupName == null || data.securityGroupName.isEmpty()) {
            throw new Http400BadRequestException("Team Name is mising or empty");
        }

        for (Team temp : getDataAccess().getTeams()) {
            if (temp.getTeamName().equalsIgnoreCase(data.teamName)) {
                throw new Http405NotAllowedException("Failed to create new team, as a team with name " + data.teamName + " already exists");
            }
            if (temp.getGitGroup().equalsIgnoreCase(data.gitGroup)) {
                LOGGER.warn("Creating new team with name " + data.teamName + ", but it reuses another team's (" + temp.getTeamName() + ") GIT Group, " + data.gitGroup);
            }
        }

        if (!data.teamPage.toLowerCase().startsWith(Const.HTTP)
                && !data.teamPage.toLowerCase().startsWith(Const.HTTPS)) {
            data.teamPage = Const.HTTP + data.teamPage;
        }

        Team team = new Team(data.teamName, data.teamEmail, data.teamSlack, data.gitGroup, data.teamPage, data.calendarId, "black", data.securityGroupName);

        getDataAccess().saveTeam(team);
        response.setStatus(200);
        request.setHandled(true);
    }

}
