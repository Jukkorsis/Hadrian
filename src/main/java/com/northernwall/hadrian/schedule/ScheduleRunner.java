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
import com.northernwall.hadrian.handlers.utility.HealthWriter;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.workItem.action.HostSmokeTestAction;
import com.northernwall.hadrian.workItem.dao.SmokeTestData;
import com.squareup.okhttp.OkHttpClient;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard
 */
public class ScheduleRunner implements Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(ScheduleRunner.class);
    
    private final int group;
    private final DataAccess dataAccess;
    private final Leader leader;
    private final Parameters parameters;
    private final Gson gson;
    private final OkHttpClient client;

    public ScheduleRunner(int group, DataAccess dataAccess, Leader leader, Parameters parameters, Gson gson, OkHttpClient client) {
        this.group = group;
        this.dataAccess = dataAccess;
        this.leader = leader;
        this.parameters = parameters;
        this.gson = gson;
        this.client = client;
    }

    @Override
    public void run() {
        if (leader.isLeader(group)) {
            LOGGER.info("Running Schedule for group {}", group);
            List<Service> services = dataAccess.getActiveServices();
            for (Service service : services) {
                int serviceGroup = service.getServiceId().hashCode() % Scheduler.GROUP_COUNT;
                if (serviceGroup == group) {
                    if (checkCron(service)) {
                        runSmokeTest(service);
                    }
                    runCollectionMetrics(service);
                }
            }
        }
    }

    private boolean checkCron(Service service) {
        String cron = service.getSmokeTestCron();
        if (cron == null || cron.isEmpty()) {
            return false;
        }
        return false;
    }
    
    private void runSmokeTest(Service service) {
        LOGGER.info("Running scheduled smoke test for {} in group {}", service.getServiceName(), group);
        List<Module> modules = dataAccess.getModules(service.getServiceId());
        if (modules == null || modules.isEmpty()) {
            return;
        }
        List<Host> hosts = null;
        for (Module module : modules) {
            if (module.getModuleType() == ModuleType.Deployable) {
                LOGGER.info("Running scheduled smoke test for {} {} in group {}", service.getServiceName(), module.getModuleName(), group);
                String smokeTestUrl = module.getSmokeTestUrl();
                if (smokeTestUrl != null && !smokeTestUrl.isEmpty()) {
                    if (hosts == null) {
                        hosts = dataAccess.getHosts(service.getServiceId());
                    }
                    if (hosts != null && !hosts.isEmpty()) {
                        for (Host host : hosts) {
                            SmokeTestData results = HostSmokeTestAction.ExecuteSmokeTest(
                                    smokeTestUrl, 
                                    host.getHostName(), 
                                    parameters, 
                                    gson, 
                                    client);
                            if (!results.result.equalsIgnoreCase("pass")) {
                                LOGGER.info("Scheduled smoke test for {} in group {} failed", service.getServiceName(), group);
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

    private void runCollectionMetrics(Service service) {
    }

    public void getHealth(HealthWriter writer) throws IOException {
        if (leader.isLeader(group)) {
            writer.addLine("Schedule Runner " + group, "Leader");
        } else {
            writer.addLine("Schedule Runner " + group, "Not leader");
        }
    }

}
