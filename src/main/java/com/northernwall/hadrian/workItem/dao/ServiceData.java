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

import com.northernwall.hadrian.domain.Service;

public class ServiceData {
    public String serviceId;
    public String serviceAbbr;
    public String serviceName;
    public String teamId;
    public String template;
    public String gitPath;
    public String mavenGroupId;
    public String mavenArtifactId;
    public String artifactType;
    public String artifactSuffix;
    public String versionUrl;
    public String availabilityUrl;
    public String runAs;
    public String startCmdLine;
    public String stopCmdLine;

    public static ServiceData create(Service service) {
        if (service == null) {
            return null;
        }
        ServiceData temp = new ServiceData();
        temp.serviceId = service.getServiceId();
        temp.serviceAbbr = service.getServiceAbbr();
        temp.serviceName = service.getServiceName();
        temp.teamId = service.getTeamId();
        temp.runAs = service.getRunAs();
        temp.gitPath = service.getGitPath();
        temp.mavenGroupId = service.getMavenGroupId();
        temp.mavenArtifactId = service.getMavenArtifactId();
        temp.artifactType = service.getArtifactType();
        temp.artifactSuffix = service.getArtifactSuffix();
        temp.versionUrl = service.getVersionUrl();
        temp.availabilityUrl = service.getAvailabilityUrl();
        temp.startCmdLine = service.getStartCmdLine();
        temp.stopCmdLine = service.getStopCmdLine();
        return temp;
    }

}
