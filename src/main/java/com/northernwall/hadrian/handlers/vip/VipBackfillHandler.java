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
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.GMT;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Audit;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.handlers.vip.dao.PostVipData;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
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
    private final WorkItemProcessor workItemProcessor;

    public VipBackfillHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper, WorkItemProcessor workItemProcessor) {
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

        //Check for duplicate VIP
        List<Vip> vips = getDataAccess().getVips(data.serviceId);
        for (Vip temp : vips) {
            if (temp.getDns().equals(data.dns)
                    && temp.getDomain().equals(data.domain)
                    && temp.getVipPort() == data.vipPort) {
                return;
            }
        }

        Module module = getModule(data.moduleId, null, service);

        Vip vip = new Vip(
                data.serviceId,
                Const.NO_STATUS,
                data.moduleId,
                data.dns,
                data.domain,
                data.external,
                data.environment,
                data.protocol,
                data.vipPort,
                data.servicePort);
        getDataAccess().saveVip(vip);

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
        notes.put("Reason", "Backfilled vip.");
        audit.notes = getGson().toJson(notes);
        
        getDataAccess().saveAudit(audit, null);

        response.setStatus(200);
        request.setHandled(true);
    }

}
