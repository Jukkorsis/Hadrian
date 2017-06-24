/*
 * Copyright 2017 Richard Thurston.
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
package com.northernwall.hadrian.handlers.report;

import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Vip;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Richard
 */
public class VipSummaryReport extends Report {

    public VipSummaryReport(DataAccess dataAccess, PrintWriter writer) {
        super(dataAccess, writer);
    }

    @Override
    public void runReport() throws IOException {
        List<Team> teams = dataAccess.getTeams();
        List<Service> services = dataAccess.getActiveServices();
        Collections.sort(teams);

        //Header row
        writer.println("vip,domain,team,service,env,inboundProtocol,outboundProtocol,servicePort,external,migration");

        //Data rows
        if (teams != null && !teams.isEmpty() && services != null && !services.isEmpty()) {
            for (Team team : teams) {
                List<Service> teamServices = Service.filterTeam(team.getTeamId(), services);
                if (teamServices != null && !teamServices.isEmpty()) {
                    Collections.sort(teamServices);
                    for (Service service : teamServices) {
                        List<Vip> vips = dataAccess.getVips(service.getServiceId());
                        if (vips != null && !vips.isEmpty()) {
                            for (Vip vip : vips) {
                                writer.println(
                                        vip.getDns()
                                        + ","
                                        + vip.getDomain()
                                        + ","
                                        + team.getTeamName()
                                        + ","
                                        + service.getServiceName()
                                        + ","
                                        + vip.getEnvironment()
                                        + ","
                                        + vip.getInboundProtocol()
                                        + ","
                                        + vip.getOutboundProtocol()
                                        + ","
                                        + vip.getServicePort()
                                        + ","
                                        + vip.isExternal()
                                        + ","
                                        + vip.getMigration());
                            }
                        }
                    }
                }
            }
        }
    }

}
