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

package com.northernwall.hadrian.handlers.service.dao;

import com.northernwall.hadrian.domain.FindBugsLevel;

/**
 *
 * @author Richard Thurston
 */
public class PostServiceData {
    public String serviceName;
    public String teamId;
    public String description;
    public String serviceType;
    public String scope;
    public boolean haFunctionality;
    public boolean haPerformance;
    public boolean haData;
    public String haNotes;
    public String gitProject;
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

}
