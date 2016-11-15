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

import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.details.HostDetailsHelper;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.db.SearchResult;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.handlers.service.dao.FindHostData;
import com.northernwall.hadrian.handlers.service.helper.InfoHelper;
import com.northernwall.hadrian.handlers.utility.routingHandler.Http400BadRequestException;
import com.northernwall.hadrian.handlers.utility.routingHandler.Http404NotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard Thurston
 */
public class HostFindHandler extends BasicHandler {

    private final InfoHelper infoHelper;
    private final HostDetailsHelper hostDetailsHelper;

    public HostFindHandler(DataAccess dataAccess, InfoHelper infoHelper, HostDetailsHelper hostDetailsHelper) {
        super(dataAccess);
        this.infoHelper = infoHelper;
        this.hostDetailsHelper = hostDetailsHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String hostName = request.getParameter("hostName");
        if (hostName == null || hostName.isEmpty()) {
            throw new Http400BadRequestException("hostName is empty");
        }

        SearchResult searchResult = getDataAccess().doSearch(
                Const.SEARCH_SPACE_HOST_NAME,
                hostName);
        if (searchResult == null) {
            throw new Http404NotFoundException("Could not find host " + hostName);
        }
        
        Host host = getDataAccess().getHost(searchResult.serviceId, searchResult.hostId);
        if (host == null) {
            throw new Http404NotFoundException("Could not find host " + hostName + ".");
        }

        Service service = getDataAccess().getService(host.getServiceId());
        if (service == null) {
            throw new Http404NotFoundException("Could not find service for host " + hostName);
        }

        Module module = getDataAccess().getModule(host.getServiceId(), host.getModuleId());
        if (module == null) {
            throw new Http404NotFoundException("Could not find module host " + hostName);
        }

        Team team = getDataAccess().getTeam(service.getTeamId());
        if (module == null) {
            throw new Http404NotFoundException("Could not find team host " + hostName);
        }

        FindHostData findHostData = new FindHostData();
        findHostData.teamId = team.getTeamId();
        findHostData.teamName = team.getTeamName();
        findHostData.serviceId = service.getServiceId();
        findHostData.serviceName = service.getServiceName();
        findHostData.moduleId = module.getModuleId();
        findHostData.moduleName = module.getModuleName();
        findHostData.hostId = host.getHostId();
        findHostData.hostName = host.getHostName();
        findHostData.status = host.getStatus();
        findHostData.busy = host.isBusy();
        findHostData.dataCenter = host.getDataCenter();
        findHostData.environment = host.getEnvironment();
        findHostData.platform = host.getPlatform();

        findHostData.version = infoHelper.readVersion(hostName, module.getVersionUrl());
        findHostData.availability = infoHelper.readAvailability(hostName, module.getAvailabilityUrl());
        findHostData.details = hostDetailsHelper.getDetails(host);

        response.setContentType(Const.JSON);
        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            getGson().toJson(findHostData, FindHostData.class, jw);
        }
        response.setStatus(200);
        request.setHandled(true);
    }

}
