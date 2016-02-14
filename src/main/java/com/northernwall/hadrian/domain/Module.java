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

import java.util.UUID;

public class Module implements Comparable<Module> {
    private String moduleId;
    private String moduleName;
    private String serviceId;
    private String moduleType;
    private String gitPath;
    private String gitFolder;
    private String mavenGroupId;
    private String mavenArtifactId;
    private String artifactType;
    private String artifactSuffix;
    private String hostAbbr;
    private String versionUrl;
    private String availabilityUrl;
    private String runAs;
    private String startCmdLine;
    private String stopCmdLine;
    private int cmdLineTimeOut;

    public Module(String moduleName, String serviceId, String moduleType, String gitPath, String gitFolder, String mavenGroupId, String mavenArtifactId, String artifactType, String artifactSuffix, String hostAbbr,  String versionUrl, String availabilityUrl, String runAs, String startCmdLine, String stopCmdLine, int cmdLineTimeOut) {
        this.moduleId = UUID.randomUUID().toString();
        this.moduleName = moduleName;
        this.serviceId = serviceId;
        this.moduleType = moduleType;
        this.gitPath = gitPath;
        this.gitFolder = gitFolder;
        this.mavenGroupId = mavenGroupId;
        this.mavenArtifactId = mavenArtifactId;
        this.artifactType = artifactType;
        this.artifactSuffix = artifactSuffix;
        this.hostAbbr = hostAbbr;
        this.versionUrl = versionUrl;
        this.availabilityUrl = availabilityUrl;
        this.runAs = runAs;
        this.startCmdLine = startCmdLine;
        this.stopCmdLine = stopCmdLine;
        this.cmdLineTimeOut = cmdLineTimeOut;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getModuleType() {
        return moduleType;
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

    public String getGitPath() {
        return gitPath;
    }

    public void setGitPath(String gitPath) {
        this.gitPath = gitPath;
    }

    public String getGitFolder() {
        return gitFolder;
    }

    public void setGitFolder(String gitFolder) {
        this.gitFolder = gitFolder;
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

    public String getHostAbbr() {
        return hostAbbr;
    }

    public void setHostAbbr(String hostAbbr) {
        this.hostAbbr = hostAbbr;
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

    public String getRunAs() {
        return runAs;
    }

    public void setRunAs(String runAs) {
        this.runAs = runAs;
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

    @Override
    public int compareTo(Module o) {
        return moduleName.compareTo(o.moduleName);
    }

}
