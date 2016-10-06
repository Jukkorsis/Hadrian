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
package com.northernwall.hadrian.handlers.utility;

import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleFile;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.handlers.BasicHandler;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConvertHandler extends BasicHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConvertHandler.class);

    public ConvertHandler(DataAccess dataAccess) {
        super(dataAccess);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    }
    
    private void convertNetwork(String oldValue, String newValue) {
        List<Service> services = getDataAccess().getActiveServices();
        if (services != null && !services.isEmpty()) {
            for (Service service : services) {
                List<Module> modules = getDataAccess().getModules(service.getServiceId());
                if (modules != null && !modules.isEmpty()) {
                    for (Module module : modules) {
                        if (module.getNetworkNames() != null
                                && !module.getNetworkNames().isEmpty()
                                && module.getNetworkNames().containsKey(oldValue)) {
                            boolean value = module.getNetworkNames().get(oldValue).booleanValue();
                            LOGGER.info("Found a module with '{}' network, {} in {} with value {}", oldValue, module.getModuleName(), service.getServiceName(), value);
                            module.getNetworkNames().put(newValue, value);
                            module.getNetworkNames().remove(oldValue);
                            getDataAccess().saveModule(module);
                        }
                        List<ModuleFile> moduleFiles = getDataAccess().getModuleFiles(service.getServiceId(), module.getModuleId(), oldValue);
                        if (moduleFiles != null && !moduleFiles.isEmpty()) {
                            for (ModuleFile moduleFile : moduleFiles) {
                                LOGGER.info("Found a module file with '{}' network, {} in {}", oldValue, module.getModuleName(), service.getServiceName());
                                moduleFile.setNetwork(newValue);
                                getDataAccess().saveModuleFile(moduleFile);
                            }
                        }
                    }
                }
                List<Host> hosts = getDataAccess().getHosts(service.getServiceId());
                if (hosts != null && !hosts.isEmpty()) {
                    for (Host host : hosts) {
                        if (host.getNetwork().equals(oldValue)) {
                            LOGGER.info("Found a host with '{}' network, {} in {}", oldValue, host.getHostName(), service.getServiceName());
                            host.setNetwork(newValue);
                            getDataAccess().saveHost(host);
                        }
                    }
                }
                List<Vip> vips = getDataAccess().getVips(service.getServiceId());
                if (vips != null && !vips.isEmpty()) {
                    for (Vip vip : vips) {
                        if (vip.getNetwork().equals(oldValue)) {
                            LOGGER.info("Found a VIP with '{}' network, {} in {}", oldValue, vip.getDns(), service.getServiceName());
                            vip.setNetwork(newValue);
                            getDataAccess().saveVip(vip);
                        }
                    }
                }
            }
        }

    }

}
