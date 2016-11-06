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

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.google.gson.Gson;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleType;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.handlers.utility.HealthWriter;
import com.northernwall.hadrian.messaging.MessageType;
import com.northernwall.hadrian.messaging.MessagingCoodinator;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.workItem.action.HostSmokeTestAction;
import com.northernwall.hadrian.workItem.dao.SmokeTestData;
import com.squareup.okhttp.OkHttpClient;
import java.io.IOException;
import java.time.ZonedDateTime;
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
public class ScheduleRunner implements Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(ScheduleRunner.class);
    private final static CronParser CRON_PARSER = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
    
    public static Cron parseCron(String cronExpression) {
        return CRON_PARSER.parse(cronExpression);
    }

    private final int group;
    private final DataAccess dataAccess;
    private final Leader leader;
    private final Parameters parameters;
    private final Gson gson;
    private final OkHttpClient client;
    private final MessagingCoodinator messagingCoodinator;
    private ZonedDateTime lastChecked;

    public ScheduleRunner(int group, DataAccess dataAccess, Leader leader, Parameters parameters, Gson gson, OkHttpClient client, MessagingCoodinator messagingCoodinator) {
        this.group = group;
        this.dataAccess = dataAccess;
        this.leader = leader;
        this.parameters = parameters;
        this.gson = gson;
        this.client = client;
        this.messagingCoodinator = messagingCoodinator;
        this.lastChecked = ZonedDateTime.now();
    }

    @Override
    public void run() {
        ZonedDateTime now = ZonedDateTime.now();
        try {
            if (leader.isLeader(group)) {
                int serviceCount = 0;
                int smokeTestCount = 0;
                List<Service> services = dataAccess.getActiveServices();
                for (Service service : services) {
                    int serviceGroup = service.getServiceId().hashCode() % Scheduler.GROUP_COUNT;
                    if (serviceGroup == group) {
                        serviceCount++;
                        if (checkCron(service, now)) {
                            smokeTestCount++;
                            runSmokeTest(service);
                        }
                        runCollectionMetrics(service);
                    }
                }
                LOGGER.info("Run schedule for group {}, service count {}, smoke test count {}", group, serviceCount, smokeTestCount);
            }
        } catch (Exception e) {
            LOGGER.error("Exception during running group {}, {}", group, e.getMessage());
        }
        lastChecked = now;
    }
    

    private boolean checkCron(Service service, ZonedDateTime now) {
        try {
            String cronExpression = service.getSmokeTestCron();
            if (cronExpression == null || cronExpression.isEmpty()) {
                return false;
            }
            Cron cron = parseCron(cronExpression);
            ExecutionTime executionTime = ExecutionTime.forCron(cron);
            ZonedDateTime last = executionTime.lastExecution(now);
            return (last.isAfter(lastChecked) && last.isBefore(now));
        } catch (Exception e) {
            LOGGER.error("CheckCron exception, {}", e.getMessage());
            return false;
        }
    }
    
    private void runSmokeTest(Service service) {
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
        if (failedHosts != null && !failedHosts.isEmpty()) {
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

    private void runCollectionMetrics(Service service) {
    }

    public void getHealth(HealthWriter writer) throws IOException {
        if (leader.isLeader(group)) {
            writer.addStringLine("Schedule Runner " + group, "Leader");
        } else {
            writer.addStringLine("Schedule Runner " + group, "Not leader");
        }
    }

}
