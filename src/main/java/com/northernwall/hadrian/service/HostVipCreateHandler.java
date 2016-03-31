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
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import com.northernwall.hadrian.service.dao.PostHostVipData;
import java.io.IOException;
import java.util.List;
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
public class HostVipCreateHandler extends BasicHandler {

    private final static Logger logger = LoggerFactory.getLogger(HostVipCreateHandler.class);

    private final AccessHelper accessHelper;
    private final WorkItemProcessor workItemProcess;

    public HostVipCreateHandler(AccessHelper accessHelper, DataAccess dataAccess, WorkItemProcessor workItemProcess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
        this.workItemProcess = workItemProcess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostHostVipData data = fromJson(request, PostHostVipData.class);
        Service service = getService(data.serviceId,  null, null);
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "add a host vip");
        Team team = getDataAccess().getTeam(service.getTeamId());
        List<Host> hosts = getDataAccess().getHosts(data.serviceId);
        Vip vip = getVip(data.vipId, null, service);
        Module module = null;
        for (Host host : hosts) {
            if (data.hostNames.contains(host.getHostName())) {
                if (host.getNetwork().equals(vip.getNetwork())) {
                    if (module == null || host.getModuleId().equals(module.getModuleId())) {
                        for (Module temp : getDataAccess().getModules(host.getServiceId())) {
                            if (temp.getModuleId().equals(host.getModuleId())) {
                                module = temp;
                            }
                        }
                    }
                    getDataAccess().saveVipRef(new VipRef(host.getHostId(), vip.getVipId(), "Adding..."));
                    WorkItem workItem = new WorkItem(Type.hostvip, Operation.create, user, team, service, module, host, vip);
                    getDataAccess().saveWorkItem(workItem);
                    workItemProcess.sendWorkItem(workItem);
                } else {
                    logger.warn("Request to add {} to {} reject because they are not on the same network", host.getHostName(), vip.getVipName());
                }
            }
        }
        response.setStatus(200);
        request.setHandled(true);
    }

}
