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

import java.util.Date;
import java.util.UUID;

/**
 *
 * @author Richard Thurston
 */
public class Service implements Comparable<Service>{
    private String serviceId;
    private String serviceAbbr;
    private String serviceName;
    private String teamId;
    private String description;
    private String serviceType;
    private GitMode gitMode;
    private String gitProject;
    private Date creationDate;

    public Service(String serviceAbbr, String serviceName, String teamId, String description, String serviceType, GitMode gitMode, String gitProject) {
        this.serviceId = UUID.randomUUID().toString();
        this.serviceAbbr = serviceAbbr;
        this.serviceName = serviceName;
        this.teamId = teamId;
        this.description = description;
        this.serviceType = serviceType;
        this.gitMode = gitMode;
        this.gitProject = gitProject;
        this.creationDate = new Date();
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceAbbr() {
        return serviceAbbr;
    }

    public void setServiceAbbr(String serviceAbbr) {
        this.serviceAbbr = serviceAbbr;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public GitMode getGitMode() {
        return gitMode;
    }

    public void setGitMode(GitMode gitMode) {
        this.gitMode = gitMode;
    }

    public String getGitProject() {
        return gitProject;
    }

    public void setGitProject(String gitProject) {
        this.gitProject = gitProject;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public int compareTo(Service o) {
        return serviceName.compareToIgnoreCase(o.serviceName);
    }

}
