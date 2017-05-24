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

import com.google.gson.Gson;
import com.northernwall.hadrian.config.ConfigHelper;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleFile;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.handlers.routing.Http400BadRequestException;
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
    
    private final AccessHelper accessHelper;
    private final ConfigHelper configHelper;

    public ConvertHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper, ConfigHelper configHelper) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
        this.configHelper = configHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        accessHelper.checkIfUserIsAdmin(request, "convert attribute");

        String attribute = request.getParameter("attr");
        String oldValue = request.getParameter("old");
        String newValue = request.getParameter("new");
        
        if (attribute == null || attribute.isEmpty()) {
            throw new Http400BadRequestException("Parameter 'attr' is missing");
        }
        
        if (oldValue == null || oldValue.isEmpty()) {
            throw new Http400BadRequestException("Parameter 'old' is missing");
        }
        
        if (newValue == null || newValue.isEmpty()) {
            throw new Http400BadRequestException("Parameter 'new' is missing");
        }
        
        if (attribute.equalsIgnoreCase("environment")) {
            if (!configHelper.getConfig().environmentNames.contains(newValue)) {
                throw new Http400BadRequestException("New Value is not a legal Environment");
            }
            LOGGER.info("Converting Environment value {} to {}", oldValue, newValue);
            convertEnvironment(oldValue, newValue);
        } else if (attribute.equalsIgnoreCase("platform")) {
            if (!configHelper.getConfig().platforms.contains(newValue)) {
                throw new Http400BadRequestException("New Value is not a legal Platform");
            }
            LOGGER.info("Converting Platform value {} to {}", oldValue, newValue);
            convertPlatform(oldValue, newValue);
        } else {
            throw new Http400BadRequestException("Parameter 'attr' is unknown");
        }
        
        response.setStatus(200);
        request.setHandled(true);
    }
    
    private void convertEnvironment(String oldValue, String newValue) {
        List<Service> services = getDataAccess().getActiveServices();
        if (services != null && !services.isEmpty()) {
            for (Service service : services) {
                List<Module> modules = getDataAccess().getModules(service.getServiceId());
                if (modules != null && !modules.isEmpty()) {
                    for (Module module : modules) {
                        if (module.getEnvironmentNames() != null
                                && !module.getEnvironmentNames().isEmpty()
                                && module.getEnvironmentNames().containsKey(oldValue)) {
                            boolean value = module.getEnvironmentNames().get(oldValue).booleanValue();
                            LOGGER.info("Found a module with '{}' environment, {} in {} with value {}", 
                                    oldValue, 
                                    module.getModuleName(), 
                                    service.getServiceName(), 
                                    value);
                            module.getEnvironmentNames().put(newValue, value);
                            module.getEnvironmentNames().remove(oldValue);
                            getDataAccess().saveModule(module);
                        }
                        List<ModuleFile> moduleFiles = getDataAccess().getModuleFiles(
                                service.getServiceId(), 
                                module.getModuleId(), 
                                oldValue);
                        if (moduleFiles != null && !moduleFiles.isEmpty()) {
                            for (ModuleFile moduleFile : moduleFiles) {
                                LOGGER.info("Found a module file with '{}' environment, {} in {}", 
                                        oldValue, 
                                        module.getModuleName(), 
                                        service.getServiceName());
                                moduleFile.setEnvironment(newValue);
                                getDataAccess().saveModuleFile(moduleFile);
                                getDataAccess().deleteModuleFile(
                                        service.getServiceId(), 
                                        module.getModuleId(), 
                                        oldValue, 
                                        moduleFile.getName());
                            }
                        }
                    }
                }
                List<Host> hosts = getDataAccess().getHosts(service.getServiceId());
                if (hosts != null && !hosts.isEmpty()) {
                    for (Host host : hosts) {
                        if (host.getEnvironment().equals(oldValue)) {
                            LOGGER.info("Found a host with '{}' environment, {} in {}", 
                                    oldValue, 
                                    host.getHostName(), 
                                    service.getServiceName());
                            host.setEnvironment(newValue);
                            getDataAccess().updateHost(host);
                        }
                    }
                }
                List<Vip> vips = getDataAccess().getVips(service.getServiceId());
                if (vips != null && !vips.isEmpty()) {
                    for (Vip vip : vips) {
                        if (vip.getEnvironment().equals(oldValue)) {
                            LOGGER.info("Found a VIP with '{}' environment, {} in {}", 
                                    oldValue, 
                                    vip.getDns(), 
                                    service.getServiceName());
                            vip.setEnvironment(newValue);
                            getDataAccess().saveVip(vip);
                        }
                    }
                }
            }
        }
    }

    private void convertPlatform(String oldValue, String newValue) {
        List<Service> services = getDataAccess().getActiveServices();
        if (services != null && !services.isEmpty()) {
            for (Service service : services) {
                List<Module> modules = getDataAccess().getModules(service.getServiceId());
                if (modules != null && !modules.isEmpty()) {
                    for (Module module : modules) {
                        if (module.getPlatform().equals(oldValue)) {
                            LOGGER.info("Found a module with '{}' environment, {} in {}", 
                                    oldValue, 
                                    module.getModuleName(), 
                                    service.getServiceName());
                            module.setPlatform(newValue);
                            getDataAccess().updateModule(module);
                        }
                    }
                }
            }
        }

    }
    
}
