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
package com.northernwall.hadrian.handlers.service.dao;

import com.northernwall.hadrian.domain.Document;
import com.northernwall.hadrian.domain.FindBugsLevel;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class GetServiceData {
    public static GetServiceData create(Service service) {
        GetServiceData temp = new GetServiceData();
        temp.serviceId = service.getServiceId();
        temp.serviceName = service.getServiceName();
        temp.teamId = service.getTeamId();
        temp.description = service.getDescription();
        temp.serviceType = service.getServiceType();
        temp.gitProject = service.getGitProject();
        temp.scope = service.getScope();
        temp.mavenGroupId = service.getMavenGroupId();
        
        temp.doBuilds = service.isDoBuilds();
        temp.doDeploys = service.isDoDeploys();
        temp.doManageVip = service.isDoManageVip();
        temp.doCheckJar = service.isDoCheckJar();
        temp.doFindBugsLevel = service.getDoFindBugsLevel();
        
        temp.testStyle = service.getTestStyle();
        temp.testHostname = service.getTestHostname();
        temp.testRunAs = service.getTestRunAs();
        temp.testDeploymentFolder = service.getTestDeploymentFolder();
        temp.testCmdLine = service.getTestCmdLine();
        temp.testTimeOut = service.getTestTimeOut();
        temp.smokeTestCron = service.getSmokeTestCron();

        temp.leftDocuments = new LinkedList<>();
        temp.middleDocuments = new LinkedList<>();
        temp.rightDocuments = new LinkedList<>();
        Collections.sort(service.getDocuments());
        for (Document doc : service.getDocuments()) {
            if (temp.middleDocuments.size() == temp.rightDocuments.size()) {
                if (temp.leftDocuments.size() == temp.middleDocuments.size()) {
                    temp.leftDocuments.add(doc);
                } else {
                    temp.middleDocuments.add(doc);
                }
            } else {
                temp.rightDocuments.add(doc);
            }
        }

        temp.creationDate = service.getCreationDate();
        temp.deletionDate = service.getDeletionDate();
        temp.active = service.isActive();
        temp.modules = new LinkedList<>();
        temp.environments = new LinkedList<>();
        temp.dataStores = new LinkedList<>();
        return temp;
    }

    public String serviceId;
    public String serviceName;
    public String teamId;
    public String description;
    public String serviceType;
    public String gitProject;
    public String scope;
    public String mavenGroupId;
    public boolean doBuilds;
    public boolean doDeploys;
    public boolean doManageVip;
    public boolean doCheckJar;
    public FindBugsLevel doFindBugsLevel;
    public String testStyle;
    public String testHostname;
    public String testRunAs;
    public String testDeploymentFolder;
    public String testCmdLine;
    public int testTimeOut;
    public String smokeTestCron;
    public List<Document> leftDocuments;
    public List<Document> middleDocuments;
    public List<Document> rightDocuments;
    public Date creationDate;
    public Date deletionDate;
    public boolean active;
    public List<GetModuleData> modules;
    public List<GetEnvironmentData> environments;
    public List<GetDataStoreData> dataStores;
    public boolean canModify;


    public void addEnvironment(String environment) {
        for (GetEnvironmentData environmentData : environments) {
            if (environmentData.environment.equals(environment)) {
                return;
            }
        }
        GetEnvironmentData environmentData = new GetEnvironmentData();
        environmentData.environment = environment;
        environments.add(environmentData);
    }

    public void addModuleEnvironment(Module module, String environment) {
        for (GetEnvironmentData environmentData : environments) {
            if (environmentData.environment.equals(environment)) {
                environmentData.addModule(module);
                return;
            }
        }
    }

    public void addHost(GetHostData hostData, GetModuleData moduleData) {
        for (GetEnvironmentData environmentData : environments) {
            if (environmentData.environment.equals(hostData.environment)) {
                environmentData.addHost(hostData, moduleData);
                return;
            }
        }
    }

    public void addVip(GetVipData vipData, GetModuleData moduleData) {
        for (GetEnvironmentData environmentData : environments) {
            if (environmentData.environment.equals(vipData.environment)) {
                environmentData.addVip(vipData, moduleData);
                return;
            }
        }
    }

    public void addCustomFunction(GetCustomFunctionData customFunctionData) {
        for (GetModuleData moduleData : modules) {
            if (customFunctionData.moduleId.equals(moduleData.moduleId)) {
                moduleData.customFunctions.add(customFunctionData);
            }
        }
        for (GetEnvironmentData environmentData : environments) {
            environmentData.addCustomFunction(customFunctionData);
        }
    }

}
