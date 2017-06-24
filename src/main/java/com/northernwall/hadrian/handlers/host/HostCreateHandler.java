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
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.db.SearchResult;
import com.northernwall.hadrian.config.Config;
import com.northernwall.hadrian.db.SearchSpace;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleType;
import com.northernwall.hadrian.domain.Environment;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import com.northernwall.hadrian.handlers.host.dao.PostHostData;
import com.northernwall.hadrian.handlers.routing.Http400BadRequestException;
import java.io.IOException;
import java.util.ArrayList;
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
public class HostCreateHandler extends BasicHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(HostCreateHandler.class);

    private final AccessHelper accessHelper;
    private final ConfigHelper configHelper;
    private final WorkItemProcessor workItemProcessor;

    public HostCreateHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper, ConfigHelper configHelper, WorkItemProcessor workItemProcessor) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
        this.configHelper = configHelper;
        this.workItemProcessor = workItemProcessor;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostHostData data = fromJson(request, PostHostData.class);
        Service service = getService(data.serviceId, null);
        Team team = getTeam(service.getTeamId(), null);
        User user = accessHelper.checkIfUserCanModify(request, team, "add a host");

        Config config = configHelper.getConfig();

        if (!config.environmentNames.contains(data.environment)) {
            throw new Http400BadRequestException("Unknown environment");
        }

        Module module = getModule(data.moduleId, null, service);
        if (module.getModuleType() == ModuleType.Library) {
            throw new Http400BadRequestException("Module must be a deployable or simulator");
        }

        for (Map.Entry<String, Integer> entry : data.counts.entrySet()) {
            int count = entry.getValue();
            String dataCenter = entry.getKey();

            if (config.dataCenters.contains(dataCenter) && count > 0) {
                checkRange(count, 1, config.maxCount, "host count");

                String prefix = buildPrefix(data.environment, config, dataCenter, module.getHostAbbr());
                int num = 1;
                int createdCount = 0;

                while (createdCount < count && num <= config.maxTotalCount) {
                    String hostName = buildHostName(prefix, num);
                    num++;
                    List<SearchResult> searchResults = getDataAccess().doSearchList(
                            SearchSpace.hostName,
                            hostName);
                    if (searchResults == null || searchResults.isEmpty()) {
                        createdCount++;
                        LOGGER.info("Building host {} - {}/{}", hostName, createdCount, count);
                        doCreateHost(hostName, data, dataCenter, user, team, service, module);
                    }
                }
            }
        }

        response.setStatus(200);
        request.setHandled(true);
    }

    private void doCreateHost(String hostName, PostHostData data, String dataCenter, User user, Team team, Service service, Module module) throws IOException {
        Host host = new Host(
                hostName,
                data.serviceId,
                data.moduleId,
                dataCenter,
                data.environment);
        getDataAccess().saveHost(host);
        getDataAccess().insertSearch(
                SearchSpace.hostName,
                hostName,
                host.getHostId(),
                service.getTeamId(),
                data.serviceId,
                data.moduleId,
                host.getHostId(),
                null);
        getDataAccess().updateStatus(
                host.getHostId(),
                true,
                "Creating...",
                Const.STATUS_WIP);

        List<WorkItem> workItems = new ArrayList<>(3);

        WorkItem workItemCreate = new WorkItem(Type.host, Operation.create, user, team, service, module, host, null);
        workItemCreate.setSpecialInstructions(data.specialInstructions);
        workItemCreate.setReason(data.reason);
        workItemCreate.getHost().version = data.version;
        workItemCreate.getHost().configVersion = data.configVersion;
        workItems.add(workItemCreate);

        if (service.isDoManageVip()) {
            WorkItem workItemEnable = new WorkItem(Type.host, Operation.addVips, user, team, service, module, host, null);
            workItems.add(workItemEnable);
        }

        if (service.isDoDeploys()) {
            WorkItem workItemDeploy = new WorkItem(Type.host, Operation.deploy, user, team, service, module, host, null);
            workItemDeploy.setReason(data.reason);
            workItemDeploy.getHost().version = data.version;
            workItemDeploy.getHost().configVersion = data.configVersion;
            workItems.add(workItemDeploy);
        }

        WorkItem workItemStatus = new WorkItem(Type.host, Operation.status, user, team, service, module, host, null);
        workItemStatus.setReason("Provisioned %% ago");
        workItems.add(workItemStatus);

        workItemProcessor.processWorkItems(workItems);
    }

    private void checkRange(int value, int min, int max, String text) throws Http400BadRequestException {
        if (value < min) {
            throw new Http400BadRequestException("Requested " + text + " is less than allowed");
        }
        if (value > max) {
            throw new Http400BadRequestException("Requested " + text + " is greater than allowed");
        }
    }

    private String buildPrefix(String environmentName, Config config, String dataCenter, String abbr) {
        for (Environment environment : config.environments) {
            if (environment.name.equals(environmentName)) {
                String prefix = environment.pattern;
                prefix = prefix.replace(Const.CONFIG_ENVIRONMENTS_DC, dataCenter);
                prefix = prefix.replace(Const.CONFIG_ENVIRONMENTS_ABBR, abbr);
                return prefix;
            }
        }
        throw new Http400BadRequestException("Unknown environment");
    }

    private String buildHostName(String prefix, int num) {
        String numStr = Integer.toString(num);
        numStr = "000".substring(numStr.length()) + numStr;
        return prefix + numStr;
    }

}
