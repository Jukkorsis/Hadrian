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

import com.northernwall.hadrian.domain.Host;

public class HostData {
    public static HostData create(Host host) {
        if (host == null) {
            return null;
        }
        HostData temp = new HostData();
        temp.hostId = host.getHostId();
        temp.hostName = host.getHostName();
        temp.dataCenter = host.getDataCenter();
        temp.environment = host.getEnvironment();
        temp.platform = host.getPlatform();
        temp.version = null;
        temp.prevVersion = null;
        temp.versionUrl = null;
        temp.configVersion = null;
        
        return temp;
    }
    
    public String hostId;
    public String hostName;
    public String dataCenter;
    public String environment;
    public String platform;
    public String version;
    public String prevVersion;
    public String versionUrl;
    public String configVersion;
    public boolean doOsUpgrade = false;


}
