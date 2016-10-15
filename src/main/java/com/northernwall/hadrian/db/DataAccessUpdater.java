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
import com.northernwall.hadrian.domain.GitMode;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataAccessUpdater {

    private final static Logger LOGGER = LoggerFactory.getLogger(DataAccessUpdater.class);

    public static void update(DataAccess dataAccess, Config config) {
        String version = dataAccess.getVersion();

        if (version == null) {
            dataAccess.setVersion("1.6");
            update(dataAccess, config);
        } else if (!version.equals("1.6")) {
            LOGGER.info("Upgrading DB version to 1.6");
            fixGitMode(dataAccess, config);
            dataAccess.setVersion("1.6");
            update(dataAccess, config);
        }

        fixHost(dataAccess);

        LOGGER.info("Current DB version is {}, no upgrade required.", version);
    }

    private static void fixGitMode(DataAccess dataAccess, Config config) {
        List<Service> services = dataAccess.getActiveServices();
        if (services != null && !services.isEmpty()) {
            for (Service service : services) {
                boolean changed = false;
                if (service.getScope() == null
                        && config.scopes != null
                        && !config.scopes.isEmpty()) {
                    service.setScope(config.scopes.get(0));
                    changed = true;
                }

                List<Module> modules = dataAccess.getModules(service.getServiceId());
                if (modules != null && !modules.isEmpty()) {
                    LOGGER.info("Upgrading service {} ({}), with {} modules", service.getServiceName(), service.getGitMode(), modules.size());
                    for (Module module : modules) {
                        switch (module.getModuleType()) {
                            case Test:
                                if (service.getTestStyle() == null) {
                                    service.setTestStyle(module.getTestStyle());
                                    if (module.getTestStyle().equals("Script")) {
                                        service.setTestHostname(module.getHostname());
                                        service.setTestRunAs(module.getRunAs());
                                        service.setTestDeploymentFolder(module.getDeploymentFolder());
                                        service.setTestCmdLine(module.getStartCmdLine());
                                    }
                                    changed = true;
                                } else {
                                    LOGGER.warn("!!!!! found second test module, {}!", module.getModuleName());
                                }
                                dataAccess.deleteModule(service.getServiceId(), module.getModuleId());
                                break;
                            default:
                                if (service.getMavenGroupId() == null) {
                                    service.setMavenGroupId(module.getMavenGroupId());
                                    changed = true;
                                }
                                break;
                        }
                    }
                    if (service.getTestStyle() == null) {
                        service.setTestStyle("Maven");
                        changed = true;
                    }
                    if (service.getGitMode() == GitMode.Flat) {
                        if (modules.size() > 1) {
                            LOGGER.warn("!!!!! {} modules for flat service {}!", modules.size(), service.getServiceName());
                        }
                        Module module = modules.get(0);
                        service.setGitProject(module.getGitProject());
                        service.setGitMode(GitMode.Consolidated);
                        changed = true;
                    }
                } else {
                    LOGGER.info("Upgrading service {} ({}), with zero modules", service.getServiceName(), service.getGitMode());
                    if (service.getGitMode() == GitMode.Flat) {
                        LOGGER.warn("!!!!! service {} is flat and has no modules, Git Project will be null!", service.getServiceName());
                    }
                }
                if (changed) {
                    dataAccess.saveService(service);
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

    private DataAccessUpdater() {
    }
}
