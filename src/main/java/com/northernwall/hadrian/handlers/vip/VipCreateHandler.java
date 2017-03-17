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
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.config.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.db.SearchResult;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.handlers.routing.Http400BadRequestException;
import com.northernwall.hadrian.handlers.vip.dao.PostVipData;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard Thurston
 */
public class VipCreateHandler extends BasicHandler {
    
    public static String checkVipName(PostVipData data) throws Http400BadRequestException {
        String dns = data.dns;
        if (dns == null || dns.isEmpty()) {
            throw new Http400BadRequestException("VIP name is missing");
        }
        dns = dns.trim();
        if (dns == null || dns.isEmpty()) {
            throw new Http400BadRequestException("VIP name is missing");
        }
        if (!dns.matches("^[a-zA-Z0-9/-]+$")) {
            throw new Http400BadRequestException("VIP name contains an illegal character");
        }
        if (dns.length() < 3) {
            throw new Http400BadRequestException("VIP name is to short, minimum is 3");
        }
        if (dns.length() > 30) {
            throw new Http400BadRequestException("VIP name is to long, maximum is 30");
        }
        return dns;
    }

    private final AccessHelper accessHelper;
    private final WorkItemProcessor workItemProcessor;

    public VipCreateHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper, WorkItemProcessor workItemProcessor) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
        this.workItemProcessor = workItemProcessor;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostVipData data = fromJson(request, PostVipData.class);

        Service service = getService(data.serviceId, null);
        Team team = getTeam(service.getTeamId(), null);
        User user = accessHelper.checkIfUserCanModify(request, team, "add a vip");

        String dns = checkVipName(data);
        //Check for duplicate VIP
        String fqdn = dns + "." + data.domain;
        SearchResult searchResult = getDataAccess().doSearch(
                Const.SEARCH_SPACE_VIP_FQDN,
                fqdn);
        if (searchResult != null) {
            throw new Http400BadRequestException("VIP already exists with FQDN of " + fqdn);
        }

        Module module = getModule(data.moduleId, null, service);
        
        //TODO check the protocol mode

        Vip vip = new Vip(
                data.serviceId,
                "Creating...",
                data.moduleId,
                dns,
                data.domain,
                data.external,
                data.environment,
                data.inboundProtocol,
                data.outboundProtocol,
                data.priorityMode,
                data.vipPort,
                data.servicePort);
        vip.setMigration(2);
        getDataAccess().saveVip(vip);
        getDataAccess().insertSearch(
                Const.SEARCH_SPACE_VIP_FQDN,
                fqdn,
                data.serviceId,
                data.moduleId,
                vip.getVipId());

        WorkItem workItem = new WorkItem(Type.vip, Operation.create, user, team, service, module, null, vip);
        workItemProcessor.processWorkItem(workItem);

        response.setStatus(200);
        request.setHandled(true);
    }


}
