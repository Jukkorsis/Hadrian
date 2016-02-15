package com.northernwall.hadrian.service.dao;

import java.util.LinkedList;
import java.util.List;

public class GetModuleNetworkData {
    public String network;
    public List<GetHostData> hosts;
    public List<GetVipData> vips;

    public GetModuleNetworkData(String network) {
        this.network = network;
        this.hosts = new LinkedList<>();
        this.vips = new LinkedList<>();
    }

}
