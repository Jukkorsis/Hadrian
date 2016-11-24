/*
 * Copyright 2016 Richard Thurston.
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
package com.northernwall.hadrian.schedule;

import com.google.gson.Gson;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleType;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.messaging.MessageType;
import com.northernwall.hadrian.messaging.MessagingCoodinator;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.workItem.action.HostSmokeTestAction;
import com.northernwall.hadrian.workItem.dao.SmokeTestData;
import com.squareup.okhttp.OkHttpClient;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard
 */
public class SmokeTestRunner implements Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(SmokeTestRunner.class);
    
    private final Service service;
    private final int group;
    private final DataAccess dataAccess;
    private final Parameters parameters;
    private final Gson gson;
    private final OkHttpClient client;
    private final MessagingCoodinator messagingCoodinator;

    public SmokeTestRunner(Service service, int group, DataAccess dataAccess, Parameters parameters, Gson gson, OkHttpClient client, MessagingCoodinator messagingCoodinator) {
        this.service = service;
        this.group = group;
        this.dataAccess = dataAccess;
        this.parameters = parameters;
        this.gson = gson;
        this.client = client;
        this.messagingCoodinator = messagingCoodinator;
    }

    @Override
    public void run() {
        LOGGER.info("Running scheduled smoke test for {} in group {}", service.getServiceName(), group);
        List<Module> modules = dataAccess.getModules(service.getServiceId());
        if (modules == null || modules.isEmpty()) {
            return;
        }
        List<Host> hosts = null;
        List<Host> failedHosts = new LinkedList<>();
        for (Module module : modules) {
            if (module.getModuleType() == ModuleType.Deployable) {
                String smokeTestUrl = module.getSmokeTestUrl();
                if (smokeTestUrl != null && !smokeTestUrl.isEmpty()) {
                    LOGGER.info("Running scheduled smoke test for {} {} in group {}", service.getServiceName(), module.getModuleName(), group);
                    if (hosts == null) {
                        hosts = dataAccess.getHosts(service.getServiceId());
                    }
                    if (hosts != null && !hosts.isEmpty()) {
                        for (Host host : hosts) {
                            if (host.getModuleId().equals(module.getModuleId())) {
                                SmokeTestData smokeTestData = HostSmokeTestAction.ExecuteSmokeTest(
                                        smokeTestUrl, 
                                        host.getHostName(), 
                                        parameters, 
                                        gson, 
                                        client);
                                if (smokeTestData == null 
                                        || smokeTestData.result == null
                                        || smokeTestData.result.isEmpty()
                                        || !smokeTestData.result.equalsIgnoreCase("pass")) {
                                    LOGGER.info("Scheduled smoke test failed for {} in {} in group {}", 
                                            host.getHostName(), 
                                            service.getServiceName(), 
                                            group);
                                    failedHosts.add(host);
                                }
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException ex) {
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!failedHosts.isEmpty()) {
            MessageType messageType = messagingCoodinator.getMessageType("failedSmokeTest");
            if (messageType != null) {
                Map<String, String> data = new HashMap<>();
                data.put("count", Integer.toString(failedHosts.size()));
                data.put("host", JoinHosts(failedHosts));
                Team team = dataAccess.getTeam(service.getTeamId());
                messagingCoodinator.sendMessage(messageType, team, service, data);
            }
        }
    }

    private String JoinHosts(List<Host> failedHosts) {
        String hostNames = "";
        int size = failedHosts.size();
        switch (size) {
            case 1:
                hostNames = failedHosts.get(0).getHostName();
                break;
            case 2:
                hostNames = failedHosts.get(0).getHostName() + " and " + failedHosts.get(1).getHostName();
                break;
            default:
                for (int i=0;i<(size-1);i++) {
                    hostNames = hostNames + failedHosts.get(0).getHostName() + ", ";
                }   hostNames = hostNames + " and " + failedHosts.get(size-1).getHostName();
                break;
        }
        return hostNames;
    }

}
