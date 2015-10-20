package com.northernwall.hadrian.service.dao;

import com.northernwall.hadrian.domain.Host;
import java.util.LinkedList;
import java.util.List;

public class GetHostData {
    public String hostId;
    public String hostName;
    public String serviceId;
    public String status;
    public String dataCenter;
    public String network;
    public String env;
    public String size;
    public List<GetVipRefData> vipRefs;
    public String version;
    public int availability;

    public static GetHostData create(Host host) {
        GetHostData temp = new GetHostData();
        temp.hostId = host.getHostId();
        temp.hostName = host.getHostName();
        temp.serviceId = host.getServiceId();
        temp.status = host.getStatus();
        temp.dataCenter = host.getDataCenter();
        temp.network = host.getNetwork();
        temp.env = host.getEnv();
        temp.size = host.getSize();
        temp.vipRefs = new LinkedList<>();
        temp.version = "-";
        temp.availability = 0;
        return temp;
    }

}
