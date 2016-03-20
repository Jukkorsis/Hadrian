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
package com.northernwall.hadrian.service;

import com.northernwall.hadrian.service.helper.InfoHelper;
import com.northernwall.hadrian.maven.MavenHelper;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.ConfigHelper;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.CustomFunction;
import com.northernwall.hadrian.domain.DataStore;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.VipRef;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ServiceRef;
import com.northernwall.hadrian.service.dao.GetCustomFunctionData;
import com.northernwall.hadrian.service.dao.GetDataStoreData;
import com.northernwall.hadrian.service.dao.GetHostData;
import com.northernwall.hadrian.service.dao.GetModuleData;
import com.northernwall.hadrian.service.dao.GetServiceData;
import com.northernwall.hadrian.service.dao.GetServiceRefData;
import com.northernwall.hadrian.service.dao.GetVipData;
import com.northernwall.hadrian.service.dao.GetVipRefData;
import com.northernwall.hadrian.service.helper.ReadAvailabilityRunnable;
import com.northernwall.hadrian.service.helper.ReadMavenVersionsRunnable;
import com.northernwall.hadrian.service.helper.ReadVersionRunnable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard Thurston
 */
public class ServiceGetHandler extends BasicHandler {

    private final AccessHelper accessHelper;
    private final DataAccess dataAccess;
    private final ConfigHelper configHelper;
    private final MavenHelper mavenHelper;
    private final InfoHelper infoHelper;
    private final Gson gson;
    private final ExecutorService executorService;

    public ServiceGetHandler(AccessHelper accessHelper, DataAccess dataAccess, ConfigHelper configHelper, MavenHelper mavenHelper, InfoHelper infoHelper) {
        super(dataAccess);
        this.accessHelper = accessHelper;
        this.dataAccess = dataAccess;
        this.configHelper = configHelper;
        this.mavenHelper = mavenHelper;
        this.infoHelper = infoHelper;
        gson = new Gson();

        executorService = Executors.newFixedThreadPool(20);
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String id = target.substring(12, target.length());
        response.setContentType(Const.JSON);
        Service service = getService(id, null, null);

        GetServiceData getServiceData = GetServiceData.create(service);
        getServiceData.canModify = accessHelper.canUserModify(request, service.getTeamId());

        List<Future> futures = new LinkedList<>();

        List<Module> modules = dataAccess.getModules(id);
        Collections.sort(modules);
        for (Module module : modules) {
            GetModuleData getModuleData = GetModuleData.create(module, configHelper.getConfig());
            futures.add(executorService.submit(new ReadMavenVersionsRunnable(getModuleData, mavenHelper)));
            getServiceData.modules.add(getModuleData);
        }

        List<Vip> vips = dataAccess.getVips(id);
        Collections.sort(vips);
        for (Vip vip : vips) {
            GetModuleData getModuleData = null;
            for (GetModuleData temp : getServiceData.modules) {
                if (vip.getModuleId().equals(temp.moduleId)) {
                    getModuleData = temp;
                }
            }
            if (getModuleData != null) {
                GetVipData getVipData = GetVipData.create(vip);
                getModuleData.addVip(getVipData);
            }
        }

        List<Host> hosts = dataAccess.getHosts(id);
        Collections.sort(hosts);
        for (Host host : hosts) {
            GetModuleData getModuleData = null;
            for (GetModuleData temp : getServiceData.modules) {
                if (host.getModuleId().equals(temp.moduleId)) {
                    getModuleData = temp;
                }
            }
            if (getModuleData != null) {
                GetHostData getHostData = GetHostData.create(host);
                futures.add(executorService.submit(new ReadVersionRunnable(getHostData, getModuleData, infoHelper)));
                futures.add(executorService.submit(new ReadAvailabilityRunnable(getHostData, getModuleData, infoHelper)));
                for (VipRef vipRef : dataAccess.getVipRefsByHost(getHostData.hostId)) {
                    GetVipRefData getVipRefData = GetVipRefData.create(vipRef);
                    for (GetVipData vip : getModuleData.getVips(host.getNetwork())) {
                        if (vip.vipId.equals(getVipRefData.vipId)) {
                            getVipRefData.vipName = vip.vipName;
                        }
                    }
                    getHostData.vipRefs.add(getVipRefData);
                }
                getModuleData.addHost(getHostData);
            }
        }

        List<DataStore> dataStores = dataAccess.getDataStores(id);
        Collections.sort(dataStores);
        for (DataStore dataStore : dataStores) {
            GetDataStoreData getDataStoreData = GetDataStoreData.create(dataStore);
            getServiceData.dataStores.add(getDataStoreData);
        }

        for (ServiceRef ref : dataAccess.getServiceRefsByClient(id)) {
            GetServiceRefData tempRef = GetServiceRefData.create(ref);
            tempRef.serviceName = dataAccess.getService(ref.getServerServiceId()).getServiceName();
            getServiceData.uses.add(tempRef);
        }

        for (ServiceRef ref : dataAccess.getServiceRefsByServer(id)) {
            GetServiceRefData tempRef = GetServiceRefData.create(ref);
            tempRef.serviceName = dataAccess.getService(ref.getClientServiceId()).getServiceName();
            getServiceData.usedBy.add(tempRef);
        }

        List<CustomFunction> customFunctions = dataAccess.getCustomFunctions(id);
        Collections.sort(customFunctions);
        for (CustomFunction customFunction : customFunctions) {
            for (GetModuleData temp : getServiceData.modules) {
                if (customFunction.getModuleId().equals(temp.moduleId)) {
                    GetCustomFunctionData getCustomFunctionData = GetCustomFunctionData.create(customFunction);
                    temp.customFunctions.add(getCustomFunctionData);
                }
            }
        }

        waitForFutures(futures);

        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            gson.toJson(getServiceData, GetServiceData.class, jw);
        }
        response.setStatus(200);
        request.setHandled(true);
    }

    private void waitForFutures(List<Future> futures) {
        for (int i = 0; i < 20; i++) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
            }
            futures.removeIf(new Predicate<Future>() {
                @Override
                public boolean test(Future t) {
                    return t.isDone();
                }
            });
            if (futures.isEmpty()) {
                return;
            }
        }
    }

}
