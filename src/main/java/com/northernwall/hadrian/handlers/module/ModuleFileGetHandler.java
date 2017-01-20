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
package com.northernwall.hadrian.handlers.module;

import com.google.gson.Gson;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleFile;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.handlers.service.dao.GetModuleFileData;
import com.northernwall.hadrian.handlers.routing.Http400BadRequestException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard Thurston
 */
public class ModuleFileGetHandler extends BasicHandler {

    private final AccessHelper accessHelper;

    public ModuleFileGetHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        Service service = getService(request);
        Team team = getTeam(service.getTeamId(), null);
        Module module = getModule(request, service);
        accessHelper.checkIfUserCanModify(request, team, "manage file for module");
        String environment = request.getParameter("environment");
        if (environment == null || environment.isEmpty()) {
            throw new Http400BadRequestException("parameter environment is missing");
        }

        List<GetModuleFileData> getModuleFileDataList = new ArrayList<>();
        List<ModuleFile> moduleFiles = getDataAccess().getModuleFiles(service.getServiceId(), module.getModuleId(), environment);
        if (moduleFiles != null && !moduleFiles.isEmpty()) {
            for (ModuleFile moduleFile : moduleFiles) {
                getModuleFileDataList.add(new GetModuleFileData(moduleFile.getName(), moduleFile.getContents()));
            }
        }

        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            getGson().toJson(getModuleFileDataList, new TypeToken<List<GetModuleFileData>>(){}.getType(), jw);
        }
        response.setStatus(200);
        request.setHandled(true);
    }
}
