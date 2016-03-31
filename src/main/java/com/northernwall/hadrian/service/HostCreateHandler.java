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
import com.northernwall.hadrian.domain.Config;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Network;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import com.northernwall.hadrian.service.dao.PostHostData;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http400BadRequestException;
import java.io.IOException;
import java.util.List;
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
public class HostCreateHandler extends BasicHandler {

    private final static Logger logger = LoggerFactory.getLogger(HostCreateHandler.class);

    private final AccessHelper accessHelper;
    private final ConfigHelper configHelper;
    private final WorkItemProcessor workItemProcess;

    public HostCreateHandler(AccessHelper accessHelper, ConfigHelper configHelper, DataAccess dataAccess, WorkItemProcessor workItemProcess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
        this.configHelper = configHelper;
        this.workItemProcess = workItemProcess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostHostData postHostData = fromJson(request, PostHostData.class);
        Service service = getService(postHostData.serviceId, null, null);
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "add a host");
        Team team = getDataAccess().getTeam(service.getTeamId());

        if (postHostData.count < 1) {
            throw new Http400BadRequestException("count must to at least 1");
        } else if (postHostData.count > 10) {
            logger.warn("Reducing count to 10, was {}", postHostData.count);
            postHostData.count = 10;
        }

        Config config = configHelper.getConfig();
        if (!config.dataCenters.contains(postHostData.dataCenter)) {
            throw new Http400BadRequestException("Unknown data center");
        }
        if (!config.networkNames.contains(postHostData.network)) {
            throw new Http400BadRequestException("Unknown network");
        }
        if (!config.envs.contains(postHostData.env)) {
            throw new Http400BadRequestException("Unknown env");
        }
        if (!config.sizes.contains(postHostData.size)) {
            throw new Http400BadRequestException("Unknown size");
        }

        List<Module> modules = getDataAccess().getModules(postHostData.serviceId);
        Module module = null;
        for (Module temp : modules) {
            if (temp.getModuleId().equals(postHostData.moduleId)) {
                module = temp;
            }
        }
        if (module == null) {
            throw new RuntimeException("Unknown module");
        }

        //calc host name
        String prefix = buildPrefix(postHostData.network, config, postHostData.dataCenter, module.getHostAbbr());
        int len = prefix.length();
        int num = 0;
        List<Host> hosts = getDataAccess().getHosts(postHostData.serviceId);
        for (Host existingHost : hosts) {
            String existingHostName = existingHost.getHostName();
            if (existingHostName.startsWith(prefix) && existingHostName.length() > len) {
                String numPart = existingHostName.substring(len);
                try {
                    int temp = Integer.parseInt(numPart);
                    if (temp > num) {
                        num = temp;
                    }
                } catch (Exception e) {
                    logger.warn("Error parsing int from last part of {}", existingHostName);
                }
            }
        }
        num++;
        for (int c = 0; c < postHostData.count; c++) {
            String numStr = Integer.toString(num + c);
            numStr = "000".substring(numStr.length()) + numStr;

            Host host = new Host(prefix + numStr,
                    postHostData.serviceId,
                    "Creating...",
                    postHostData.moduleId,
                    postHostData.dataCenter,
                    postHostData.network,
                    postHostData.env,
                    postHostData.size);
            getDataAccess().saveHost(host);

            WorkItem workItemCreate = new WorkItem(Type.host, Operation.create, user, team, service, module, host, null);
            WorkItem workItemDeploy = new WorkItem(Type.host, Operation.deploy, user, team, service, module, host, null);

            workItemCreate.getHost().version = postHostData.version;
            workItemCreate.getHost().reason = postHostData.reason;
            workItemCreate.setNextId(workItemDeploy.getId());
            workItemDeploy.getHost().version = postHostData.version;
            workItemDeploy.getHost().reason = postHostData.reason;

            getDataAccess().saveWorkItem(workItemCreate);
            getDataAccess().saveWorkItem(workItemDeploy);

            workItemProcess.sendWorkItem(workItemCreate);
        }

        response.setStatus(200);
        request.setHandled(true);
    }

    private String buildPrefix(String networkName, Config config, String dataCenter, String abbr) {
        for (Network network : config.networks) {
            if (network.name.equals(networkName)) {
                String prefix = network.pattern;
                prefix = prefix.replace(Const.CONFIG_NETWORKS_DC, dataCenter);
                prefix = prefix.replace(Const.CONFIG_NETWORKS_ABBR, abbr);
                return prefix;
            }
        }
        throw new Http400BadRequestException("Unknown network");
    }

}
