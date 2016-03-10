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
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.VipRef;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 *
 * @author Richard Thurston
 */
public class HostVipDeleteHandler extends AbstractHandler {

    private final AccessHelper accessHelper;
    private final DataAccess dataAccess;
    private final WorkItemProcessor workItemProcess;

    public HostVipDeleteHandler(AccessHelper accessHelper, DataAccess dataAccess, WorkItemProcessor workItemProcess) {
        this.accessHelper = accessHelper;
        this.dataAccess = dataAccess;
        this.workItemProcess = workItemProcess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String serviceId = target.substring(9, 45);
        String hostId = target.substring(46, 82);
        String vipId = target.substring(83);

        Service service = dataAccess.getService(serviceId);
        if (service == null) {
            throw new RuntimeException("Could not find service");
        }
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "delete host vip");
        Team team = dataAccess.getTeam(service.getTeamId());
        VipRef vipRef = dataAccess.getVipRef(hostId, vipId);
        if (vipRef == null) {
            return;
        }
        Host host = dataAccess.getHost(serviceId, hostId);
        if (host == null) {
            throw new RuntimeException("Could not find host");
        }
        Vip vip = dataAccess.getVip(serviceId, vipId);
        if (vip == null) {
            throw new RuntimeException("Could not find vip");
        }
        vipRef.setStatus("Removing...");
        dataAccess.updateVipRef(vipRef);
        WorkItem workItem = new WorkItem(Type.hostvip, Operation.delete, user, team, service, null, host, vip);
        dataAccess.saveWorkItem(workItem);
        workItemProcess.sendWorkItem(workItem);

        response.setStatus(200);
        request.setHandled(true);
    }

}
