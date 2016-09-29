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

import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleType;

public class ModuleData {
    public String moduleId;
    public String moduleName;
    public int order;
    public ModuleType moduleType;
    public String template;
    public String gitProject;
    public String gitFolder;
    public String mavenGroupId;
    public String mavenArtifactId;
    public String artifactType;
    public String artifactSuffix;
    public String outbound;
    public String hostAbbr;
    public String hostname;
    public String versionUrl;
    public String availabilityUrl;
    public String runAs;
    public String deploymentFolder;
    public String dataFolder;
    public String logsFolder;
    public int logsRetention;
    public String startCmdLine;
    public int startTimeOut;
    public String stopCmdLine;
    public int stopTimeOut;
    public String configName;
    public String testStyle;

    public static ModuleData create(Module module) {
        if (module == null) {
            return null;
        }
        ModuleData temp = new ModuleData();
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
        temp.outbound = module.getOutbound();
        temp.hostAbbr = module.getHostAbbr();
        temp.hostname = module.getHostname();
        temp.versionUrl = module.getVersionUrl();
        temp.availabilityUrl = module.getAvailabilityUrl();
        temp.runAs = module.getRunAs();
        temp.deploymentFolder = module.getDeploymentFolder();
        temp.dataFolder = module.getDataFolder();
        temp.logsFolder = module.getLogsFolder();
        temp.logsRetention = module.getLogsRetention();
        temp.startCmdLine = module.getStartCmdLine();
        temp.startTimeOut = module.getStartTimeOut();
        temp.stopCmdLine = module.getStopCmdLine();
        temp.stopTimeOut = module.getStopTimeOut();
        temp.configName = module.getConfigName();
        temp.testStyle = module.getTestStyle();
        return temp;
    }

    @Override
    public String toString() {
        return "Module{" + "moduleId=" + moduleId + ", moduleName=" + moduleName + ", order=" + order + ", moduleType=" + moduleType + '}';
    }

}
