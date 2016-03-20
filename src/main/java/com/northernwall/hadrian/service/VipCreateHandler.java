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

import com.northernwall.hadrian.Util;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.service.dao.PostVipData;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard Thurston
 */
public class VipCreateHandler extends BasicHandler {

    private final AccessHelper accessHelper;
    private final DataAccess dataAccess;
    private final WorkItemProcessor workItemProcess;

    public VipCreateHandler(AccessHelper accessHelper, DataAccess dataAccess, WorkItemProcessor workItemProcess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
        this.dataAccess = dataAccess;
        this.workItemProcess = workItemProcess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostVipData postVipData = Util.fromJson(request, PostVipData.class);

        Service service = getService(postVipData.serviceId, null, null);
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "add a vip");
        Team team = dataAccess.getTeam(service.getTeamId());

        //Check for duplicate VIP
        List<Vip> vips = dataAccess.getVips(postVipData.serviceId);
        for (Vip temp : vips) {
            if (temp.getVipName().equals(postVipData.vipName)) {
                return;
            }
            if (temp.getDns().equals(postVipData.dns)
                    && temp.getDomain().equals(postVipData.domain)
                    && temp.getVipPort() == postVipData.vipPort) {
                return;
            }
        }

        Module module = getModule(postVipData.moduleId, null, service);

        Vip vip = new Vip(
                postVipData.vipName,
                postVipData.serviceId,
                "Creating...",
                postVipData.moduleId,
                postVipData.dns,
                postVipData.domain,
                postVipData.external,
                postVipData.network,
                postVipData.protocol,
                postVipData.vipPort,
                postVipData.servicePort);
        dataAccess.saveVip(vip);

        WorkItem workItem = new WorkItem(Type.vip, Operation.create, user, team, service, module, null, vip);
        dataAccess.saveWorkItem(workItem);
        workItemProcess.sendWorkItem(workItem);
        response.setStatus(200);
        request.setHandled(true);
    }

}
