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
package com.northernwall.hadrian.handlers.host;

import com.google.gson.Gson;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.handlers.host.dao.GetHostReducedData;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard Thurston
 */
public class HostsGetHandler extends BasicHandler {

    public HostsGetHandler(DataAccess dataAccess, Gson gson) {
        super(dataAccess, gson);
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        Service service = getService(request);
        List<Host> hosts = getDataAccess().getHosts(service.getServiceId());
        
        Map<String, List<GetHostReducedData>> hostMap = new HashMap<>();
        
        Module module = null;
        for (Host host : hosts) {
            if (module == null || !module.getModuleId().equals(host.getModuleId())) {
                module = getDataAccess().getModule(service.getServiceId(), host.getModuleId());
            }
            
            List<GetHostReducedData> hostList = hostMap.get(module.getModuleName());
            if (hostList == null) {
                hostList = new LinkedList<>();
                hostMap.put(module.getModuleName(), hostList);
            }
            hostList.add(GetHostReducedData.create(host));
        }
        
        toJson(response, hostMap);
        response.setStatus(200);
        request.setHandled(true);
    }

}
