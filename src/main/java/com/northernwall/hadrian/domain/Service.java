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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
    private String businessImpact;
    private String piiUsage;
    private String runAs;
    private String gitPath;
    private String mavenGroupId;
    private String mavenArtifactId;
    private String artifactType;
    private String artifactSuffix;
    private String versionUrl;
    private String availabilityUrl;
    private String startCmdLine;
    private String stopCmdLine;
    private int cmdLineTimeOut;
    private Date creationDate;
    private final Map<String, String> links = new HashMap<>();

    public Service(String serviceAbbr, String serviceName, String teamId, String description, String businessImpact, String piiUsage, String runAs, String gitPath, String mavenGroupId, String mavenArtifactId, String artifactType, String artifactSuffix, String versionUrl, String availabilityUrl, String startCmdLine, String stopCmdLine, int cmdLineTimeOut) {
        this.serviceId = UUID.randomUUID().toString();
        this.serviceAbbr = serviceAbbr;
        this.serviceName = serviceName;
        this.teamId = teamId;
        this.description = description;
        this.businessImpact = businessImpact;
        this.piiUsage = piiUsage;
        this.runAs = runAs;
        this.gitPath = gitPath;
        this.mavenGroupId = mavenGroupId;
        this.mavenArtifactId = mavenArtifactId;
        this.artifactType = artifactType;
        this.artifactSuffix = artifactSuffix;
        this.versionUrl = versionUrl;
        this.availabilityUrl = availabilityUrl;
        this.startCmdLine = startCmdLine;
        this.stopCmdLine = stopCmdLine;
        this.cmdLineTimeOut = cmdLineTimeOut;
        this.creationDate = new Date();
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

    public String getBusinessImpact() {
        return businessImpact;
    }

    public void setBusinessImpact(String businessImpact) {
        this.businessImpact = businessImpact;
    }

    public String getPiiUsage() {
        return piiUsage;
    }

    public void setPiiUsage(String piiUsage) {
        this.piiUsage = piiUsage;
    }

    public String getRunAs() {
        return runAs;
    }

    public void setRunAs(String runAs) {
        this.runAs = runAs;
    }

    public String getGitPath() {
        return gitPath;
    }

    public void setGitPath(String gitPath) {
        this.gitPath = gitPath;
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

    public String getArtifactType() {
        return artifactType;
    }

    public void setArtifactType(String artifactType) {
        this.artifactType = artifactType;
    }

    public String getArtifactSuffix() {
        return artifactSuffix;
    }

    public void setArtifactSuffix(String artifactSuffix) {
        this.artifactSuffix = artifactSuffix;
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

    public String getStartCmdLine() {
        return startCmdLine;
    }

    public void setStartCmdLine(String startCmdLine) {
        this.startCmdLine = startCmdLine;
    }

    public String getStopCmdLine() {
        return stopCmdLine;
    }

    public void setStopCmdLine(String stopCmdLine) {
        this.stopCmdLine = stopCmdLine;
    }

    public int getCmdLineTimeOut() {
        return cmdLineTimeOut;
    }

    public void setCmdLineTimeOut(int cmdLineTimeOut) {
        this.cmdLineTimeOut = cmdLineTimeOut;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Map<String, String> getLinks() {
        return links;
    }

    @Override
    public int compareTo(Service o) {
        return serviceName.compareTo(o.serviceName);
    }

}
