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
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.handlers.team.dao.GetTeamData;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

public class TeamGetHandler extends BasicHandler {

    private final AccessHelper accessHelper;

    public TeamGetHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        Team team = getTeam(request);

        GetTeamData getTeamData = GetTeamData.create(team);
        getTeamData.canModify = accessHelper.canUserModify(request, team);

        toJson(response, getTeamData);
        response.setStatus(200);
        request.setHandled(true);
    }

}
