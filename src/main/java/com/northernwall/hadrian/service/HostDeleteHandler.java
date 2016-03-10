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
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Operation;
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
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Thurston
 */
public class HostDeleteHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(HostDeleteHandler.class);

    private final AccessHelper accessHelper;
    private final DataAccess dataAccess;
    private final WorkItemProcessor workItemProcess;

    public HostDeleteHandler(AccessHelper accessHelper, DataAccess dataAccess, WorkItemProcessor workItemProcess) {
        this.accessHelper = accessHelper;
        this.dataAccess = dataAccess;
        this.workItemProcess = workItemProcess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String serviceId = target.substring(9, 45);
        String hostId = target.substring(46);
        Host host = dataAccess.getHost(serviceId, hostId);
        if (host == null) {
            throw new Http404NotFoundException("Could not find host with id " + hostId);
        }
        Service service = dataAccess.getService(host.getServiceId());
        if (service == null) {
            throw new Http404NotFoundException("Could not find service");
        }
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "deleting a host");
        Team team = dataAccess.getTeam(service.getTeamId());
        host.setStatus("Deleting...");
        dataAccess.updateHost(host);
        WorkItem workItem = new WorkItem(Type.host, Operation.delete, user, team, service, null, host, null);
        dataAccess.saveWorkItem(workItem);
        workItemProcess.sendWorkItem(workItem);

        response.setStatus(200);
        request.setHandled(true);
    }

}
