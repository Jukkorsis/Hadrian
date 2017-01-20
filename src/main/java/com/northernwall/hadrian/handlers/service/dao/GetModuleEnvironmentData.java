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

import com.northernwall.hadrian.handlers.vip.dao.GetVipData;
import com.northernwall.hadrian.handlers.host.dao.GetHostData;
import java.util.LinkedList;
import java.util.List;

public class GetModuleEnvironmentData {
    public String moduleId;
    public String moduleName;
    public String environment;
    public boolean hasSmokeTest;
    public List<GetHostData> hosts = new LinkedList<>();
    public List<GetVipData> vips = new LinkedList<>();
    public List<GetCustomFunctionData> cfs = new LinkedList<>();

    public GetModuleEnvironmentData(String moduleId, String moduleName, String environment, boolean hasSmokeTest) {
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.environment = environment;
        this.hasSmokeTest = hasSmokeTest;
    }

}
