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
package com.northernwall.hadrian.handlers.host.dao;

import java.util.Map;

public class PostHostData {
    public String serviceId;
    public String moduleId;
    public String environment;
    public String platform;
    public int sizeCpu;
    public int sizeMemory;
    public int sizeStorage;
    public String version;
    public String configVersion;
    public Map<String, Integer> counts;
    public String specialInstructions;
    public String reason;

}
