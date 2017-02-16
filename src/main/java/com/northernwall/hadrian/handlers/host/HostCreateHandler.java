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
import com.northernwall.hadrian.ConfigHelper;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.db.SearchResult;
import com.northernwall.hadrian.domain.Config;
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

        checkRange(data.count, 1, config.maxCount, "host count");
        checkRange(data.sizeCpu, config.minCpu, config.maxCpu, "CPU size");
        checkRange(data.sizeMemory, config.minMemory, config.maxMemory, "memory size");
        checkRange(data.sizeStorage, config.minStorage, config.maxStorage, "storage size");

        if (!config.dataCenters.contains(data.dataCenter)) {
            throw new Http400BadRequestException("Unknown data center");
        }
        if (!config.environmentNames.contains(data.environment)) {
            throw new Http400BadRequestException("Unknown environment");
        }
        if (!config.platforms.contains(data.platform)) {
            throw new Http400BadRequestException("Unknown operating platform");
        }

        Module module = getModule(data.moduleId, null, service);
        if (module.getModuleType() == ModuleType.Library) {
            throw new Http400BadRequestException("Module must be a deployable or simulator");
        }

        String prefix = buildPrefix(data.environment, config, data.dataCenter, module.getHostAbbr());
        int num = 1;
        int createdCount = 0;
        while (createdCount < data.count && num <= config.maxTotalCount) {
            String hostName = buildHostName(prefix, num);
            num++;
            SearchResult searchResult = getDataAccess().doSearch(
                    Const.SEARCH_SPACE_HOST_NAME,
                    hostName);
            if (searchResult == null) {
                createdCount++;
                LOGGER.info("Building host {} - {}/{}", hostName, createdCount, data.count);
                doCreateHost(hostName, data, user, team, service, module);
            }
        }

        if (createdCount == 0) {
            throw new Http400BadRequestException("Could not create any hosts, max host count per data center reached");
        }

        response.setStatus(200);
        request.setHandled(true);
    }

    private void doCreateHost(String hostName, PostHostData data, User user, Team team, Service service, Module module) throws IOException {
        Host host = new Host(
                hostName,
                data.serviceId,
                data.moduleId,
                data.dataCenter,
                data.environment,
                data.platform);
        getDataAccess().saveHost(host);
        getDataAccess().insertSearch(
                Const.SEARCH_SPACE_HOST_NAME,
                hostName,
                data.serviceId,
                data.moduleId,
                host.getHostId());
        getDataAccess().updateSatus(
                host.getHostId(),
                true,
                "Creating...");
        
        if (data.specialInstructions != null 
                && !data.specialInstructions.isEmpty()) {
            LOGGER.info("Host creation with special instructions. {}", data.specialInstructions);
        }

        List<WorkItem> workItems = new ArrayList<>(3);

        WorkItem workItemCreate = new WorkItem(Type.host, Operation.create, user, team, service, module, host, null);
        workItemCreate.setSpecialInstructions(data.specialInstructions);
        workItemCreate.setReason(data.reason);
        workItemCreate.getHost().sizeCpu = data.sizeCpu;
        workItemCreate.getHost().sizeMemory = data.sizeMemory;
        workItemCreate.getHost().sizeStorage = data.sizeStorage;
        workItemCreate.getHost().version = data.version;
        workItemCreate.getHost().configVersion = data.configVersion;
        workItems.add(workItemCreate);

        if (service.isDoDeploys()) {
            WorkItem workItemDeploy = new WorkItem(Type.host, Operation.deploy, user, team, service, module, host, null);
            workItemDeploy.setReason(data.reason);
            workItemDeploy.getHost().version = data.version;
            workItemDeploy.getHost().configVersion = data.configVersion;
            workItems.add(workItemDeploy);
        }

        if (service.isDoManageVip()) {
            WorkItem workItemEnable = new WorkItem(Type.host, Operation.addVips, user, team, service, module, host, null);
            workItems.add(workItemEnable);
        }

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
