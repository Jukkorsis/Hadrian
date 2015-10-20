package com.northernwall.hadrian.webhook.dao;

import com.northernwall.hadrian.domain.Service;

public class ServiceData {
    public String serviceId;
    public String serviceAbbr;
    public String serviceName;
    public String teamId;
    public String mavenGroupId;
    public String mavenArtifactId;
    public String versionUrl;
    public String availabilityUrl;

    public static ServiceData create(Service service) {
        ServiceData temp = new ServiceData();
        temp.serviceId = service.getServiceId();
        temp.serviceAbbr = service.getServiceAbbr();
        temp.serviceName = service.getServiceName();
        temp.teamId = service.getTeamId();
        temp.mavenGroupId = service.getMavenGroupId();
        temp.mavenArtifactId = service.getMavenArtifactId();
        temp.versionUrl = service.getVersionUrl();
        temp.availabilityUrl = service.getAvailabilityUrl();
        return temp;
    }

}
