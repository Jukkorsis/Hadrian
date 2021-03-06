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

package com.northernwall.hadrian.workItem.dao;

import com.northernwall.hadrian.domain.Vip;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Richard Thurston
 */
public class VipData {
    public static VipData create(Vip vip) {
        if (vip == null) {
            return null;
        }
        VipData temp = new VipData();
        temp.vipId = vip.getVipId();
        temp.dns = vip.getDns();
        temp.domain = vip.getDomain();
        temp.external = vip.isExternal();
        temp.environment = vip.getEnvironment();
        temp.inboundProtocol = vip.getInboundProtocol();
        temp.inboundModifiers = vip.getInboundModifiers();
        temp.outboundProtocol = vip.getOutboundProtocol();
        temp.outboundModifiers = vip.getOutboundModifiers();
        temp.vipPort = vip.getVipPort();
        temp.servicePort = vip.getServicePort();
        temp.httpCheckPort = vip.getHttpCheckPort();
        temp.migration = vip.getMigration();
        temp.migrateDCs = new LinkedList<>();
        
        return temp;
    }
    
    public String vipId;
    public String dns;
    public String domain;
    public boolean external;
    public String environment;
    public String inboundProtocol;
    public List<String> inboundModifiers;
    public String outboundProtocol;
    public List<String> outboundModifiers;
    public int vipPort;
    public int servicePort;
    public int httpCheckPort;
    public int migration;
    public List<String> migrateDCs;

}
