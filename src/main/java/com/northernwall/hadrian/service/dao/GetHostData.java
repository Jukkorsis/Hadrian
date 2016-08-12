/*
 * Copyright 2014 Richard Thurston.
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
package com.northernwall.hadrian.service.dao;

import com.northernwall.hadrian.domain.Host;

public class GetHostData {
    public String hostId;
    public String hostName;
    public String serviceId;
    public String status;
    public boolean busy;
    public String dataCenter;
    public String network;
    public String env;
    public String version;
    public int availability;

    public static GetHostData create(Host host) {
        GetHostData temp = new GetHostData();
        temp.hostId = host.getHostId();
        temp.hostName = host.getHostName();
        temp.serviceId = host.getServiceId();
        temp.status = host.getStatus();
        temp.busy = host.isBusy();
        temp.dataCenter = host.getDataCenter();
        temp.network = host.getNetwork();
        temp.env = host.getEnv();
        temp.version = "-";
        temp.availability = 0;
        return temp;
    }

}
