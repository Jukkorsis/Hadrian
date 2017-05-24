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
import java.util.LinkedList;
import java.util.List;
import org.dshops.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard
 */
public class HostMetricsRunner implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(HostMetricsRunner.class);

    private final Service service;
    private final int group;
    private final DataAccess dataAccess;
    private final MetricRegistry metricRegistry;

    public HostMetricsRunner(Service service, int group, DataAccess dataAccess, MetricRegistry metricRegistry) {
        this.service = service;
        this.group = group;
        this.dataAccess = dataAccess;
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void run() {
        LOGGER.info("Running scheduled metrics for {} in group {}", service.getServiceName(), group);

        List<HostCounter> hostCounters = new LinkedList<>();
        List<Host> hosts = dataAccess.getHosts(service.getServiceId());
        Team team = dataAccess.getTeam(service.getTeamId());

        if (hosts != null && !hosts.isEmpty()) {
            for (Host host : hosts) {
                HostCounter hostCounter = find(hostCounters, host);
                hostCounter.count++;
            }
        }

        if (!hostCounters.isEmpty()) {
            for (HostCounter hostCounter : hostCounters) {
                metricRegistry.event(
                        "host-count", 
                        hostCounter.count, 
                        "metricTeam",
                        team.getTeamName(),
                        "metricService",
                        service.getServiceName(),
                        "metricDataCenter",
                        hostCounter.dataCenter,
                        "metricEnvironment",
                        hostCounter.environment);
            }
        }
    }

    private HostCounter find(List<HostCounter> hostCounters, Host host) {
        if (!hostCounters.isEmpty()) {
            for (HostCounter hostCounter : hostCounters) {
                if (hostCounter.dataCenter.equals(host.getDataCenter())
                        && hostCounter.environment.equals(host.getEnvironment())) {
                    return hostCounter;
                }
            }
        }

        HostCounter hostCounter = new HostCounter();
        hostCounter.dataCenter = host.getDataCenter();
        hostCounter.environment = host.getEnvironment();
        hostCounters.add(hostCounter);
        return hostCounter;
    }

}
