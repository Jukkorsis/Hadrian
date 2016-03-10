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
package com.northernwall.hadrian.service;

import com.northernwall.hadrian.service.helper.HostDetailsHelper;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.service.dao.GetHostDetailsData;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http404NotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 *
 * @author Richard Thurston
 */
public class HostGetHandler extends AbstractHandler {

    private final DataAccess dataAccess;
    private final HostDetailsHelper hostDetailsHelper;
    private final Gson gson;

    public HostGetHandler(DataAccess dataAccess, HostDetailsHelper hostDetailsHelper) {
        this.dataAccess = dataAccess;
        this.hostDetailsHelper = hostDetailsHelper;
        gson = new Gson();
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String serviceId = target.substring(9, 45);
        String hostId = target.substring(46, 82);
        Host host = dataAccess.getHost(serviceId, hostId);
        if (host == null) {
            throw new Http404NotFoundException("Could not find host");
        }

        GetHostDetailsData details = hostDetailsHelper.getDetails(host);

        response.setContentType(Const.JSON);
        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            gson.toJson(details, GetHostDetailsData.class, jw);
        }
        response.setStatus(200);
        request.setHandled(true);
    }

}
