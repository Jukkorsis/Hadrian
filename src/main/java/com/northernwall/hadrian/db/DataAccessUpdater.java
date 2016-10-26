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
package com.northernwall.hadrian.db;

import com.northernwall.hadrian.domain.Config;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Vip;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataAccessUpdater {

    private final static Logger LOGGER = LoggerFactory.getLogger(DataAccessUpdater.class);

    public static void update(DataAccess dataAccess, Config config) {
        String version = dataAccess.getVersion();

        if (version == null || version.isEmpty()) {
            dataAccess.setVersion("1.7");
            LOGGER.info("New DB, initial version set.");
        } else if (version.equals("1.6")) {
            LOGGER.info("Current DB version is 1.6, update started.");
            scanForNetworkAndEnv(dataAccess, config);
            dataAccess.setVersion("1.7");
            LOGGER.info("DB update finished.");
        } else {
            LOGGER.info("Current DB version is {}, no upgrade required.", version);
        }
        fixHost(dataAccess);
    }

    private static void fixHost(DataAccess dataAccess) {
        int serviceCount = 0;
        int hostCount = 0;
        List<Service> services = dataAccess.getActiveServices();
        if (services != null && !services.isEmpty()) {
            for (Service service : services) {
                dataAccess.backfillService(service);
                serviceCount++;
                List<Host> hosts = dataAccess.getHosts(service.getServiceId());
                if (hosts != null && !hosts.isEmpty()) {
                    for (Host host : hosts) {
                        dataAccess.backfillHostName(host);
                        hostCount++;
                    }
                }
            }
        }
        LOGGER.info("Backfilled {} service, {} hosts", serviceCount, hostCount);
    }

    private static void scanForNetworkAndEnv(DataAccess dataAccess, Config config) {
        List<Service> services = dataAccess.getActiveServices();
        if (services != null && !services.isEmpty()) {
            for (Service service : services) {
                List<Module> modules = dataAccess.getModules(service.getServiceId());
                if (modules != null && !modules.isEmpty()) {
                    for (Module module : modules) {
                        module.setEnvironmentNames(module.networkNames);
                        dataAccess.saveModule(module);
                    }
                }
                List<Host> hosts = dataAccess.getHosts(service.getServiceId());
                if (hosts != null && !hosts.isEmpty()) {
                    for (Host host : hosts) {
                        host.setEnvironment(host.network);
                        host.setPlatform(host.env);
                        dataAccess.saveHost(host);
                    }
                }
                List<Vip> vips = dataAccess.getVips(service.getServiceId());
                if (vips != null && !vips.isEmpty()) {
                    for (Vip vip : vips) {
                        vip.setEnvironment(vip.network);
                        dataAccess.saveVip(vip);
                    }
                }
            }
        }
    }

    private DataAccessUpdater() {
    }
    
}
