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
import com.northernwall.hadrian.handlers.utility.HealthWriter;
import com.northernwall.hadrian.messaging.MessagingCoodinator;
import com.northernwall.hadrian.parameters.Parameters;
import com.squareup.okhttp.OkHttpClient;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.dshops.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard
 */
public class Scheduler {
    private final static Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
    public static final int THREAD_COUNT = 25;
    public static final int GROUP_COUNT = 10;
    
    private final Leader leader;
    private final ScheduledExecutorService scheduledExecutorService;
    private final List<ScheduleRunner> runners;

    public Scheduler(DataAccess dataAccess, MetricRegistry metricRegistry, Leader leader, Parameters parameters, OkHttpClient client, MessagingCoodinator messagingCoodinator) {
        Gson gson = new Gson();
        
        this.leader = leader;
        scheduledExecutorService = Executors.newScheduledThreadPool(THREAD_COUNT);
        runners = new LinkedList<>();
        
        for (int group=0; group<GROUP_COUNT; group++) {
            ScheduleRunner runner = new ScheduleRunner(
                    group, 
                    dataAccess, 
                    metricRegistry,
                    leader, 
                    parameters, 
                    gson, 
                    client,
                    messagingCoodinator,
                    scheduledExecutorService);
            
            runners.add(runner);
            
            scheduledExecutorService.scheduleWithFixedDelay(
                    runner, 
                    5, 
                    5, 
                    TimeUnit.MINUTES);
        }
        LOGGER.info("Scheduler started with {} groups and using {}", GROUP_COUNT, leader.getClass().getSimpleName());
    }

    public void getHealth(HealthWriter writer) throws IOException {
        writer.addStringLine("Scheduler Leader", leader.getClass().getSimpleName());
        for (ScheduleRunner runner : runners) {
            runner.getHealth(writer);
        }
    }
    
}
