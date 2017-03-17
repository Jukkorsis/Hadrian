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
import com.northernwall.hadrian.GMT;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.db.SearchResult;
import com.northernwall.hadrian.domain.Audit;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.handlers.routing.Http400BadRequestException;
import com.northernwall.hadrian.handlers.vip.dao.PostVipData;
import java.io.IOException;
import java.util.HashMap;
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
public class VipBackfillHandler extends BasicHandler {

    private final AccessHelper accessHelper;

    public VipBackfillHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostVipData data = fromJson(request, PostVipData.class);

        Service service = getService(data.serviceId, null);
        Team team = getTeam(service.getTeamId(), null);
        User user = accessHelper.checkIfUserCanModify(request, team, "add a vip");

        String dns = VipCreateHandler.checkVipName(data);
        //Check for duplicate VIP
        String fqdn = dns + "." + data.domain;
        SearchResult searchResult = getDataAccess().doSearch(
                Const.SEARCH_SPACE_VIP_FQDN,
                fqdn);
        if (searchResult != null) {
            throw new Http400BadRequestException("VIP already exists with FQDN of " + fqdn);
        }

        Module module = getModule(data.moduleId, null, service);

        Vip vip = new Vip(
                data.serviceId,
                Const.STATUS_NO,
                data.moduleId,
                dns,
                data.domain,
                data.external,
                data.environment,
                data.inboundProtocol,
                data.inboundModifiers,
                data.outboundProtocol,
                data.outboundModifiers,
                data.priorityMode,
                data.vipPort,
                data.servicePort,
                data.httpCheckPort);
        vip.setMigration(0);
        getDataAccess().saveVip(vip);
        getDataAccess().insertSearch(
                Const.SEARCH_SPACE_VIP_FQDN,
                fqdn,
                data.serviceId,
                data.moduleId,
                vip.getVipId());

        Audit audit = new Audit();
        audit.serviceId = service.getServiceId();
        audit.setTimePerformed(GMT.getGmtAsDate());
        audit.timeRequested = GMT.getGmtAsDate();
        audit.requestor = user.getUsername();
        audit.type = Type.vip;
        audit.operation = Operation.create;
        audit.successfull = true;
        audit.moduleName = module.getModuleName();
        audit.vipName = data.dns;
        
        Map<String, String> notes = new HashMap<>();
        notes.put("Inbound_Protocol", data.inboundProtocol);
        if (data.inboundModifiers != null && !data.inboundModifiers.isEmpty()) {
            String temp = "";
            for (String modifier : data.inboundModifiers) {
                temp = temp + " " + modifier;
            }
            notes.put("Inbound_Modifiers", temp);
        }
        notes.put("Outbound_Protocol", data.outboundProtocol);
        if (data.outboundModifiers != null && !data.outboundModifiers.isEmpty()) {
            String temp = "";
            for (String modifier : data.outboundModifiers) {
                temp = temp + " " + modifier;
            }
            notes.put("Outbound_Modifiers", temp);
        }
        notes.put("DNS", dns + "." + data.domain);
        if (data.vipPort > 0) {
            notes.put("VIP_Port", Integer.toString(data.vipPort));
        }
        notes.put("Service_Port", Integer.toString(data.servicePort));
        if (data.httpCheckPort > 0) {
            notes.put("HTTP_Check_Port", Integer.toString(data.httpCheckPort));
        }
        notes.put("Priority_Mode", data.priorityMode);
        notes.put("External", Boolean.toString(data.external));
        notes.put("Reason", "Backfilled vip.");
        audit.notes = getGson().toJson(notes);
        
        getDataAccess().saveAudit(audit, null);

        response.setStatus(200);
        request.setHandled(true);
    }

}
