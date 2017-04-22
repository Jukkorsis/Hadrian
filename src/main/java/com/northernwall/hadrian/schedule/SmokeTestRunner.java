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

import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.messaging.MessageType;
import com.northernwall.hadrian.messaging.MessagingCoodinator;
import com.northernwall.hadrian.workItem.dao.SmokeTestData;
import com.northernwall.hadrian.workItem.helper.SmokeTestHelper;
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
    private final Module module;
    private final int group;
    private final DataAccess dataAccess;
    private final SmokeTestHelper smokeTestHelper;
    private final MessagingCoodinator messagingCoodinator;

    public SmokeTestRunner(Service service, Module module, int group, DataAccess dataAccess, SmokeTestHelper smokeTestHelper, MessagingCoodinator messagingCoodinator) {
        this.service = service;
        this.module = module;
        this.group = group;
        this.dataAccess = dataAccess;
        this.smokeTestHelper = smokeTestHelper;
        this.messagingCoodinator = messagingCoodinator;
    }

    @Override
    public void run() {
        LOGGER.info("Running scheduled smoke test for {} {} in group {}", service.getServiceName(), module.getModuleName(), group);
        List<Host> hosts = dataAccess.getHosts(service.getServiceId());
        List<Host> failedHosts = new LinkedList<>();
        if (hosts != null && !hosts.isEmpty()) {
            for (Host host : hosts) {
                if (host.getModuleId().equals(module.getModuleId())) {
                    SmokeTestData smokeTestData = smokeTestHelper.ExecuteSmokeTest(
                            module.getSmokeTestUrl(),
                            host.getHostName(),
                            service.getServiceName(),
                            "cron");
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
        if (!failedHosts.isEmpty()) {
            Team team = dataAccess.getTeam(service.getTeamId());
            messagingCoodinator.sendMessage("Smoke test failed on hosts " + JoinHosts(failedHosts), team);
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
                for (int i = 0; i < (size - 1); i++) {
                    hostNames = hostNames + failedHosts.get(0).getHostName() + ", ";
                }
                hostNames = hostNames + " and " + failedHosts.get(size - 1).getHostName();
                break;
        }
        return hostNames;
    }

}
