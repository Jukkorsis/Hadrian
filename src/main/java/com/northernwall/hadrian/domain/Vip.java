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
public class Vip implements Comparable<Vip>{
    private String vipId;
    private String serviceId;
    @com.google.gson.annotations.Expose(serialize = false)
    private boolean busy;
    @com.google.gson.annotations.Expose(serialize = false)
    private String statusCode;
    @com.google.gson.annotations.Expose(serialize = false)
    private String status;
    private String moduleId;
    private String dns;
    private String domain;
    private boolean external;
    private String environment;
    private String inboundProtocol;
    private List<String> inboundModifiers;
    private String outboundProtocol;
    private List<String> outboundModifiers;
    private int vipPort;
    private int servicePort;
    private int httpCheckPort;
    private List<String> blackListHosts;
    private List<String> migratedDCs;
    private List<String> unmigratedDCs;
    
    /**
     * 0 - not migrated
     * 1 - created new LB but DNS points to old LB
     * 2 - DNS points to new LB, migration complete
     */
    private int migration;

    public Vip() {
        this.vipId = UUID.randomUUID().toString();
        this.serviceId = null;
        this.busy = false;
        this.status = Const.STATUS_NO;
        this.statusCode = Const.STATUS_NO;
        this.moduleId = null;
        this.dns = null;
        this.domain = null;
        this.external = false;
        this.environment = null;
        this.inboundProtocol = null;
        this.inboundModifiers = new LinkedList<>();
        this.outboundProtocol = null;
        this.outboundModifiers = new LinkedList<>();
        this.vipPort = 0;
        this.servicePort = 8080;
        this.httpCheckPort = 0;
        this.blackListHosts = new LinkedList<>();
        this.migratedDCs = new LinkedList<>();
        this.unmigratedDCs = new LinkedList<>();
        this.migration = 0;
    }

    public Vip(String serviceId, String moduleId, String dns, String domain, boolean external, String environment, String inboundProtocol, List<String> inboundModifiers, String outboundProtocol, List<String> outboundModifiers, int vipPort, int servicePort, int httpCheckPort) {
        this.vipId = UUID.randomUUID().toString();
        this.serviceId = serviceId;
        this.busy = false;
        this.status = Const.STATUS_NO;
        this.statusCode = Const.STATUS_NO;
        this.moduleId = moduleId;
        this.dns = dns;
        this.domain = domain;
        this.external = external;
        this.environment = environment;
        this.inboundProtocol = inboundProtocol;
        this.inboundModifiers = inboundModifiers;
        this.outboundProtocol = outboundProtocol;
        this.outboundModifiers = outboundModifiers;
        this.vipPort = vipPort;
        this.servicePort = servicePort;
        this.httpCheckPort = httpCheckPort;
        this.blackListHosts = new LinkedList<>();
        this.migration = 0;
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

    public String getInboundProtocol() {
        return inboundProtocol;
    }

    public void setInboundProtocol(String inboundProtocol) {
        this.inboundProtocol = inboundProtocol;
    }

    public List<String> getInboundModifiers() {
        return inboundModifiers;
    }

    public void setInboundModifiers(List<String> inboundModifiers) {
        this.inboundModifiers = inboundModifiers;
    }

    public String getOutboundProtocol() {
        return outboundProtocol;
    }

    public void setOutboundProtocol(String outboundProtocol) {
        this.outboundProtocol = outboundProtocol;
    }
    
    public List<String> getOutboundModifiers() {
        return outboundModifiers;
    }

    public void setOutboundModifiers(List<String> outboundModifiers) {
        this.outboundModifiers = outboundModifiers;
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

    public int getHttpCheckPort() {
        return httpCheckPort;
    }

    public void setHttpCheckPort(int httpCheckPort) {
        this.httpCheckPort = httpCheckPort;
    }

    public List<String> getBlackListHosts() {
        return blackListHosts;
    }

    public int getMigration() {
        return migration;
    }

    public void setMigration(int migration) {
        this.migration = migration;
    }

    public List<String> getMigratedDCs() {
        if (migratedDCs == null) {
            migratedDCs = new LinkedList<>();
        }
        return migratedDCs;
    }

    public List<String> getUnmigratedDCs() {
        if (unmigratedDCs == null) {
            unmigratedDCs = new LinkedList<>();
        }
        return unmigratedDCs;
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
