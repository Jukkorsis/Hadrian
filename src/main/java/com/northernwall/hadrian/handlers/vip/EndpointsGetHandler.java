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
package com.northernwall.hadrian.handlers.vip;

import com.google.gson.Gson;
import com.northernwall.hadrian.config.Const;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.db.SearchResult;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.handlers.routing.Http404NotFoundException;
import com.northernwall.hadrian.handlers.vip.dao.GetEndpointData;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard Thurston
 */
public class EndpointsGetHandler extends BasicHandler {

    public EndpointsGetHandler(DataAccess dataAccess, Gson gson) {
        super(dataAccess, gson);
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String hostname = request.getParameter("hostname");
        SearchResult result = getDataAccess().doSearch(Const.SEARCH_SPACE_HOST_NAME, hostname);

        if (result == null) {
            throw new Http404NotFoundException("Could not find hostname");
        }

        Module module = getDataAccess().getModule(result.serviceId, result.moduleId);
        String monitoringPath = module.getAvailabilityUrl();
        int i = monitoringPath.indexOf("/");
        monitoringPath = monitoringPath.substring(i);

        List<GetEndpointData> endpoints = new LinkedList<>();
        List<Vip> vips = getDataAccess().getVips(result.serviceId);
        for (Vip vip : vips) {
            if (vip.getModuleId().equals(module.getModuleId())) {
                GetEndpointData endpoint = GetEndpointData.create(vip);
                endpoint.monitoringPath = monitoringPath;
            }
        }

        toJson(response, endpoints);
        response.setStatus(200);
        request.setHandled(true);
    }

}
