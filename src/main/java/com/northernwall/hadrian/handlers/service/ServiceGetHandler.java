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
package com.northernwall.hadrian.handlers.service;

import com.google.gson.Gson;
import com.northernwall.hadrian.handlers.service.helper.InfoHelper;
import com.northernwall.hadrian.config.ConfigHelper;
import com.northernwall.hadrian.config.Const;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.CustomFunction;
import com.northernwall.hadrian.domain.DataStore;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.handlers.service.dao.GetCustomFunctionData;
import com.northernwall.hadrian.handlers.service.dao.GetDataStoreData;
import com.northernwall.hadrian.handlers.module.dao.GetModuleData;
import com.northernwall.hadrian.handlers.service.dao.GetServiceData;
import com.northernwall.hadrian.handlers.vip.dao.GetVipData;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard Thurston
 */
public class ServiceGetHandler extends ServiceRefreshHandler {

    private final AccessHelper accessHelper;

    public ServiceGetHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper, ConfigHelper configHelper, InfoHelper infoHelper) {
        super(dataAccess, gson, accessHelper, configHelper, infoHelper);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType(Const.JSON);
        Service service = getService(request);

        GetServiceData getServiceData = GetServiceData.create(service);
        Team team = getTeam(service.getTeamId(), null);
        getServiceData.canModify = accessHelper.canUserModify(request, team);

        if (service.isActive()) {
            getModuleInfo(service, getServiceData, true);

            List<Future> futures = new LinkedList<>();
            getVipInfo(service, getServiceData);
            getHostInfo(service, getServiceData, futures);
            getDataStoreInfo(service, getServiceData);
            getCustomFunctionInfo(service, getServiceData);
            waitForFutures(futures, 151, 100);
        }

        toJson(response, getServiceData);
        response.setStatus(200);
        request.setHandled(true);
    }

    private void getCustomFunctionInfo(Service service, GetServiceData getServiceData) {
        List<CustomFunction> customFunctions = getDataAccess().getCustomFunctions(service.getServiceId());
        Collections.sort(customFunctions);
        for (CustomFunction customFunction : customFunctions) {
            if (!customFunction.isTeamOnly() || getServiceData.canModify) {
                GetCustomFunctionData getCustomFunctionData = GetCustomFunctionData.create(customFunction);
                getServiceData.addCustomFunction(getCustomFunctionData);
            }
        }
    }

    private void getDataStoreInfo(Service service, GetServiceData getServiceData) {
        List<DataStore> dataStores = getDataAccess().getDataStores(service.getServiceId());
        Collections.sort(dataStores);
        for (DataStore dataStore : dataStores) {
            GetDataStoreData getDataStoreData = GetDataStoreData.create(dataStore);
            getServiceData.dataStores.add(getDataStoreData);
        }
    }

    private void getVipInfo(Service service, GetServiceData getServiceData) {
        List<Vip> vips = getDataAccess().getVips(service.getServiceId());
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
                getServiceData.addVip(getVipData, getModuleData);
            }
        }
    }

}
