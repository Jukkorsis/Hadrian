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

import com.northernwall.hadrian.domain.CustomFunction;

public class GetCustomFunctionData {
    public String customFunctionId;
    public String name;
    public String serviceId;
    public String method;
    public String url;
    public String helpText;
    public boolean teamOnly;

    public static GetCustomFunctionData create(CustomFunction customFunction) {
        GetCustomFunctionData temp = new GetCustomFunctionData();
        temp.customFunctionId = customFunction.getCustomFunctionId();
        temp.name = customFunction.getName();
        temp.serviceId = customFunction.getServiceId();
        temp.method = customFunction.getMethod();
        temp.url = customFunction.getUrl();
        temp.helpText = customFunction.getHelpText();
        temp.teamOnly = customFunction.isTeamOnly();
        return temp;
    }

}