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

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Richard Thurston
 */
public class Service implements Comparable<Service>{
    private String serviceId;
    private String serviceAbbr;
    private String serviceName;
    private String teamId;
    private String description;
    private String mavenGroupId;
    private String mavenArtifactId;
    private String versionUrl;
    private String availabilityUrl;
    private List<String> customFunctions;

    public Service(String serviceAbbr, String serviceName, String teamId, String description, String mavenGroupId, String mavenArtifactId, String versionUrl, String availabilityUrl) {
        this.serviceId = UUID.randomUUID().toString();
        this.serviceAbbr = serviceAbbr;
        this.serviceName = serviceName;
        this.teamId = teamId;
        this.description = description;
        this.mavenGroupId = mavenGroupId;
        this.mavenArtifactId = mavenArtifactId;
        this.versionUrl = versionUrl;
        this.availabilityUrl = availabilityUrl;
        this.customFunctions = new LinkedList<>();
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceAbbr() {
        return serviceAbbr;
    }

    public void setServiceAbbr(String serviceAbbr) {
        this.serviceAbbr = serviceAbbr;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMavenGroupId() {
        return mavenGroupId;
    }

    public void setMavenGroupId(String mavenGroupId) {
        this.mavenGroupId = mavenGroupId;
    }

    public String getMavenArtifactId() {
        return mavenArtifactId;
    }

    public void setMavenArtifactId(String mavenArtifactId) {
        this.mavenArtifactId = mavenArtifactId;
    }

    public String getVersionUrl() {
        return versionUrl;
    }

    public void setVersionUrl(String versionUrl) {
        this.versionUrl = versionUrl;
    }

    public String getAvailabilityUrl() {
        return availabilityUrl;
    }

    public void setAvailabilityUrl(String availabilityUrl) {
        this.availabilityUrl = availabilityUrl;
    }

    public List<String> getCustomFunctions() {
        return customFunctions;
    }

    @Override
    public int compareTo(Service o) {
        return serviceName.compareTo(o.serviceName);
    }

}
