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
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleType;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.handlers.utility.HealthWriter;
import com.northernwall.hadrian.messaging.MessagingCoodinator;
import com.northernwall.hadrian.workItem.helper.SmokeTestHelper;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import org.dshops.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard
 */
public class ScheduleRunner implements Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(ScheduleRunner.class);
    private final static CronParser CRON_PARSER = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
    private final static String METRICS_CRON = "15 4 * * *";
    
    public static Cron parseCron(String cronExpression) {
        return CRON_PARSER.parse(cronExpression);
    }

    private final int group;
    private final DataAccess dataAccess;
    private final MetricRegistry metricRegistry;
    private final Leader leader;
    private final SmokeTestHelper smokeTestHelper;
    private final MessagingCoodinator messagingCoodinator;
    private final ScheduledExecutorService scheduledExecutorService;
    private ZonedDateTime prevChecked;

    public ScheduleRunner(int group, DataAccess dataAccess, MetricRegistry metricRegistry, Leader leader, SmokeTestHelper smokeTestHelper, MessagingCoodinator messagingCoodinator, ScheduledExecutorService scheduledExecutorService) {
        this.group = group;
        this.dataAccess = dataAccess;
        this.metricRegistry = metricRegistry;
        this.leader = leader;
        this.smokeTestHelper = smokeTestHelper;
        this.messagingCoodinator = messagingCoodinator;
        this.scheduledExecutorService = scheduledExecutorService;
        this.prevChecked = ZonedDateTime.now();
    }

    @Override
    public void run() {
        ZonedDateTime now = ZonedDateTime.now();
        
        boolean doMetrics = checkCron(METRICS_CRON, now);
        
        try {
            if (leader.isLeader(group)) {
                processGroup(now, doMetrics);
            }
        } catch (Exception e) {
            LOGGER.error("Exception during running group {}, {}", group, e.getMessage());
        } finally {
            prevChecked = now;
        }
    }

    private void processGroup(ZonedDateTime now, boolean doMetrics) {
        int serviceCount = 0;
        int smokeTestCount = 0;
        List<Service> services = dataAccess.getActiveServices();
        for (Service service : services) {
            int serviceGroup = Math.abs(service.getServiceId().hashCode() % Scheduler.GROUP_COUNT);
            if (serviceGroup == group) {
                serviceCount++;
                List<Module> modules = dataAccess.getModules(service.getServiceId());
                if (modules != null && !modules.isEmpty()) {
                    LOGGER.info("Processing {} with {} modules", service.getServiceName(), modules.size());
                    for (Module module : modules) {
                        LOGGER.debug("Processing {} {} with type {}", module.getModuleName(), service.getServiceName(), module.getModuleType());
                        if (module.getModuleType() == ModuleType.Deployable) {
                            String smokeTestCron = module.getSmokeTestCron();
                            String smokeTestUrl = module.getSmokeTestUrl();
                            LOGGER.debug("Processing {} {} with '{}' {}", module.getModuleName(), service.getServiceName(), smokeTestCron, smokeTestUrl);
                            if (smokeTestUrl != null
                                    && !smokeTestUrl.isEmpty()
                                    && smokeTestCron != null
                                    && !smokeTestCron.isEmpty()
                                    && checkCron(smokeTestCron, now)) {
                                smokeTestCount++;
                                scheduledExecutorService.submit(new SmokeTestRunner(service, module, group, dataAccess, smokeTestHelper, messagingCoodinator));
                            }
                        }
                    }
                }
                if (doMetrics) {
                    scheduledExecutorService.submit(new HostMetricsRunner(service, group, dataAccess, metricRegistry));
                    scheduledExecutorService.submit(new VipMetricsRunner(service, group, dataAccess, metricRegistry));
                }
            }
        }
        LOGGER.debug("Run schedule for group {}, service count {}, smoke test count {}", group, serviceCount, smokeTestCount);
    }

    private boolean checkCron(String cronExpression, ZonedDateTime now) {
        try {
            if (cronExpression == null || cronExpression.isEmpty()) {
                return false;
            }
            Cron cron = parseCron(cronExpression);
            ExecutionTime executionTime = ExecutionTime.forCron(cron);
            ZonedDateTime last = executionTime.lastExecution(now);
            boolean b1 = last.isAfter(prevChecked);
            boolean b2 = last.isBefore(now);
            LOGGER.info("cron={} now={} last={} prevCheck={} b1={} b2={}", cronExpression, now.toString(), last.toString(), prevChecked.toString(), b1, b2);
            return (b1 && b2);
            //return (last.isAfter(lastChecked) && last.isBefore(now));
        } catch (Exception e) {
            LOGGER.error("Check cron '{}' failed, {}", cronExpression, e.getMessage());
            return false;
        }
    }
    
    public void getHealth(HealthWriter writer) throws IOException {
        if (leader.isLeader(group)) {
            writer.addStringLine("Schedule Runner " + group, "Leader");
        } else {
            writer.addStringLine("Schedule Runner " + group, "Not leader");
        }
    }

}
