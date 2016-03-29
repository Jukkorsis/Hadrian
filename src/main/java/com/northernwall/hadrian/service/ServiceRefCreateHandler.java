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

import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Audit;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.ServiceRef;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.service.dao.PostServiceRefData;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard Thurston
 */
public class ServiceRefCreateHandler extends BasicHandler {

    private final AccessHelper accessHelper;

    public ServiceRefCreateHandler(AccessHelper accessHelper, DataAccess dataAccess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String clientId = target.substring(12, target.length() - 4);
        PostServiceRefData postServiceRefData = fromJson(request, PostServiceRefData.class);
        Service clientService = getService(clientId, null, null);
        
        User user = accessHelper.checkIfUserCanModify(request, clientService.getTeamId(), "add a service ref");
        for (Entry<String, String> entry : postServiceRefData.uses.entrySet()) {
            if (entry.getValue().equalsIgnoreCase("true")) {
                String serverId = entry.getKey();
                Service serverService = getDataAccess().getService(serverId);
                if (serverService != null) {
                    ServiceRef ref = new ServiceRef(clientId, serverId);
                    getDataAccess().saveServiceRef(ref);
                    Map<String, String> notes = new HashMap<>();
                    notes.put("uses", serverService.getServiceAbbr());
                    createAudit(clientId, user.getUsername(), Type.serviceRef, Operation.create, notes);
                    notes = new HashMap<>();
                    notes.put("use_by", clientService.getServiceAbbr());
                    createAudit(serverId, user.getUsername(), Type.serviceRef, Operation.create, notes);
                }
            }
        }
        response.setStatus(200);
        request.setHandled(true);
    }

    private void createAudit(String serviceId, String requestor, Type type, Operation operation, Map<String, String> notes) {
        Audit audit = new Audit();
        audit.serviceId = serviceId;
        audit.timePerformed = new Date();
        audit.timeRequested = new Date();
        audit.requestor = requestor;
        audit.type = type;
        audit.operation = operation;
        audit.notes = getGson().toJson(notes);
        getDataAccess().saveAudit(audit);
    }

}
