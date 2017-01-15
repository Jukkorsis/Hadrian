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
import com.northernwall.hadrian.domain.CustomFunction;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.handlers.service.dao.PostCustomFunctionData;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard Thurston
 */
public class CustomFuntionCreateHandler extends BasicHandler {

    private final AccessHelper accessHelper;

    public CustomFuntionCreateHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostCustomFunctionData postCFData = fromJson(request, PostCustomFunctionData.class);
        Service service = getService(postCFData.serviceId, null);
        Team team = getTeam(service.getTeamId(), null);
        accessHelper.checkIfUserCanModify(request, team, "create custom function");

        CustomFunction customFunction = new CustomFunction(
                service.getServiceId(),
                postCFData.moduleId,
                postCFData.name,
                postCFData.method,
                postCFData.url,
                postCFData.teamOnly);
        getDataAccess().saveCustomFunction(customFunction);

        response.setStatus(200);
        request.setHandled(true);
    }

}
