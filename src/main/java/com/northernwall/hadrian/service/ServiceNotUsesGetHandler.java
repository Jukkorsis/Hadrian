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
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceRef;
import com.northernwall.hadrian.service.dao.GetNotUsesData;
import com.northernwall.hadrian.service.dao.GetServiceRefData;
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
        String id = request.getParameter("serviceId");
        List<Service> services = dataAccess.getServices();
        List<ServiceRef> refs = dataAccess.getServiceRefsByClient(id);

        GetNotUsesData notUses = new GetNotUsesData();
        for (Service service : services) {
            if (!service.getServiceId().equals(id)) {
                boolean found = false;
                for (ServiceRef ref : refs) {
                    if (service.getServiceId().equals(ref.getServerServiceId())) {
                        found = true;
                    }
                }
                if (!found) {
                    GetServiceRefData ref = new GetServiceRefData();
                    ref.clientServiceId = id;
                    ref.serverServiceId = service.getServiceId();
                    ref.serviceName = service.getServiceName();
                    notUses.refs.add(ref);
                }
            }
        }

        Collections.sort(notUses.refs, new Comparator<GetServiceRefData>() {
            @Override
            public int compare(GetServiceRefData o1, GetServiceRefData o2) {
                return o1.serviceName.compareTo(o2.serviceName);
            }
        });
        
        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            gson.toJson(notUses, GetNotUsesData.class, jw);
        }
        response.setStatus(200);
        request.setHandled(true);
    }

}
