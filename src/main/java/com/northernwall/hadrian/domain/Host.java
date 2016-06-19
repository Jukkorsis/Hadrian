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

import java.util.UUID;

/**
 *
 * @author Richard Thurston
 */
public class Host implements Comparable<Host> {
    private String hostId;
    private String hostName;
    private String serviceId;
    private String moduleId;
    private String status;
    private String dataCenter;
    private String network;
    private String env;

    public Host() {
        this.hostId = UUID.randomUUID().toString();
        this.hostName = null;
        this.serviceId = null;
        this.status = "-";
        this.dataCenter = null;
        this.network = null;
        this.env = null;
    }

    public Host(String hostName, String serviceId, String status, String moduleId, String dataCenter, String network, String env) {
        this.hostId = UUID.randomUUID().toString();
        this.hostName = hostName;
        this.serviceId = serviceId;
        this.status = status;
        this.moduleId = moduleId;
        this.dataCenter = dataCenter;
        this.network = network;
        this.env = env;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    @Override
    public int compareTo(Host o) {
        return hostName.compareTo(o.hostName);
    }

}
