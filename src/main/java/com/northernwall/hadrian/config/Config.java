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
package com.northernwall.hadrian.config;

import com.northernwall.hadrian.domain.Environment;
import com.northernwall.hadrian.domain.InboundProtocol;
import java.util.LinkedList;
import java.util.List;

public class Config {
    public String mavenGroupId = "";
    public String versionUrl = "";
    public String availabilityUrl = "";
    public String smokeTestUrl = "";
    public String deploymentFolder = "";
    public String dataFolder = "";
    public String logsFolder = "";
    public String gitUiURL = "";
    public String securityGroupName = "";
    public String moduleConfigName = "";
    public String hostSpecialInstructions = "";
    public String hostSpecialInstructionsTrueSla = "";
    public String hostSpecialInstructionsFalseSla = "";
    public boolean enableHostProvisioning = false;
    public boolean enableHostReboot = false;
    public boolean enableVipProvisioning = false;
    public boolean enableVipMigration = false;
    public boolean enableSshAccess = false;
    public int minCpu = 0;
    public int maxCpu = 0;
    public int minMemory = 0;
    public int maxMemory = 0;
    public int minStorage = 0;
    public int maxStorage = 0;
    public int maxCount = 10;
    public int maxTotalCount = 100;
    public List<String> dataCenters = new LinkedList<>();
    public List<String> environmentNames = new LinkedList<>();
    public List<Environment> environments = new LinkedList<>();
    public List<String> platforms = new LinkedList<>();
    public List<InboundProtocol> inboundProtocols = new LinkedList<>();
    public List<String> domains = new LinkedList<>();
    public List<String> artifactTypes = new LinkedList<>();
    public List<String> scopes = new LinkedList<>();
    public List<String> folderWhiteList = new LinkedList<>();
    
}
