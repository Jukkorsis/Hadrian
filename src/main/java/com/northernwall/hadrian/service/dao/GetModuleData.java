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

import com.northernwall.hadrian.domain.Config;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleType;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GetModuleData {

    public String moduleId;
    public String moduleName;
    public int order;
    public ModuleType moduleType;
    public String gitProject;
    public String gitFolder;
    public String mavenGroupId;
    public String mavenArtifactId;
    public String artifactType;
    public String artifactSuffix;
    public String hostAbbr;
    public String versionUrl;
    public String availabilityUrl;
    public String runAs;
    public String deploymentFolder;
    public String startCmdLine;
    public int startTimeOut;
    public String stopCmdLine;
    public int stopTimeOut;
    public String configName;
    public Map<String,Boolean> networkNames = new HashMap<>();
    public List<String> versions;
    public List<String> configVersions;
    public List<GetModuleNetworkData> networks;
    public List<GetCustomFunctionData> customFunctions;
    public List<GetModuleRefData> uses;
    public List<GetModuleRefData> usedBy;
    
    public static GetModuleData create(Module module, Config config) {
        GetModuleData temp = new GetModuleData();
        temp.moduleId = module.getModuleId();
        temp.moduleName = module.getModuleName();
        temp.order = module.getOrder();
        temp.moduleType = module.getModuleType();
        temp.gitProject = module.getGitProject();
        temp.gitFolder = module.getGitFolder();
        temp.mavenGroupId = module.getMavenGroupId();
        temp.mavenArtifactId = module.getMavenArtifactId();
        temp.artifactType = module.getArtifactType();
        temp.artifactSuffix = module.getArtifactSuffix();
        temp.hostAbbr = module.getHostAbbr();
        temp.versionUrl = module.getVersionUrl();
        temp.availabilityUrl = module.getAvailabilityUrl();
        temp.runAs = module.getRunAs();
        temp.deploymentFolder = module.getDeploymentFolder();
        temp.startCmdLine = module.getStartCmdLine();
        temp.startTimeOut = module.getStartTimeOut();
        temp.stopCmdLine = module.getStopCmdLine();
        temp.stopTimeOut = module.getStopTimeOut();
        temp.configName = module.getConfigName();
        temp.networkNames = module.getNetworkNames();
        temp.versions = new LinkedList<>();
        temp.networks = new LinkedList<>();
        for (String network : config.networkNames) {
            temp.networks.add(new GetModuleNetworkData(network));
        }
        temp.customFunctions = new LinkedList<>();
        temp.uses = new LinkedList<>();
        temp.usedBy = new LinkedList<>();
        return temp;
    }

    public void addHost(GetHostData hostData) {
        for (GetModuleNetworkData getModuleNetworkData : networks) {
            if (getModuleNetworkData.network.equals(hostData.network)) {
                getModuleNetworkData.hosts.add(hostData);
                return;
            }
        }
    }

    public void addVip(GetVipData vipData) {
        for (GetModuleNetworkData getModuleNetworkData : networks) {
            if (getModuleNetworkData.network.equals(vipData.network)) {
                getModuleNetworkData.vips.add(vipData);
                return;
            }
        }
    }
    
    public List<GetVipData> getVips(String network) {
        for (GetModuleNetworkData getModuleNetworkData : networks) {
            if (getModuleNetworkData.network.equals(network)) {
                return getModuleNetworkData.vips;
            }
        }
        return new LinkedList<>();
    }

    public void addCustomFunction(GetCustomFunctionData customFunctionData) {
        customFunctions.add(customFunctionData);
    }
    
}
