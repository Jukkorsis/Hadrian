/*
 * Copyright 2015 Richard Thurston.
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
package com.northernwall.hadrian.domain;

import com.northernwall.hadrian.workItem.dao.HostData;
import com.northernwall.hadrian.workItem.dao.ModuleData;
import com.northernwall.hadrian.workItem.dao.ServiceData;
import com.northernwall.hadrian.workItem.dao.TeamData;
import com.northernwall.hadrian.workItem.dao.VipData;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

/**
 *
 * @author Richard Thurston
 */
public class WorkItem {

    private String id;
    private Type type;
    private Operation operation;
    private String nextId;
    private String username;
    private String fullname;
    private Date requestDate;
    private TeamData team;
    private ServiceData service;
    private ModuleData mainModule;
    private List<ModuleData> modules;
    private HostData host;
    private VipData vip;
    private String reason;

    public WorkItem(Type type, Operation operation, User user, Team team, Service service, Module module, Host host, Vip vip, String reason) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.operation = operation;
        this.nextId = null;
        this.username = user.getUsername();
        this.fullname = user.getFullName();
        this.requestDate = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime();
        this.team = TeamData.create(team);
        this.service = ServiceData.create(service);
        this.mainModule = ModuleData.create(module);
        this.modules = new LinkedList<>();
        this.host = HostData.create(host);
        this.vip = VipData.create(vip);
        this.reason = reason;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public String getNextId() {
        return nextId;
    }

    public void setNextId(String nextId) {
        this.nextId = nextId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public TeamData getTeam() {
        return team;
    }

    public void setTeam(TeamData team) {
        this.team = team;
    }

    public ServiceData getService() {
        return service;
    }

    public void setService(ServiceData service) {
        this.service = service;
    }

    public ModuleData getMainModule() {
        return mainModule;
    }

    public void setMainModule(ModuleData module) {
        this.mainModule = module;
    }

    public void addModule(Module module) {
        modules.add(ModuleData.create(module));
    }

    public List<ModuleData> getModules() {
        return modules;
    }

    public HostData getHost() {
        return host;
    }

    public void setHost(HostData host) {
        this.host = host;
    }

    public VipData getVip() {
        return vip;
    }

    public void setVip(VipData vip) {
        this.vip = vip;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("WorkItem{id=");
        str.append(id);
        str.append(", type=");
        str.append(type);
        str.append(", operation=");
        str.append(operation);
        str.append(", nextId=");
        str.append(nextId);
        str.append(", username=");
        str.append(username);
        str.append(", fullname=");
        str.append(fullname);
        str.append(", requestDate=");
        str.append(requestDate);
        str.append(", team=");
        str.append(team);
        str.append(", service=");
        str.append(service);
        if (mainModule != null) {
            str.append(", mainModule=");
            str.append(mainModule.toString());
        }
        if (modules != null && !modules.isEmpty()) {
            str.append(", modules=[");
            for (ModuleData temp : modules) {
                str.append(temp.toString());
                str.append(",");
            }
            str.append("]");
        }
        if (host != null) {
            str.append(", host=");
            str.append(host);
        }
        if (vip != null) {
            str.append(", vip=");
            str.append(vip);
        }
        return str.toString();
    }

}
