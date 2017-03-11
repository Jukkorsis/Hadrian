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

import com.northernwall.hadrian.config.Const;
import com.northernwall.hadrian.config.Config;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleType;
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
            version = "1.9";
            dataAccess.setVersion(version);
            LOGGER.info("New DB, initial version set to 1.9");
        }
        if (version.equals("1.7")) {
            fixModule(dataAccess, config);
            version = "1.8";
            dataAccess.setVersion(version);
            LOGGER.info("DB has been upgraded to 1.8 from 1.7");
        }
        if (version.equals("1.8")) {
            fixVip(dataAccess, config);
            version = "1.9";
            dataAccess.setVersion(version);
            LOGGER.info("DB has been upgraded to 1.9 from 1.8");
        }
        if (version.equals("1.9")) {
            fixHost(dataAccess);
            LOGGER.info("Current DB version is 1.9, no upgrade required.");
        }
    }

    private static void fixVip(DataAccess dataAccess, Config config) {
        List<Service> services = dataAccess.getActiveServices();
        if (services != null && !services.isEmpty()) {
            for (Service service : services) {
                List<Vip> vips = dataAccess.getVips(service.getServiceId());
                if (vips != null && !vips.isEmpty()) {
                    for (Vip vip : vips) {
                        if (vip.getLbConfig() == null || vip.getLbConfig().isEmpty()) {
                            vip.setLbConfig(config.lbConfigs.get(0));
                            dataAccess.saveVip(vip);
                        }
                    }
                }
            }
        }
    }

    private static void fixModule(DataAccess dataAccess, Config config) {
        List<Service> services = dataAccess.getActiveServices();
        if (services != null && !services.isEmpty()) {
            for (Service service : services) {
                List<Module> modules = dataAccess.getModules(service.getServiceId());
                if (modules != null && !modules.isEmpty()) {
                    for (Module module : modules) {
                        if (module.getModuleType() == ModuleType.Deployable
                                || module.getModuleType() == ModuleType.Simulator) {
                            module.setPlatform(config.platforms.get(0));
                            module.setSizeCpu(config.minCpu);
                            module.setSizeMemory(config.minMemory);
                            module.setSizeStorage(config.minStorage);
                            dataAccess.saveModule(module);
                        }
                    }
                }
            }
        }
    }

    private static void fixHost(DataAccess dataAccess) {
        int serviceCount = 0;
        int hostCount = 0;
        List<Service> services = dataAccess.getActiveServices();
        if (services != null && !services.isEmpty()) {
            for (Service service : services) {
                dataAccess.insertSearch(
                        Const.SEARCH_SPACE_SERVICE_NAME,
                        service.getServiceName(),
                        service.getServiceId(),
                        null,
                        null);
                if (service.getGitProject() != null
                        && !service.getGitProject().isEmpty()) {
                    dataAccess.insertSearch(
                            Const.SEARCH_SPACE_GIT_PROJECT,
                            service.getGitProject(),
                            service.getServiceId(),
                            null,
                            null);
                }
                serviceCount++;
                if (service.getMavenGroupId() != null
                        && !service.getMavenGroupId().isEmpty()) {
                    List<Module> modules = dataAccess.getModules(service.getServiceId());
                    if (modules != null && !modules.isEmpty()) {
                        for (Module module : modules) {
                            if (module.getMavenArtifactId() != null
                                    && !module.getMavenArtifactId().isEmpty()) {
                                dataAccess.insertSearch(
                                        Const.SEARCH_SPACE_MAVEN_GROUP_ARTIFACT,
                                        service.getMavenGroupId() + "." + module.getMavenArtifactId(),
                                        service.getServiceId(),
                                        module.getModuleId(),
                                        null);
                            }
                        }
                    }
                }
                List<Host> hosts = dataAccess.getHosts(service.getServiceId());
                if (hosts != null && !hosts.isEmpty()) {
                    for (Host host : hosts) {
                        dataAccess.insertSearch(
                                Const.SEARCH_SPACE_HOST_NAME,
                                host.getHostName(),
                                service.getServiceId(),
                                host.getModuleId(),
                                host.getHostId());
                        hostCount++;
                    }
                }
            }
        }
        LOGGER.info("Backfilled {} service, {} hosts", serviceCount, hostCount);
    }

    private DataAccessUpdater() {
    }

}
