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
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http404NotFoundException;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Thurston
 */
public class VipDeleteHandler extends BasicHandler {

    private final static Logger logger = LoggerFactory.getLogger(VipDeleteHandler.class);

    private final AccessHelper accessHelper;
    private final DataAccess dataAccess;
    private final WorkItemProcessor workItemProcess;

    public VipDeleteHandler(AccessHelper accessHelper, DataAccess dataAccess, WorkItemProcessor workItemProcess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
        this.dataAccess = dataAccess;
        this.workItemProcess = workItemProcess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String serviceId = target.substring(8, 44);
        String vipId = target.substring(45);
        Service service = getService(serviceId, null, null);
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "delete a vip");
        Team team = dataAccess.getTeam(service.getTeamId());

        Vip vip = dataAccess.getVip(serviceId, vipId);
        if (vip == null) {
            throw new Http404NotFoundException("Could not find vip with id " + vipId);
        }

        vip.setStatus("Deleting...");
        dataAccess.updateVip(vip);

        WorkItem workItem = new WorkItem(Type.vip, Operation.delete, user, team, service, null, null, vip);
        dataAccess.saveWorkItem(workItem);
        workItemProcess.sendWorkItem(workItem);
        response.setStatus(200);
        request.setHandled(true);
    }

}
