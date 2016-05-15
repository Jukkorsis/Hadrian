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

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ModuleRef;
import com.northernwall.hadrian.domain.ModuleType;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.service.dao.GetNotUsesData;
import com.northernwall.hadrian.service.dao.GetModuleRefData;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 *
 * @author Richard Thurston
 */
public class ServiceNotUsesGetHandler extends AbstractHandler {

    private final DataAccess dataAccess;
    private final Gson gson;

    public ServiceNotUsesGetHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        gson = new Gson();
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String serviceId = request.getParameter("serviceId");
        String moduleId = request.getParameter("moduleId");
        List<ModuleRef> refs = dataAccess.getModuleRefsByClient(serviceId, moduleId);

        GetNotUsesData notUses = new GetNotUsesData();

        List<Service> services = dataAccess.getActiveServices();
        for (Service service : services) {
            List<Module> modules = dataAccess.getModules(service.getServiceId());
            for (Module module : modules) {
                if (checkModule(serviceId, moduleId, refs, module)) {
                    GetModuleRefData ref = new GetModuleRefData();
                    ref.serverServiceId = service.getServiceId();
                    ref.serverModuleId = module.getModuleId();
                    ref.serviceName = service.getServiceName();
                    ref.moduleName = module.getModuleName();
                    notUses.refs.add(ref);
                }
            }
        }

        Collections.sort(notUses.refs, new Comparator<GetModuleRefData>() {
            @Override
            public int compare(GetModuleRefData o1, GetModuleRefData o2) {
                return o1.moduleName.compareTo(o2.moduleName);
            }
        });

        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            gson.toJson(notUses, GetNotUsesData.class, jw);
        }
        response.setStatus(200);
        request.setHandled(true);
    }

    private boolean checkModule(String serviceId, String moduleId, List<ModuleRef> refs, Module module) {
        if (module.getServiceId().equals(serviceId) && module.getModuleId().equals(moduleId)) {
            return false;
        }
        if (module.getModuleType().equals(ModuleType.Test)) {
            return false;
        }
        for (ModuleRef ref : refs) {
            if (module.getServiceId().equals(ref.getServerServiceId())
                    && module.getModuleId().equals(ref.getServerModuleId())) {
                return false;
            }
        }
        return true;
    }

}
