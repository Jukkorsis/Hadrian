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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Richard Thurston
 */
public class PutModuleData {
    public String serviceId;
    public String moduleId;
    public String moduleName;
    public String mavenArtifactId;
    public String artifactType;
    public String artifactSuffix;
    public String outbound;
    public String hostAbbr;
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
    public Map<String,Boolean> networkNames = new HashMap<>();

}
