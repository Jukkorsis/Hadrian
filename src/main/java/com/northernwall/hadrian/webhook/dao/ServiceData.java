package com.northernwall.hadrian.webhook.dao;

import com.northernwall.hadrian.domain.Service;

public class ServiceData {
    public String serviceId;
    public String serviceAbbr;
    public String serviceName;
    public String teamId;
    public String runAs;
    public String gitPath;
    public String mavenGroupId;
    public String mavenArtifactId;
    public String artifactType;
    public String artifactSuffix;
    public String versionUrl;
    public String availabilityUrl;
    public String startCmdLine;
    public String stopCmdLine;

    public static ServiceData create(Service service) {
        if (service == null) {
            return null;
        }
        ServiceData temp = new ServiceData();
        temp.serviceId = service.getServiceId();
        temp.serviceAbbr = service.getServiceAbbr();
        temp.serviceName = service.getServiceName();
        temp.teamId = service.getTeamId();
        temp.runAs = service.getRunAs();
        temp.gitPath = service.getGitPath();
        temp.mavenGroupId = service.getMavenGroupId();
        temp.mavenArtifactId = service.getMavenArtifactId();
        temp.versionUrl = service.getVersionUrl();
        temp.availabilityUrl = service.getAvailabilityUrl();
        temp.startCmdLine = service.getStartCmdLine();
        temp.stopCmdLine = service.getStopCmdLine();
        return temp;
    }

}
