package com.northernwall.hadrian.service.dao;

import com.northernwall.hadrian.domain.GitMode;
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
    public String serviceType;
    public GitMode gitMode;
    public String gitProject;
    public Date creationDate;
    public List<GetModuleData> modules;
    public List<GetDataStoreData> dataStores;
    public List<GetCustomFunctionData> customFunctions;
    public List<GetServiceRefData> uses;
    public List<GetServiceRefData> usedBy;
    public List<GetPairData> links;
    public boolean canModify;

    public static GetServiceData create(Service service) {
        GetServiceData temp = new GetServiceData();
        temp.serviceId = service.getServiceId();
        temp.serviceAbbr = service.getServiceAbbr();
        temp.serviceName = service.getServiceName();
        temp.teamId = service.getTeamId();
        temp.description = service.getDescription();
        temp.serviceType = service.getServiceType();
        temp.gitMode = service.getGitMode();
        temp.gitProject = service.getGitProject();
        temp.creationDate = service.getCreationDate();
        temp.modules = new LinkedList<>();
        temp.dataStores = new LinkedList<>();
        temp.customFunctions = new LinkedList<>();
        temp.uses = new LinkedList<>();
        temp.usedBy = new LinkedList<>();
        temp.links = new LinkedList<>();
        if (service.getLinks() != null && !service.getLinks().isEmpty()) {
            for (Map.Entry<String, String> entry : service.getLinks().entrySet()) {
                temp.links.add(new GetPairData(entry.getKey(), entry.getValue()));
            }
        }
        return temp;
    }

}
