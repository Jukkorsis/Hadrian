/*
 * Copyright 2015 Richard Thurston.
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
package com.northernwall.hadrian.handlers.dashboard;

import com.google.gson.Gson;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleType;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.handlers.dashboard.dao.GetDashboardData;
import com.northernwall.hadrian.handlers.dashboard.dao.GetDataCenterData;
import com.northernwall.hadrian.handlers.dashboard.dao.GetModuleData;
import com.northernwall.hadrian.handlers.dashboard.helper.ReadAvailabilityRunnable;
import com.northernwall.hadrian.handlers.service.helper.InfoHelper;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

public class DashboardAllHandler extends BasicHandler {

    private final InfoHelper infoHelper;
    private final ExecutorService executorService;

    public DashboardAllHandler(DataAccess dataAccess, Gson gson, InfoHelper infoHelper) {
        super(dataAccess, gson);

        this.infoHelper = infoHelper;

        executorService = Executors.newFixedThreadPool(20);
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String environment = request.getParameter("env");

        GetDashboardData getDashboardData = new GetDashboardData();
        getDashboardData.teamName = "All";

        List<Service> services = getDataAccess().getActiveServices();
        List<Future> futures = new LinkedList<>();
        if (services != null && !services.isEmpty()) {
            for (Service service : services) {
                List<Module> modules = getDataAccess().getModules(service.getServiceId());
                List<Host> hosts = null;
                if (modules != null && !modules.isEmpty()) {
                    for (Module module : modules) {
                        if (module.getModuleType() == ModuleType.Deployable) {
                            if (hosts == null) {
                                hosts = getDataAccess().getHosts(service.getServiceId());
                            }
                            List<Host> moduleHosts = Host.filterModule(module.getModuleId(), environment, hosts);
                            if (moduleHosts != null && !moduleHosts.isEmpty()) {
                                GetModuleData moduleData = new GetModuleData();
                                moduleData.serviceId = service.getServiceId();
                                moduleData.serviceName = service.getServiceName();
                                moduleData.moduleName = module.getModuleName();
                                getDashboardData.addModule(moduleData);

                                for (Host host : moduleHosts) {
                                    GetDataCenterData moduleDataCenterData = moduleData.counts.get(host.getDataCenter());
                                    if (moduleDataCenterData == null) {
                                        moduleDataCenterData = new GetDataCenterData();
                                        moduleData.counts.put(host.getDataCenter(), moduleDataCenterData);
                                    }
                                    GetDataCenterData totalDataCenterData = getDashboardData.counts.get(host.getDataCenter());
                                    if (totalDataCenterData == null) {
                                        totalDataCenterData = new GetDataCenterData();
                                        getDashboardData.counts.put(host.getDataCenter(), totalDataCenterData);
                                    }
                                    futures.add(executorService.submit(new ReadAvailabilityRunnable(moduleDataCenterData, totalDataCenterData, host, module, infoHelper)));
                                }
                            }
                        }
                    }
                }
            }
        }

        waitForFutures(futures, 151, 100);

        toJson(response, getDashboardData);
        response.setStatus(200);
        request.setHandled(true);
    }

}
