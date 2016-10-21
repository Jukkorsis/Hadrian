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

import com.northernwall.hadrian.GMT;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Richard Thurston
 */
public class Service implements Comparable<Service>{
    public static List<Service> filterTeam(String teamId, List<Service> services) {
        List<Service> temp = new LinkedList<>();
        if (services != null && !services.isEmpty()) {
            for (Service service : services) {
                if (service.getTeamId().equals(teamId)) {
                    temp.add(service);
                }
            }
        }
        return temp;
    }
    
    private String serviceId;
    private String serviceName;
    private String teamId;
    private String description;
    private String serviceType;
    private String gitProject;
    private String scope;
    private String mavenGroupId;
    private String testStyle;
    private String testHostname;
    private String testRunAs;
    private String testDeploymentFolder;
    private String testCmdLine;
    private int testTimeOut;
    private List<Document> documents;
    private Date creationDate;
    private Date deletionDate;
    private boolean active = true;

    public Service(String serviceName, String teamId, String description, String serviceType, String gitProject, String scope, String mavenGroupId, String testStyle, String testHostname, String testRunAs, String testDeploymentFolder, String testCmdLine, int testTimeOut, boolean active) {
        this.serviceId = UUID.randomUUID().toString();
        this.serviceName = serviceName;
        this.teamId = teamId;
        this.description = description;
        this.serviceType = serviceType;
        this.gitProject = gitProject;
        this.scope = scope;
        this.mavenGroupId = mavenGroupId;
        this.testStyle = testStyle;
        this.testHostname = testHostname;
        this.testRunAs = testRunAs;
        this.testDeploymentFolder = testDeploymentFolder;
        this.testCmdLine = testCmdLine;
        this.testTimeOut = testTimeOut;
        this.documents = new LinkedList<>();
        this.creationDate = GMT.getGmtAsDate();
        this.deletionDate = null;
        this.active = active;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
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

    public void setTeamId(String teamId) {
        this.teamId = teamId;
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

    public String getGitProject() {
        return gitProject;
    }

    public void setGitProject(String gitProject) {
        this.gitProject = gitProject;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getMavenGroupId() {
        return mavenGroupId;
    }

    public void setMavenGroupId(String mavenGroupId) {
        this.mavenGroupId = mavenGroupId;
    }

    public String getTestStyle() {
        return testStyle;
    }

    public void setTestStyle(String testStyle) {
        this.testStyle = testStyle;
    }

    public String getTestHostname() {
        return testHostname;
    }

    public void setTestHostname(String testHostname) {
        this.testHostname = testHostname;
    }

    public String getTestRunAs() {
        return testRunAs;
    }

    public void setTestRunAs(String testRunAs) {
        this.testRunAs = testRunAs;
    }

    public String getTestDeploymentFolder() {
        return testDeploymentFolder;
    }

    public void setTestDeploymentFolder(String testDeploymentFolder) {
        this.testDeploymentFolder = testDeploymentFolder;
    }

    public String getTestCmdLine() {
        return testCmdLine;
    }

    public void setTestCmdLine(String testCmdLine) {
        this.testCmdLine = testCmdLine;
    }

    public int getTestTimeOut() {
        if (testTimeOut == 0) {
            return 300;
        }
        return testTimeOut;
    }

    public void setTestTimeOut(int testTimeOut) {
        this.testTimeOut = testTimeOut;
    }

    public List<Document> getDocuments() {
        if (documents == null) {
            documents = new LinkedList<>();
        }
        return documents;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(Date deletionDate) {
        this.deletionDate = deletionDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public int compareTo(Service o) {
        return serviceName.compareToIgnoreCase(o.serviceName);
    }
    
}
