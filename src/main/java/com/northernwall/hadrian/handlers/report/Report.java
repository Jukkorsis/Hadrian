/*
 * Copyright 2016 Richard Thurston.
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
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleType;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Richard
 */
public abstract class Report {

    protected final DataAccess dataAccess;
    protected final PrintWriter writer;

    public Report(DataAccess dataAccess, PrintWriter writer) {
        this.dataAccess = dataAccess;
        this.writer = writer;
    }
    
    public abstract void runReport() throws IOException;

    protected void outputServiceHeader() throws IOException {
        writer.print("Team,Service,Scope,DoBuilds,DoDeploys,DoManageVip,DoCheckJar");
    }

    protected void outputServiceRow(Team team, Service service) throws IOException {
        writer.print(team.getTeamName());
        writer.print(",");
        writer.print(service.getServiceName());
        writer.print(",");
        writer.print(service.getScope());
        writer.print(",");
        writer.print(service.isDoBuilds());
        writer.print(",");
        writer.print(service.isDoDeploys());
        writer.print(",");
        writer.print(service.isDoManageVip());
        writer.print(",");
        writer.print(service.isDoCheckJar());
    }

    protected void outputModuleHeader() throws IOException {
        writer.print(",Module,Type,HostAbbr,Outbound,RunAs");
    }

    protected void outputModuleRow(Module module) throws IOException {
        if (module == null) {
            writer.print(",,,,,");
            return;
        }
        
        writer.print(",");
        writer.print(module.getModuleName());
        writer.print(",");
        writer.print(module.getModuleType());

        if (module.getModuleType() != ModuleType.Library) {
            writer.print(",");
            writer.print(module.getHostAbbr());
            writer.print(",");
            writer.print(module.getOutbound());
            writer.print(",");
            writer.print(module.getRunAs());
        } else {
            writer.print(",,,");
        }
    }

    protected void outputHostHeader() {
            writer.print(",HostName,DataCenter,Environmnt");
    }

    protected void outputHostRow(Host host) {
        if (host == null) {
            writer.print(",,,");
            return;
        }
        
        writer.print(",");
        writer.print(host.getHostName());
        writer.print(",");
        writer.print(host.getDataCenter());
        writer.print(",");
        writer.print(host.getEnvironment());
    }

    protected void outputListHeader(List<String> headers) {
        for (String header : headers) {
            writer.print(",");
            writer.print(header);
        }
    }

    protected void outputListRows(List<String> headers, HashMap<String, Integer> counts, String defaultValue) {
        for (String header : headers) {
            writer.print(",");
            if (counts != null && !counts.isEmpty()) {
                if (counts.containsKey(header)) {
                    writer.print(counts.get(header));
                } else if (defaultValue != null && !defaultValue.isEmpty()) {
                    writer.print(defaultValue);
                }
            }
        }
    }

}
