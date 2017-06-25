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

package com.northernwall.hadrian.handlers.vip.dao;

import com.northernwall.hadrian.domain.Vip;
import java.util.List;

/**
 *
 * @author Richard Thurston
 */
public class GetVipData {
    public static GetVipData create(Vip vip) {
        GetVipData temp = new GetVipData();
        temp.vipId = vip.getVipId();
        temp.serviceId = vip.getServiceId();
        temp.busy = vip.isBusy();
        temp.statusCode = vip.getStatusCode();
        temp.status = vip.getStatus();
        temp.dns = vip.getDns();
        temp.domain = vip.getDomain();
        temp.external = vip.isExternal();
        temp.environment = vip.getEnvironment();
        temp.inboundProtocol = vip.getInboundProtocol();
        temp.outboundProtocol = vip.getOutboundProtocol();
        temp.vipPort = vip.getVipPort();
        temp.servicePort = vip.getServicePort();
        temp.migration = vip.getMigration();
        temp.migratedDCs = vip.getMigratedDCs();
        temp.unmigratedDCs = vip.getUnmigratedDCs();
        temp.inboundText = "";
        temp.outboundText = "";
        return temp;
    }
    
    public String vipId;
    public String serviceId;
    public boolean busy;
    public String status;
    public String statusCode;
    public String dns;
    public String domain;
    public boolean external;
    public String environment;
    public String inboundProtocol;
    public String outboundProtocol;
    public int vipPort;
    public int servicePort;
    public int migration;
    public List<String> migratedDCs;
    public List<String> unmigratedDCs;
    public String inboundText;
    public String outboundText;

}
