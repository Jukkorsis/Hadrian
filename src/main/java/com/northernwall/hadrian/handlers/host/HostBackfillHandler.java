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
package com.northernwall.hadrian.handlers.host;

import com.google.gson.Gson;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.config.ConfigHelper;
import com.northernwall.hadrian.config.Const;
import com.northernwall.hadrian.GMT;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.db.SearchResult;
import com.northernwall.hadrian.domain.Audit;
import com.northernwall.hadrian.config.Config;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleType;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.handlers.service.dao.PostBackfillHostData;
import com.northernwall.hadrian.handlers.routing.Http400BadRequestException;
import com.northernwall.hadrian.parameters.Parameters;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Thurston
 */
public class HostBackfillHandler extends BasicHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(HostBackfillHandler.class);

    public static String scrubHostname(String hostname) {
        if (hostname == null) {
            return null;
        }
        String temp = hostname.trim();
        if (temp == null || temp.isEmpty()) {
            return null;
        }
        int index = temp.indexOf('.');
        if (index == -1) {
            return temp;
        }
        if (index == 0) {
            return null;
        }
        temp = temp.substring(0, index).trim();
        if (temp == null || temp.isEmpty()) {
            return null;
        }
        return temp;
    }

    private final AccessHelper accessHelper;
    private final ConfigHelper configHelper;
    private final Parameters parameter;

    public HostBackfillHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper, ConfigHelper configHelper, Parameters parameter) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
        this.configHelper = configHelper;
        this.parameter = parameter;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostBackfillHostData data = fromJson(request, PostBackfillHostData.class);
        Service service = getService(data.serviceId, null);
        Team team = getTeam(service.getTeamId(), null);
        User user = accessHelper.checkIfUserCanModify(request, team, "backfill host");

        Config config = configHelper.getConfig();
        if (!config.dataCenters.contains(data.dataCenter)) {
            throw new Http400BadRequestException("unknown datacenter, " + data.dataCenter);
        }
        if (!config.environmentNames.contains(data.environment)) {
            throw new Http400BadRequestException("unknown environment, " + data.environment);
        }
        if (!config.platforms.contains(data.platform)) {
            throw new Http400BadRequestException("unknown operating platform, " + data.platform);
        }

        if (data.hosts == null || data.hosts.isEmpty()) {
            throw new Http400BadRequestException("Hosts is empty");
        }

        Module module = getModule(data.moduleId, null, service);
        if (module.getModuleType() != ModuleType.Deployable
                && module.getModuleType() != ModuleType.Simulator) {
            throw new Http400BadRequestException("Module must be a deployable or simulator");
        }

        int creationCount = 0;
        String[] hostnames = data.hosts.split(",");
        for (String hostname : hostnames) {
            String scrubedHostName = scrubHostname(hostname);
            if (scrubedHostName != null && !scrubedHostName.isEmpty()) {
                if (checkHostAlreadyExists(scrubedHostName)) {
                    doBackfill(scrubedHostName, service, module, data, user);
                    creationCount++;
                }
            }
        }

        if (creationCount == 0) {
            throw new Http400BadRequestException("The listed hosts already exist");
        }

        response.setStatus(200);
        request.setHandled(true);
    }

    private void doBackfill(String scrubedHostName, Service service, Module module, PostBackfillHostData data, User user) {
        Host host = new Host(
                scrubedHostName,
                service.getServiceId(),
                module.getModuleId(),
                data.dataCenter,
                data.environment,
                data.platform);

        getDataAccess().saveHost(host);
        getDataAccess().insertSearch(
                Const.SEARCH_SPACE_HOST_NAME,
                scrubedHostName,
                service.getServiceId(),
                module.getModuleId(),
                host.getHostId());
        getDataAccess().updateStatus(
                host.getHostId(),
                false,
                "Backfilled %% ago",
                Const.STATUS_INFO);

        Audit audit = new Audit();
        audit.serviceId = service.getServiceId();
        audit.setTimePerformed(GMT.getGmtAsDate());
        audit.timeRequested = GMT.getGmtAsDate();
        audit.requestor = user.getUsername();
        audit.type = Type.host;
        audit.operation = Operation.create;
        audit.successfull = true;
        audit.moduleName = module.getModuleName();
        audit.hostName = scrubedHostName;

        Map<String, String> notes = new HashMap<>();
        notes.put("Reason", "Backfilled host.");
        notes.put("DC", data.dataCenter);
        notes.put("Environment", data.environment);
        notes.put("Plaform", data.platform);
        audit.notes = getGson().toJson(notes);

        getDataAccess().saveAudit(audit, null);
    }

    private boolean checkHostAlreadyExists(String hostname) {
        SearchResult searchResult = getDataAccess().doSearch(
                Const.SEARCH_SPACE_HOST_NAME,
                hostname);
        if (searchResult != null) {
            LOGGER.warn("Could not backfill host {} becuase it already exists, {}", hostname, searchResult.hostId);
            return false;
        }
        
        if (parameter.getBoolean(Const.CHECK_RESOLVE_HOSTNAME, Const.CHECK_RESOLVE_HOSTNAME_DEFAULT)) {
            try {
                InetAddress address = InetAddress.getByName(hostname);
                LOGGER.info("Backfill host {} resolves to IP address {}", hostname, address.getHostAddress());
            } catch (UnknownHostException ex) {
                LOGGER.warn("Could not backfill host {} becuase the hostname does not resolve to an IP address", hostname, searchResult.hostId);
                return false;
            }
        }
        return true;
    }

}
