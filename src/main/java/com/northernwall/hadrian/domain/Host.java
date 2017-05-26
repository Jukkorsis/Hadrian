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

import com.northernwall.hadrian.config.Const;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Richard Thurston
 */
public class Host implements Comparable<Host> {
    public static List<Host> filterModule(String moduleId, List<Host> hosts) {
        if (hosts == null || hosts.isEmpty() || moduleId == null) {
            return null;
        }
        List<Host> temp = new LinkedList<>();
        for (Host host : hosts) {
            if (host.getModuleId().equals(moduleId)) {
                temp.add(host);
            }
        }
        return temp;
    }

    public static List<Host> filterModule(String moduleId, String environment, List<Host> hosts) {
        if (hosts == null || hosts.isEmpty() || moduleId == null) {
            return null;
        }
        List<Host> temp = new LinkedList<>();
        for (Host host : hosts) {
            if (host.getModuleId().equals(moduleId) 
                    && host.getEnvironment().equals(environment)) {
                temp.add(host);
            }
        }
        return temp;
    }

    private String hostId;
    private String hostName;
    private String serviceId;
    private String moduleId;
    @com.google.gson.annotations.Expose(serialize = false)
    private boolean busy;
    @com.google.gson.annotations.Expose(serialize = false)
    private String statusCode;
    @com.google.gson.annotations.Expose(serialize = false)
    private String status;
    private String dataCenter;
    private String environment;
    private String comment;

    public Host() {
        this.hostId = UUID.randomUUID().toString();
        this.hostName = null;
        this.serviceId = null;
        this.busy = false;
        this.status = Const.STATUS_NO;
        this.statusCode = Const.STATUS_NO;
        this.dataCenter = null;
        this.environment = null;
        this.comment = null;
    }

    public Host(String hostName, String serviceId, String moduleId, String dataCenter, String environment) {
        this.hostId = UUID.randomUUID().toString();
        this.hostName = hostName;
        this.serviceId = serviceId;
        this.busy = false;
        this.status = Const.STATUS_NO;
        this.statusCode = Const.STATUS_NO;
        this.moduleId = moduleId;
        this.dataCenter = dataCenter;
        this.environment = environment;
        this.comment = null;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public boolean isBusy() {
        return busy;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(boolean busy, String status, String statusCode) {
        this.busy = busy;
        this.status = status;
        this.statusCode = statusCode;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(String dataCenter) {
        this.dataCenter = dataCenter;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public int compareTo(Host o) {
        return hostName.compareTo(o.hostName);
    }

}
