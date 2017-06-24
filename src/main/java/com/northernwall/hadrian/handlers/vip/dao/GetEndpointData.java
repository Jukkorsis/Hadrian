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
package com.northernwall.hadrian.handlers.vip.dao;

import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Vip;
import java.util.List;

/**
 *
 * @author Richard
 */
public class GetEndpointData {

    public static GetEndpointData create(Service service, Module module, Vip vip) {
        GetEndpointData temp = new GetEndpointData();
        
        temp.serviceName = service.getServiceName();

        temp.moduleName = module.getModuleName();
        int i = module.getAvailabilityUrl().indexOf("/");
        temp.monitoringPath = module.getAvailabilityUrl().substring(i);

        temp.dns = vip.getDns();
        temp.domain = vip.getDomain();
        temp.environment = vip.getEnvironment();
        temp.inboundProtocol = vip.getInboundProtocol();
        temp.inboundModifiers = vip.getInboundModifiers();
        temp.outboundProtocol = vip.getOutboundProtocol();
        temp.outboundModifiers = vip.getOutboundModifiers();
        temp.external = vip.isExternal();
        temp.vipPort = vip.getVipPort();
        temp.servicePort = vip.getServicePort();
        temp.httpCheckPort = vip.getHttpCheckPort();
        temp.migration = vip.getMigration();
        return temp;
    }

    public String serviceName;
    public String moduleName;
    public String dns;
    public String domain;
    public String environment;
    public String inboundProtocol;
    public List<String> inboundModifiers;
    public String outboundProtocol;
    public List<String> outboundModifiers;
    public boolean external;
    public int vipPort;
    public int servicePort;
    public int httpCheckPort;
    public int migration;
    public String monitoringPath;
}
