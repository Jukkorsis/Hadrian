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
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.details.VipDetailsHelper;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.handlers.vip.dao.GetVipDetailRowData;
import com.northernwall.hadrian.handlers.vip.dao.GetVipDetailRowDataComparator;
import com.northernwall.hadrian.handlers.vip.dao.GetVipDetailsData;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard Thurston
 */
public class VipGetDetailsHandler extends BasicHandler {

    private final VipDetailsHelper vipDetailsHelper;
    private final GetVipDetailRowDataComparator comparator;

    public VipGetDetailsHandler(DataAccess dataAccess, Gson gson, VipDetailsHelper vipDetailsHelper) {
        super(dataAccess, gson);
        this.vipDetailsHelper = vipDetailsHelper;
        this.comparator = new GetVipDetailRowDataComparator();
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        Service service = getService(request);
        Vip vip = getVip(request, service);

        GetVipDetailsData details = vipDetailsHelper.getDetails(vip);

        List<Host> hosts = getDataAccess().getHosts(service.getServiceId());
        for (Host host : hosts) {
            if (host.getEnvironment().equals(vip.getEnvironment())
                    && host.getModuleId().equals(vip.getModuleId())) {
                boolean found = false;
                for (GetVipDetailRowData row : details.rows) {
                    if (host.getHostName().equalsIgnoreCase(row.hostName)) {
                        found = true;
                        if (host.getComment() != null && !host.getComment().isEmpty()) {
                            row.comment = host.getComment();
                        }
                        if (vip.getDisabledHosts().contains(host.getHostName())) {
                            row.disabled = true;
                        }
                    }
                }
                if (!found) {
                    GetVipDetailRowData temp = new GetVipDetailRowData();
                    temp.hostName = host.getHostName();
                    temp.warning = "Host not found in VIP";
                    if (host.getComment() != null && !host.getComment().isEmpty()) {
                        temp.comment = host.getComment();
                    }
                    temp.disabled = true;
                    details.rows.add(temp);
                }
            }
        }

        for (GetVipDetailRowData row : details.rows) {
            if (row.warning.equals("-")) {
                for (Host host : hosts) {
                    if (host.getHostName().equalsIgnoreCase(row.hostName)) {
                        row.warning = "Host in VIP, but not in inventory";
                        if (vip.getDisabledHosts().contains(host.getHostName())) {
                            row.disabled = true;
                        }
                    }
                }
            }
        }

        Collections.sort(details.rows, comparator);

        toJson(response, details);
        response.setStatus(200);
        request.setHandled(true);
    }

}
