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
package com.northernwall.hadrian.domain;

import java.util.LinkedList;
import java.util.List;

public class Config {
    public String versionUrl = "";
    public String availabilityUrl = "";
    public String startCmd = "";
    public String stopCmd = "";
    public List<String> dataCenters = new LinkedList<>();
    public List<String> networks = new LinkedList<>();
    public List<String> envs = new LinkedList<>();
    public List<String> sizes = new LinkedList<>();
    public List<String> protocols = new LinkedList<>();
    public List<String> domains = new LinkedList<>();
    public List<String> serviceTypes = new LinkedList<>();
    public List<String> gitModes = new LinkedList<>();
    public List<String> moduleTypes = new LinkedList<>();
    public List<String> artifactTypes = new LinkedList<>();
    public List<String> templates = new LinkedList<>();
    
}
