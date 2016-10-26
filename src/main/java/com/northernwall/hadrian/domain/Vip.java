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

import com.northernwall.hadrian.Const;
import java.util.UUID;

/**
 *
 * @author Richard Thurston
 */
public class Vip implements Comparable<Vip>{
    private String vipId;
    private String serviceId;
    private boolean busy;
    private String status;
    private String moduleId;
    private String dns;
    private String domain;
    private boolean external;
    public String network; //TODO remove in next release
    private String environment;
    private String protocol;
    private int vipPort;
    private int servicePort;

    public Vip() {
        this.vipId = UUID.randomUUID().toString();
        this.serviceId = null;
        this.busy = false;
        this.status = Const.NO_STATUS;
        this.moduleId = null;
        this.dns = null;
        this.domain = null;
        this.external = false;
        this.environment = null;
        this.protocol = "HTTP";
        this.vipPort = 80;
        this.servicePort = 8080;
    }

    public Vip(String serviceId, String status, String moduleId, String dns, String domain, boolean external, String environment, String protocol, int vipPort, int servicePort) {
        this.vipId = UUID.randomUUID().toString();
        this.serviceId = serviceId;
        this.busy = false;
        this.status = status;
        this.moduleId = moduleId;
        this.dns = dns;
        this.domain = domain;
        this.external = external;
        this.environment = environment;
        this.protocol = protocol;
        this.vipPort = vipPort;
        this.servicePort = servicePort;
    }

    public String getVipId() {
        return vipId;
    }

    public void setVipId(String vipId) {
        this.vipId = vipId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(boolean busy, String status) {
        this.busy = busy;
        this.status = status;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getDns() {
        return dns;
    }

    public void setDns(String dns) {
        this.dns = dns;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    public int getVipPort() {
        return vipPort;
    }

    public void setVipPort(int vipPort) {
        this.vipPort = vipPort;
    }

    public int getServicePort() {
        return servicePort;
    }

    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }

    @Override
    public int compareTo(Vip o) {
        int temp = dns.compareTo(o.dns);
        if (temp != 0) {
            return temp;
        }
        temp = domain.compareTo(o.domain);
        if (temp != 0) {
            return temp;
        }
        return vipPort - o.vipPort;
    }

}
