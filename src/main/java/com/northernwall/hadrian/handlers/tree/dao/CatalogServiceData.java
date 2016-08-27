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
package com.northernwall.hadrian.handlers.tree.dao;

import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;

public class CatalogServiceData {
    public String teamId;
    public String teamName;
    public String serviceId;
    public String serviceName;
    public String serviceDescription;

    public static CatalogServiceData create(Team team, Service service) {
        CatalogServiceData serviceData = new CatalogServiceData();
        serviceData.teamId = team.getTeamId();
        serviceData.teamName = team.getTeamName();
        serviceData.serviceId = service.getServiceId();
        serviceData.serviceName = service.getServiceName();
        serviceData.serviceDescription = service.getDescription();
        return serviceData;
    }

}
