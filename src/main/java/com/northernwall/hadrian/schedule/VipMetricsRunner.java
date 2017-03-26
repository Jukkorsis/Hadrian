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
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Vip;
import java.util.LinkedList;
import java.util.List;
import org.dshops.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard
 */
public class VipMetricsRunner implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(VipMetricsRunner.class);

    private final Service service;
    private final int group;
    private final DataAccess dataAccess;
    private final MetricRegistry metricRegistry;

    public VipMetricsRunner(Service service, int group, DataAccess dataAccess, MetricRegistry metricRegistry) {
        this.service = service;
        this.group = group;
        this.dataAccess = dataAccess;
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void run() {
        LOGGER.info("Running scheduled metrics for {} in group {}", service.getServiceName(), group);

        List<VipCounter> vipCounters = new LinkedList<>();
        List<Vip> vips = dataAccess.getVips(service.getServiceId());
        Team team = dataAccess.getTeam(service.getTeamId());

        if (vips != null && !vips.isEmpty()) {
            for (Vip vip : vips) {
                VipCounter vipCounter = find(vipCounters, vip);
                vipCounter.count++;
            }
        }

        if (!vipCounters.isEmpty()) {
            for (VipCounter vipCounter : vipCounters) {
                metricRegistry.event(
                        "vip-count", 
                        vipCounter.count, 
                        "metricTeam",
                        team.getTeamName(),
                        "metricService",
                        service.getServiceName(),
                        "metricEnvironment",
                        vipCounter.environment,
                        "metricMigration",
                        Integer.toString(vipCounter.migration));
            }
        }
    }

    private VipCounter find(List<VipCounter> vipCounters, Vip vip) {
        if (!vipCounters.isEmpty()) {
            for (VipCounter vipCounter : vipCounters) {
                if (vipCounter.environment.equals(vip.getEnvironment())
                        && vipCounter.migration == vip.getMigration()) {
                    return vipCounter;
                }
            }
        }

        VipCounter vipCounter = new VipCounter();
        vipCounter.environment = vip.getEnvironment();
        vipCounter.migration = vip.getMigration();
        vipCounters.add(vipCounter);
        return vipCounter;
    }

}
