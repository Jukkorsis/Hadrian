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
public class WorkItem {

    private String type;
    private String id;
    private String nextId;
    private String env;
    private String size;
    private String version;
    private String username;
    private boolean external;
    private int servicePort;

    public static WorkItem createUpdateHost(String id, String env, String size, String version, String username) {
        WorkItem workItem = new WorkItem();
        workItem.type = "host";
        workItem.id = id;
        workItem.nextId = null;
        workItem.env = env;
        workItem.size = size;
        workItem.version = version;
        workItem.username = username;
        return workItem;
    }

    public static WorkItem createUpdateVip(String id, boolean external, int servicePort) {
        WorkItem workItem = new WorkItem();
        workItem.type = "vip";
        workItem.id = id;
        workItem.nextId = null;
        workItem.external = external;
        workItem.servicePort = servicePort;
        return workItem;
    }

    private WorkItem() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNextId() {
        return nextId;
    }

    public void setNextId(String nextId) {
        this.nextId = nextId;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean getExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    public int getServicePort() {
        return servicePort;
    }

    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }

}
