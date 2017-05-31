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
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import com.northernwall.hadrian.handlers.vip.dao.PutVipData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard Thurston
 */
public class VipModifyHandler extends BasicHandler {

    private final AccessHelper accessHelper;
    private final WorkItemProcessor workItemProcessor;

    public VipModifyHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper, WorkItemProcessor workItemProcessor) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
        this.workItemProcessor = workItemProcessor;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PutVipData data = fromJson(request, PutVipData.class);

        Service service = getService(data.serviceId, null);
        Vip vip = getVip(data.vipId, service);
        Module module = getModule(vip.getModuleId(), null, service);
        Team team = getTeam(service.getTeamId(), null);
        User user = accessHelper.checkIfUserCanModify(request, team, "modify a vip");

        getDataAccess().updateStatus(
                vip.getVipId(),
                true,
                "Updating...",
                Const.STATUS_WIP);

        List<WorkItem> workItems = new ArrayList<>(3);

        WorkItem workItemUpdate = new WorkItem(Type.vip, Operation.update, user, team, service, module, null, vip);
        workItemUpdate.getVip().servicePort = data.servicePort;
        workItemUpdate.getVip().priorityMode = data.priorityMode;
        workItems.add(workItemUpdate);
                
        WorkItem workItemStatus = new WorkItem(Type.vip, Operation.status, user, team, service, module, null, vip);
        workItemStatus.setReason("Updated %% ago");
        workItems.add(workItemStatus);
        
        workItemProcessor.processWorkItems(workItems);
        
        response.setStatus(200);
        request.setHandled(true);
    }

}
