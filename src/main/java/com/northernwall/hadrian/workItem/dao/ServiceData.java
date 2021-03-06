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

import com.northernwall.hadrian.domain.FindBugsLevel;
import com.northernwall.hadrian.domain.Service;

public class ServiceData {
    public static ServiceData create(Service service) {
        if (service == null) {
            return null;
        }
        ServiceData temp = new ServiceData();
        temp.serviceId = service.getServiceId();
        temp.serviceName = service.getServiceName();
        temp.teamId = service.getTeamId();
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

        return temp;
    }
    
    public String serviceId;
    public String serviceName;
    public String teamId;
    public String serviceType;
    public String gitProject;
    public String scope;
    public String mavenGroupId;
    public boolean doBuilds;
    public boolean doDeploys;
    public boolean doManageVip;
    public boolean doCheckJar;
    public String testStyle;
    public String testHostname;
    public String testRunAs;
    public String testDeploymentFolder;
    public String testCmdLine;
    public int testTimeOut;
    public FindBugsLevel doFindBugsLevel;


}
