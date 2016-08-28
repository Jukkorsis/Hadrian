/*
 * Copyright 2014 Richard Thurston.
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
package com.northernwall.hadrian.handlers.service.dao;

import com.northernwall.hadrian.domain.Document;
import com.northernwall.hadrian.domain.GitMode;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class GetServiceData {

    public String serviceId;
    public String serviceName;
    public String teamId;
    public String description;
    public String serviceType;
    public GitMode gitMode;
    public String gitProject;
    public List<Document> leftDocuments;
    public List<Document> middleDocuments;
    public List<Document> rightDocuments;
    public Date creationDate;
    public Date deletionDate;
    public boolean active;
    public List<GetModuleData> modules;
    public List<GetNetworkData> networks;
    public List<GetDataStoreData> dataStores;
    public boolean canModify;

    public static GetServiceData create(Service service) {
        GetServiceData temp = new GetServiceData();
        temp.serviceId = service.getServiceId();
        temp.serviceName = service.getServiceName();
        temp.teamId = service.getTeamId();
        temp.description = service.getDescription();
        temp.serviceType = service.getServiceType();
        temp.gitMode = service.getGitMode();
        temp.gitProject = service.getGitProject();

        temp.leftDocuments = new LinkedList<>();
        temp.middleDocuments = new LinkedList<>();
        temp.rightDocuments = new LinkedList<>();
        Collections.sort(service.getDocuments());
        for (Document doc : service.getDocuments()) {
            if (temp.middleDocuments.size() == temp.rightDocuments.size()) {
                if (temp.leftDocuments.size() == temp.middleDocuments.size()) {
                    temp.leftDocuments.add(doc);
                } else {
                    temp.middleDocuments.add(doc);
                }
            } else {
                temp.rightDocuments.add(doc);
            }
        }

        temp.creationDate = service.getCreationDate();
        temp.deletionDate = service.getDeletionDate();
        temp.active = service.isActive();
        temp.modules = new LinkedList<>();
        temp.networks = new LinkedList<>();
        temp.dataStores = new LinkedList<>();
        return temp;
    }

    public void addNetwork(String network) {
        for (GetNetworkData networkData : networks) {
            if (networkData.network.equals(network)) {
                return;
            }
        }
        GetNetworkData networkData = new GetNetworkData();
        networkData.network = network;
        networks.add(networkData);
    }

    public void addModuleNetwork(Module module, String network) {
        for (GetNetworkData networkData : networks) {
            if (networkData.network.equals(network)) {
                networkData.addModule(module);
                return;
            }
        }
    }

    public void addHost(GetHostData hostData, GetModuleData moduleData) {
        for (GetNetworkData networkData : networks) {
            if (networkData.network.equals(hostData.network)) {
                networkData.addHost(hostData, moduleData);
                return;
            }
        }
    }

    public void addVip(GetVipData vipData, GetModuleData moduleData) {
        for (GetNetworkData networkData : networks) {
            if (networkData.network.equals(vipData.network)) {
                networkData.addVip(vipData, moduleData);
                return;
            }
        }
    }

}
