package com.northernwall.hadrian.service.dao;

import com.northernwall.hadrian.domain.Service;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GetServiceData {

    public String serviceId;
    public String serviceAbbr;
    public String serviceName;
    public String teamId;
    public String description;
    public String businessImpact;
    public String piiUsage;
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
    public int cmdLineTimeOut;
    public Date creationDate;
    public List<GetHostData> hosts;
    public List<GetVipData> vips;
    public List<GetDataStoreData> dataStores;
    public List<GetCustomFunctionData> customFunctions;
    public List<GetServiceRefData> uses;
    public List<GetServiceRefData> usedBy;
    public List<String> versions;
    public List<GetPairData> links;
    public boolean canModify;

    public static GetServiceData create(Service service) {
        GetServiceData temp = new GetServiceData();
        temp.serviceId = service.getServiceId();
        temp.serviceAbbr = service.getServiceAbbr();
        temp.serviceName = service.getServiceName();
        temp.teamId = service.getTeamId();
        temp.description = service.getDescription();
        temp.businessImpact = service.getBusinessImpact();
        temp.piiUsage = service.getPiiUsage();
        temp.runAs = service.getRunAs();
        temp.gitPath = service.getGitPath();
        temp.mavenGroupId = service.getMavenGroupId();
        temp.mavenArtifactId = service.getMavenArtifactId();
        temp.artifactType = service.getArtifactType();
        temp.artifactSuffix = service.getArtifactSuffix();
        temp.versionUrl = service.getVersionUrl();
        temp.availabilityUrl = service.getAvailabilityUrl();
        temp.startCmdLine = service.getStartCmdLine();
        temp.stopCmdLine = service.getStopCmdLine();
        temp.cmdLineTimeOut = service.getCmdLineTimeOut();
        temp.creationDate = service.getCreationDate();
        temp.hosts = new LinkedList<>();
        temp.vips = new LinkedList<>();
        temp.dataStores = new LinkedList<>();
        temp.customFunctions = new LinkedList<>();
        temp.uses = new LinkedList<>();
        temp.usedBy = new LinkedList<>();
        temp.versions = new LinkedList<>();
        temp.links = new LinkedList<>();
        if (service.getLinks() != null && !service.getLinks().isEmpty()) {
            for (Map.Entry<String, String> entry : service.getLinks().entrySet()) {
                temp.links.add(new GetPairData(entry.getKey(), entry.getValue()));
            }
        }
        return temp;
    }

}
