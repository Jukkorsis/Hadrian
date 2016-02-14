package com.northernwall.hadrian.service.dao;

import com.northernwall.hadrian.domain.Config;
import com.northernwall.hadrian.domain.Module;
import java.util.LinkedList;
import java.util.List;

public class GetModuleData {

    public String moduleId;
    public String moduleName;
    public String moduleType;
    public String gitPath;
    public String gitFolder;
    public String mavenGroupId;
    public String mavenArtifactId;
    public String artifactType;
    public String artifactSuffix;
    public String hostAbbr;
    public String versionUrl;
    public String availabilityUrl;
    public String runAs;
    public String startCmdLine;
    public String stopCmdLine;
    public int cmdLineTimeOut;
    public List<String> versions;
    public List<GetModuleNetworkData> networks;
    
    public static GetModuleData create(Module module, Config config) {
        GetModuleData temp = new GetModuleData();
        temp.moduleId = module.getModuleId();
        temp.moduleName = module.getModuleName();
        temp.moduleType = module.getModuleType();
        temp.gitPath = module.getGitPath();
        temp.gitFolder = module.getGitFolder();
        temp.mavenGroupId = module.getMavenGroupId();
        temp.mavenArtifactId = module.getMavenArtifactId();
        temp.artifactType = module.getArtifactType();
        temp.artifactSuffix = module.getArtifactSuffix();
        temp.hostAbbr = module.getHostAbbr();
        temp.versionUrl = module.getVersionUrl();
        temp.availabilityUrl = module.getAvailabilityUrl();
        temp.runAs = module.getRunAs();
        temp.startCmdLine = module.getStartCmdLine();
        temp.stopCmdLine = module.getStopCmdLine();
        temp.cmdLineTimeOut = module.getCmdLineTimeOut();
        temp.versions = new LinkedList<>();
        temp.networks = new LinkedList<>();
        for (String network : config.networks) {
            temp.networks.add(new GetModuleNetworkData(network));
        }
        return temp;
    }

    public void addHost(GetHostData hostData) {
        for (GetModuleNetworkData getModuleNetworkData : networks) {
            if (getModuleNetworkData.network.equals(hostData.network)) {
                getModuleNetworkData.hosts.add(hostData);
                return;
            }
        }
    }

}
