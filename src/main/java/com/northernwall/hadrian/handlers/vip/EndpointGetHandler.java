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
package com.northernwall.hadrian.handlers.vip;

import com.google.gson.Gson;
import com.northernwall.hadrian.config.Const;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.db.SearchResult;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.handlers.routing.Http404NotFoundException;
import com.northernwall.hadrian.handlers.vip.dao.GetEndpointData;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard Thurston
 */
public class EndpointGetHandler extends BasicHandler {
    
    public EndpointGetHandler(DataAccess dataAccess, Gson gson) {
        super(dataAccess, gson);
    }
    
    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String fqdn = target.substring(13);
        SearchResult result = getDataAccess().doSearch(Const.SEARCH_SPACE_VIP_FQDN, fqdn);
        
        if (result == null) {
            throw new Http404NotFoundException("Could not find endpoint");
        }
        
        Vip vip = getDataAccess().getVip(result.serviceId, result.hostId);
        GetEndpointData endpoint = GetEndpointData.create(vip);
        
        Module module = getDataAccess().getModule(result.serviceId, vip.getModuleId());
        
        endpoint.monitoringPath = module.getAvailabilityUrl();
        int i = endpoint.monitoringPath.indexOf("/");
        endpoint.monitoringPath = endpoint.monitoringPath.substring(i);
        
        toJson(response, endpoint);
        response.setStatus(200);
        request.setHandled(true);
    }
    
}
