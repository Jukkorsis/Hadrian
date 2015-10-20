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

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Thurston
 */
public class DataStore implements Comparable<DataStore>{
    private final static Logger logger = LoggerFactory.getLogger(DataStore.class);

    private String dataStoreId;
    private String dataStoreName;
    private String teamId;
    private String type;

    public DataStore(String dataStoreName, String teamId, String type) {
        this.dataStoreId = UUID.randomUUID().toString();
        this.dataStoreName = dataStoreName;
        this.teamId = teamId;
        this.type = type;
    }

    public String getDataStoreId() {
        return dataStoreId;
    }

    public void setDataStoreId(String dataStoreId) {
        this.dataStoreId = dataStoreId;
    }

    public String getDataStoreName() {
        return dataStoreName;
    }

    public void setDataStoreName(String dataStoreName) {
        this.dataStoreName = dataStoreName;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int compareTo(DataStore o) {
        return dataStoreName.compareTo(o.dataStoreName);
    }
    
}
