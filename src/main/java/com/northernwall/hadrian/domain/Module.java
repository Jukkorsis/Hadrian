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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Module implements Comparable<Module> {
    private String moduleId;
    private String moduleName;
    private String serviceId;
    private int order;
    private ModuleType moduleType;
    private String gitProject;
    private String gitFolder;
    private String mavenGroupId;
    private String mavenArtifactId;
    private String artifactType;
    private String artifactSuffix;
    private String hostAbbr;
    private String hostname;
    private String versionUrl;
    private String availabilityUrl;
    private String runAs;
    private String deploymentFolder;
    private String dataFolder;
    private String logsFolder;
    private int logsRetention;
    private String startCmdLine;
    private int startTimeOut;
    private String stopCmdLine;
    private int stopTimeOut;
    private String configName;
    private Map<String,Boolean> networkNames = new HashMap<>();

    public Module(String moduleName, String serviceId, int order, ModuleType moduleType, String gitProject, String gitFolder, String mavenGroupId, String mavenArtifactId, String artifactType, String artifactSuffix, String hostAbbr,  String hostname,  String versionUrl, String availabilityUrl, String runAs, String deploymentFolder, String dataFolder, String logsFolder, int logsRetention ,String startCmdLine, int startTimeOut, String stopCmdLine, int stopTimeOut, String configName, Map<String,Boolean> networkNames) {
        this.moduleId = UUID.randomUUID().toString();
        this.moduleName = moduleName;
        this.serviceId = serviceId;
        this.order = order;
        this.moduleType = moduleType;
        this.gitProject = gitProject;
        this.gitFolder = gitFolder;
        this.mavenGroupId = mavenGroupId;
        this.mavenArtifactId = mavenArtifactId;
        this.artifactType = artifactType;
        this.artifactSuffix = artifactSuffix;
        this.hostAbbr = hostAbbr;
        this.hostname = hostname;
        this.versionUrl = versionUrl;
        this.availabilityUrl = availabilityUrl;
        this.runAs = runAs;
        this.deploymentFolder = deploymentFolder;
        this.dataFolder = dataFolder;
        this.logsFolder = logsFolder;
        this.logsRetention = logsRetention;
        this.startCmdLine = startCmdLine;
        this.startTimeOut = startTimeOut;
        this.stopCmdLine = stopCmdLine;
        this.stopTimeOut = stopTimeOut;
        this.configName = configName;
        this.networkNames = networkNames;
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

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public ModuleType getModuleType() {
        return moduleType;
    }

    public void setModuleType(ModuleType moduleType) {
        this.moduleType = moduleType;
    }

    public String getGitProject() {
        return gitProject;
    }

    public void setGitProject(String gitProject) {
        this.gitProject = gitProject;
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

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
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

    public String getDeploymentFolder() {
        return deploymentFolder;
    }

    public void setDeploymentFolder(String deploymentFolder) {
        this.deploymentFolder = deploymentFolder;
    }

    public String getDataFolder() {
        return dataFolder;
    }

    public void setDataFolder(String dataFolder) {
        this.dataFolder = dataFolder;
    }

    public String getLogsFolder() {
        return logsFolder;
    }

    public void setLogsFolder(String logsFolder) {
        this.logsFolder = logsFolder;
    }

    public int getLogsRetention() {
        return logsRetention;
    }

    public void setLogsRetention(int logsRetention) {
        this.logsRetention = logsRetention;
    }

    public String getStartCmdLine() {
        return startCmdLine;
    }

    public void setStartCmdLine(String startCmdLine) {
        this.startCmdLine = startCmdLine;
    }

    public int getStartTimeOut() {
        return startTimeOut;
    }

    public void setStartTimeOut(int startTimeOut) {
        this.startTimeOut = startTimeOut;
    }

    public String getStopCmdLine() {
        return stopCmdLine;
    }

    public void setStopCmdLine(String stopCmdLine) {
        this.stopCmdLine = stopCmdLine;
    }
    
    public int getStopTimeOut() {
        return stopTimeOut;
    }

    public void setStopTimeOut(int stopTimeOut) {
        this.stopTimeOut = stopTimeOut;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public Map<String, Boolean> getNetworkNames() {
        return networkNames;
    }

    public void setNetworkNames(Map<String, Boolean> networkNames) {
        this.networkNames = networkNames;
    }
    
    public void cleanNetworkNames() {
        if (networkNames == null || networkNames.isEmpty()) {
            return;
        }
        List<String> keys = null;
        for (String key : networkNames.keySet()) {
            if (!networkNames.get(key).booleanValue()) {
                if (keys == null) {
                    keys = new LinkedList<>();
                }
                keys.add(key);
            }
        }
        if (keys == null) {
            return;
        }
        for (String key : keys) {
            networkNames.remove(key);
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + Objects.hashCode(this.moduleId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Module other = (Module) obj;
        if (!Objects.equals(this.moduleId, other.moduleId)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Module o) {
        int result = order - o.order;
        if (result == 0) {
            result = moduleId.compareTo(o.moduleId);
        }
        return result;
    }

}
