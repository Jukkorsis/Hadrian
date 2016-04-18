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
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Audit;
import com.northernwall.hadrian.domain.Config;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
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
        User user = accessHelper.checkIfUserIsOps(request, "Backfill");
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String s = reader.readLine();
        while (s != null && !s.isEmpty()) {
            String[] parts = s.split(",");
            if (parts.length == 7) {
                backfillHost(
                        parts[0].trim(),
                        parts[1].trim(),
                        parts[2].trim(),
                        parts[3].trim(),
                        parts[4].trim(),
                        parts[5].trim(),
                        parts[6].trim(),
                        user);
            }
            s = reader.readLine();
        }
        response.setStatus(200);
        request.setHandled(true);
    }

    private void backfillHost(String serviceAbbr, String moduleName, String hostName, String dataCenter, String network, String env, String size, User user) {
        Config config = configHelper.getConfig();
        if (config.dataCenters.contains(dataCenter)
                && config.networkNames.contains(network)
                && config.envs.contains(env)
                && config.sizes.contains(size)) {
            for (Service service : getDataAccess().getServices()) {
                if (service.getServiceAbbr().equalsIgnoreCase(serviceAbbr)) {
                    List<Host> hosts = getDataAccess().getHosts(service.getServiceId());
                    for (Host host : hosts) {
                        if (host.getHostName().equalsIgnoreCase(hostName)) {
                            logger.warn("There already exists host '{}' on service '{}'", hostName, serviceAbbr);
                            return;
                        }
                    }
                    Module module = null;
                    List<Module> modules = getDataAccess().getModules(service.getServiceId());
                    for (Module temp : modules) {
                        if (temp.getModuleName().equalsIgnoreCase(moduleName)) {
                            module = temp;
                        }
                    };
                    if (module == null) {
                        logger.warn("Could not find module with name {} in service {}", moduleName, serviceAbbr);
                        return;
                    }
                    Host host = new Host(hostName,
                            service.getServiceId(),
                            Const.NO_STATUS,
                            module.getModuleId(),
                            dataCenter,
                            network,
                            env,
                            size);
                    getDataAccess().saveHost(host);

                    Audit audit = new Audit();
                    audit.serviceId = service.getServiceId();
                    audit.timePerformed = getGmt();
                    audit.timeRequested = getGmt();
                    audit.requestor = user.getUsername();
                    audit.type = Type.host;
                    audit.operation = Operation.create;
                    audit.moduleName = module.getModuleName();
                    audit.hostName = hostName;
                    Map<String, String> notes = new HashMap<>();
                    notes.put("reason", "Backfill via OPS tool.");
                    audit.notes = getGson().toJson(notes);
                    getDataAccess().saveAudit(audit, null);

                    return;
                }
            }
            logger.warn("Could not find a service with the abbr '{}'", serviceAbbr);
        }
    }

}
