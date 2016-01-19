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
import com.northernwall.hadrian.workItem.dao.ServiceData;
import com.northernwall.hadrian.workItem.dao.TeamData;
import com.northernwall.hadrian.workItem.dao.VipData;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author Richard Thurston
 */
public class WorkItem {

    private String id;
    private String type;
    private String operation;
    private String nextId;
    private String username;
    private String fullname;
    private Date requestDate;
    private TeamData team;
    private ServiceData service;
    private HostData host;
    private VipData vip;
    private VipData newVip;

    public WorkItem(String type, String operation, User user, Team team, Service service, Host host, Vip vip, Vip newVip) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.operation = operation;
        this.nextId = null;
        this.username = user.getUsername();
        this.fullname = user.getFullName();
        this.requestDate = new Date();
        this.team = TeamData.create(team);
        this.service = ServiceData.create(service);
        this.host = HostData.create(host);
        this.vip = VipData.create(vip);
        this.newVip = VipData.create(newVip);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
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

    public VipData getNewVip() {
        return newVip;
    }

    public void setNewVip(VipData newVip) {
        this.newVip = newVip;
    }

}
