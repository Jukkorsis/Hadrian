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
package com.northernwall.hadrian.handlers.service;

import com.google.gson.Gson;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.handlers.routing.Http400BadRequestException;
import com.northernwall.hadrian.workItem.dao.SmokeTestData;
import com.northernwall.hadrian.workItem.helper.SmokeTestHelper;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard Thurston
 */
public class SmokeTestExecHandler extends BasicHandler {

    private final AccessHelper accessHelper;
    private final SmokeTestHelper smokeTestHelper;

    public SmokeTestExecHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper, SmokeTestHelper smokeTestHelper) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
        this.smokeTestHelper = smokeTestHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        Service service = getService(request);
        Team team = getTeam(service.getTeamId(), null);

        accessHelper.checkIfUserCanModify(request, team, "execute smoke test");

        Module module = getModule(request, service);
        
        if (module.getSmokeTestUrl() == null || module.getSmokeTestUrl().isEmpty()) {
            throw new Http400BadRequestException("No smoke test url defined for module");
        }
        
        String endPoint = request.getParameter("endPoint");

        if (endPoint == null || endPoint.isEmpty()) {
            throw new Http400BadRequestException("No end point provided");
        }
        
        SmokeTestData smokeTestData = smokeTestHelper.ExecuteSmokeTest(
                module.getSmokeTestUrl(),
                endPoint);

        if (smokeTestData == null) {
            throw new Http400BadRequestException("Error executing smoke test");
        }
        
        toJson(response, smokeTestData);
        response.setStatus(200);
        request.setHandled(true);
    }

}
