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

import com.northernwall.hadrian.ConfigHelper;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.GMT;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Audit;
import com.northernwall.hadrian.domain.Config;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleType;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.service.dao.PostBackfillHostData;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http400BadRequestException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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

    private final static Logger logger = LoggerFactory.getLogger(HostBackfillHandler.class);

    private final AccessHelper accessHelper;
    private final ConfigHelper configHelper;

    public HostBackfillHandler(AccessHelper accessHelper, ConfigHelper configHelper, DataAccess dataAccess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
        this.configHelper = configHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostBackfillHostData data = fromJson(request, PostBackfillHostData.class);
        Service service = getService(data.serviceId, null, null);
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "backfill host");

        Config config = configHelper.getConfig();
        if (!config.dataCenters.contains(data.dataCenter)) {
            throw new Http400BadRequestException("unknown datacenter, " + data.dataCenter);
        }
        if (!config.networkNames.contains(data.network)) {
            throw new Http400BadRequestException("unknown network, " + data.network);
        }
        if (!config.envs.contains(data.env)) {
            throw new Http400BadRequestException("unknown env, " + data.env);
        }
        if (!config.sizes.contains(data.size)) {
            throw new Http400BadRequestException("unknown size, " + data.size);
        }

        Module module = getModule(data.moduleId, null, service);
        if (module.getModuleType() != ModuleType.Deployable) {
            throw new Http400BadRequestException("Module must be a deployable");
        }

        List<Host> hosts = getDataAccess().getHosts(service.getServiceId());

        String[] hostnames = data.hosts.split(",");
        for (String hostname : hostnames) {
            if (hostname != null && !hostname.isEmpty()) {
                String temp = hostname.trim();
                if (temp != null && !temp.isEmpty()) {
                    boolean found = false;
                    for (Host host : hosts) {
                        if (host.getHostName().equalsIgnoreCase(temp)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        Host host = new Host(temp,
                                service.getServiceId(),
                                Const.NO_STATUS,
                                module.getModuleId(),
                                data.dataCenter,
                                data.network,
                                data.env,
                                data.size);
                        getDataAccess().saveHost(host);

                        Audit audit = new Audit();
                        audit.serviceId = service.getServiceId();
                        audit.timePerformed = GMT.getGmtAsDate();
                        audit.timeRequested = GMT.getGmtAsDate();
                        audit.requestor = user.getUsername();
                        audit.type = Type.host;
                        audit.operation = Operation.create;
                        audit.moduleName = module.getModuleName();
                        audit.hostName = temp;
                        Map<String, String> notes = new HashMap<>();
                        notes.put("reason", "Backfilled host.");
                        audit.notes = getGson().toJson(notes);
                        getDataAccess().saveAudit(audit, null);
                    }
                }
            }
        }

        response.setStatus(200);
        request.setHandled(true);
    }

}
