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
import java.util.Map;
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
public class HostVipCreateHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(HostVipCreateHandler.class);

    private final AccessHelper accessHelper;
    private final DataAccess dataAccess;
    private final WorkItemProcessor workItemProcess;

    public HostVipCreateHandler(AccessHelper accessHelper, DataAccess dataAccess, WorkItemProcessor workItemProcess) {
        this.accessHelper = accessHelper;
        this.dataAccess = dataAccess;
        this.workItemProcess = workItemProcess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostHostVipData data = Util.fromJson(request, PostHostVipData.class);
        Service service = dataAccess.getService(data.serviceId);
        if (service == null) {
            throw new RuntimeException("Could not find service");
        }
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "add a host vip");
        Team team = dataAccess.getTeam(service.getTeamId());
        List<Host> hosts = dataAccess.getHosts(data.serviceId);
        List<Vip> vips = dataAccess.getVips(data.serviceId);
        Module module = null;
        for (Map.Entry<String, String> entry : data.hosts.entrySet()) {
            if (entry.getValue().equalsIgnoreCase("true")) {
                boolean found = false;
                for (Host host : hosts) {
                    if (entry.getKey().equals(host.getHostId())) {
                        found = true;
                        for (Map.Entry<String, String> entry2 : data.vips.entrySet()) {
                            if (entry2.getValue().equalsIgnoreCase("true")) {
                                boolean found2 = false;
                                for (Vip vip : vips) {
                                    if (entry2.getKey().equals(vip.getVipId())) {
                                        found2 = true;
                                        if (host.getNetwork().equals(vip.getNetwork())) {
                                            if (module == null || host.getModuleId().equals(module.getModuleId())) {
                                                for (Module temp : dataAccess.getModules(host.getServiceId())) {
                                                    if (temp.getModuleId().equals(host.getModuleId())) {
                                                        module = temp;
                                                    }
                                                }
                                            }
                                            dataAccess.saveVipRef(new VipRef(host.getHostId(), vip.getVipId(), "Adding..."));
                                            WorkItem workItem = new WorkItem(Type.hostvip, Operation.create, user, team, service, module, host, vip);
                                            dataAccess.saveWorkItem(workItem);
                                            workItemProcess.sendWorkItem(workItem);
                                        } else {
                                            logger.warn("Request to add {} to {} reject because they are not on the same network", host.getHostName(), vip.getVipName());
                                        }
                                    }
                                }
                                if (!found2) {
                                    logger.error("Asked to add host(s) to vip {}, but vip is not on service {}", entry2.getKey(), data.serviceId);
                                }
                            }
                        }
                    }
                }
                if (!found) {
                    logger.error("Asked to add vip(s) to host {}, but host is not on service {}", entry.getKey(), data.serviceId);
                }
            }
        }
                response.setStatus(200);
                request.setHandled(true);
    }

}
