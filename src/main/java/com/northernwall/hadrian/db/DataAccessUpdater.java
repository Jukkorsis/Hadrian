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

import com.northernwall.hadrian.config.Config;
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
            version = "1.12";
            dataAccess.setVersion(version);
            LOGGER.info("New DB, initial version set to 1.12");
            return;
        }

        if (version.equals("1.12")) {
            fixSearch(dataAccess);
            LOGGER.info("Current DB version is 1.12, no upgrade required.");
        }
    }

    private static void fixSearch(DataAccess dataAccess) {
        int serviceCount = 0;
        int hostCount = 0;
        int vipCount = 0;
        List<Service> services = dataAccess.getActiveServices();
        if (services != null && !services.isEmpty()) {
            for (Service service : services) {
                dataAccess.insertSearch(
                        SearchSpace.serviceName,
                        service.getServiceName(),
                        service.getTeamId(),
                        service.getServiceId(),
                        null,
                        null,
                        null);
                if (service.getGitProject() != null
                        && !service.getGitProject().isEmpty()) {
                    dataAccess.insertSearch(
                            SearchSpace.gitProject,
                            service.getGitProject(),
                            service.getTeamId(),
                            service.getServiceId(),
                            null,
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
                                        SearchSpace.mavenGroupArtifact,
                                        service.getMavenGroupId() + "." + module.getMavenArtifactId(),
                                        service.getTeamId(),
                                        service.getServiceId(),
                                        module.getModuleId(),
                                        null,
                                        null);
                            }
                        }
                    }
                }
                List<Host> hosts = dataAccess.getHosts(service.getServiceId());
                if (hosts != null && !hosts.isEmpty()) {
                    for (Host host : hosts) {
                        dataAccess.insertSearch(
                                SearchSpace.hostName,
                                host.getHostName(),
                                host.getHostId(),
                                service.getTeamId(),
                                service.getServiceId(),
                                host.getModuleId(),
                                host.getHostId(),
                                null);
                        hostCount++;
                    }
                }
                List<Vip> vips = dataAccess.getVips(service.getServiceId());
                if (vips != null && !vips.isEmpty()) {
                    for (Vip vip : vips) {
                        String fqdn = vip.getDns() + "." + vip.getDomain();
                        dataAccess.deleteSearch(
                                SearchSpace.serviceName, 
                                fqdn,
                                vip.getVipId());
                        dataAccess.insertSearch(
                                SearchSpace.vipFqdn,
                                fqdn,
                                service.getTeamId(),
                                service.getServiceId(),
                                vip.getModuleId(),
                                null,
                                vip.getVipId());
                        vipCount++;
                    }
                }
            }
        }
        LOGGER.info("Backfilled {} service, {} hosts, {} vips", serviceCount, hostCount, vipCount);
    }

    private DataAccessUpdater() {
    }

}
