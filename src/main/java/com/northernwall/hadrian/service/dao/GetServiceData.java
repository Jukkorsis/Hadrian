package com.northernwall.hadrian.service.dao;

import com.northernwall.hadrian.domain.Service;
import java.util.LinkedList;
import java.util.List;

public class GetServiceData {
    public String serviceId;
    public String serviceAbbr;
    public String serviceName;
    public String teamId;
    public String description;
    public String mavenGroupId;
    public String mavenArtifactId;
    public String versionUrl;
    public String availabilityUrl;
    public List<GetCustomFunctionData> customFunctions;
    public List<GetHostData> hosts;
    public List<GetVipData> vips;
    public List<GetServiceRefData> uses;
    public List<GetServiceRefData> usedBy;
    public List<String> versions;

    public static GetServiceData create(Service service) {
        GetServiceData temp = new GetServiceData();
        temp.serviceId = service.getServiceId();
        temp.serviceAbbr = service.getServiceAbbr();
        temp.serviceName = service.getServiceName();
        temp.teamId = service.getTeamId();
        temp.description = service.getDescription();
        temp.mavenGroupId = service.getMavenGroupId();
        temp.mavenArtifactId = service.getMavenArtifactId();
        temp.versionUrl = service.getVersionUrl();
        temp.availabilityUrl = service.getAvailabilityUrl();
        temp.customFunctions = new LinkedList<>();
        temp.hosts = new LinkedList<>();
        temp.vips = new LinkedList<>();
        temp.uses = new LinkedList<>();
        temp.usedBy = new LinkedList<>();
        temp.versions = new LinkedList<>();
        return temp;
    }

}
