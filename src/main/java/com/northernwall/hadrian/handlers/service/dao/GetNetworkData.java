package com.northernwall.hadrian.handlers.service.dao;

import com.northernwall.hadrian.domain.Module;
import java.util.LinkedList;
import java.util.List;

public class GetNetworkData {

    public String network;
    public List<GetModuleNetworkData> modules = new LinkedList<>();

    public void addModule(Module module) {
        for (GetModuleNetworkData moduleNetworkData : modules) {
            if (moduleNetworkData.moduleId.equals(module.getModuleId())) {
                return;
            }
        }
        GetModuleNetworkData moduleNetworkData = new GetModuleNetworkData(
                module.getModuleId(),
                module.getModuleName(),
                network);
        modules.add(moduleNetworkData);
    }

    public void addHost(GetHostData hostData, GetModuleData moduleData) {
        for (GetModuleNetworkData moduleNetworkData : modules) {
            if (moduleNetworkData.moduleId.equals(moduleData.moduleId)) {
                moduleNetworkData.hosts.add(hostData);
                return;
            }
        }
    }

    void addVip(GetVipData vipData, GetModuleData moduleData) {
        for (GetModuleNetworkData moduleNetworkData : modules) {
            if (moduleNetworkData.moduleId.equals(moduleData.moduleId)) {
                moduleNetworkData.vips.add(vipData);
                return;
            }
        }
    }

}
