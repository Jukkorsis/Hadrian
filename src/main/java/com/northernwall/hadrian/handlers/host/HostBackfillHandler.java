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
import com.northernwall.hadrian.db.SearchSpace;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;
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
    private final Parameters parameters;

    public HostBackfillHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper, ConfigHelper configHelper, Parameters parameters) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
        this.configHelper = configHelper;
        this.parameters = parameters;
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
        List<String> scrubedHosts = new LinkedList<>();
        for (String hostname : hostnames) {
            String scrubedHostName = scrubHostname(hostname);
            if (scrubedHostName != null && !scrubedHostName.isEmpty()) {
                validateHostname(module, scrubedHostName);
                scrubedHosts.add(scrubedHostName);
            }
        }
        for (String hostname : scrubedHosts) {
            doBackfill(hostname, service, module, data, user);
            creationCount++;
        }

        if (creationCount == 0) {
            throw new Http400BadRequestException("No hosts added");
        }

        response.setStatus(200);
        request.setHandled(true);
    }

    private void validateHostname(Module module, String hostname) {
        String pattern = parameters.getString(Const.CHECK_HOSTNAME_PATTERN, null);
        if (pattern != null && !pattern.isEmpty()) {
            try {
                if (!hostname.matches(pattern)) {
                    String whiteList = parameters.getString(Const.CHECK_HOSTNAME_WHITELIST, null);
                    boolean found = false;
                    if (whiteList != null && !whiteList.isEmpty()) {
                        String[] parts = whiteList.split(",");
                        for (String part : parts) {
                            if (hostname.equalsIgnoreCase(part)) {
                                found = true;
                            }
                        }
                    }
                    if (!found) {
                        throw new Http400BadRequestException(hostname + " does not match " + pattern + " or whitelist.");
                    }
                }
            } catch (PatternSyntaxException ex) {
                LOGGER.error("Match pattern '{}' is illegal, {}", pattern, ex.getMessage());
                throw new RuntimeException("Internal pattern match failure, check pattern");
            }
        }

        List<SearchResult> searchResults = getDataAccess().doSearchList(
                SearchSpace.hostName,
                hostname);
        if (searchResults != null && !searchResults.isEmpty()) {
            for (SearchResult searchResult : searchResults) {
                if (module.getModuleId().equals(searchResult.moduleId)) {
                    LOGGER.warn("Could not backfill host {} becuase it already exists in {}", hostname, module.getModuleName());
                    throw new Http400BadRequestException(hostname + " is already associated to " + module.getModuleName());
                }
            }
        }

        if (parameters.getBoolean(Const.CHECK_RESOLVE_HOSTNAME, Const.CHECK_RESOLVE_HOSTNAME_DEFAULT)) {
            try {
                InetAddress address = InetAddress.getByName(hostname);
                LOGGER.info("Backfill host {} resolves to IP address {}", hostname, address.getHostAddress());
            } catch (UnknownHostException ex) {
                LOGGER.warn("Could not backfill host {} becuase the hostname does not resolve to an IP address", hostname);
                throw new Http400BadRequestException("Could not resolve IP address for " + hostname + ", check if host exists");
            }
        }
    }

    private void doBackfill(String scrubedHostName, Service service, Module module, PostBackfillHostData data, User user) {
        Host host = new Host(
                scrubedHostName,
                service.getServiceId(),
                module.getModuleId(),
                data.dataCenter,
                data.environment);

        getDataAccess().saveHost(host);
        getDataAccess().insertSearch(
                SearchSpace.hostName,
                scrubedHostName,
                host.getHostId(),
                service.getTeamId(),
                service.getServiceId(),
                module.getModuleId(),
                host.getHostId(),
                null);
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
        audit.operation = Operation.backfill;
        audit.successfull = true;
        audit.moduleName = module.getModuleName();
        audit.hostName = scrubedHostName;

        Map<String, String> notes = new HashMap<>();
        notes.put("Reason", "Backfilled host.");
        notes.put("DC", data.dataCenter);
        notes.put("Environment", data.environment);
        audit.notes = getGson().toJson(notes);

        getDataAccess().saveAudit(audit, null);
    }

}
