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

package com.northernwall.hadrian.domain;

/**
 *
 * @author Richard Thurston
 */
public class ModuleRef {
    private String clientServiceId;
    private String clientModuleId;
    private String serverServiceId;
    private String serverModuleId;

    public ModuleRef(String clientServiceId, String clientModuleId, String serverServiceId, String serverModuleId) {
        this.clientServiceId = clientServiceId;
        this.clientModuleId = clientModuleId;
        this.serverServiceId = serverServiceId;
        this.serverModuleId = serverModuleId;
    }

    public String getClientServiceId() {
        return clientServiceId;
    }

    public void setClientServiceId(String clientServiceId) {
        this.clientServiceId = clientServiceId;
    }

    public String getClientModuleId() {
        return clientModuleId;
    }

    public void setClientModuleId(String clientModuleId) {
        this.clientModuleId = clientModuleId;
    }

    public String getServerServiceId() {
        return serverServiceId;
    }

    public void setServerServiceId(String serverServiceId) {
        this.serverServiceId = serverServiceId;
    }

    public String getServerModuleId() {
        return serverModuleId;
    }

    public void setServerModuleId(String serverModuleId) {
        this.serverModuleId = serverModuleId;
    }

}
