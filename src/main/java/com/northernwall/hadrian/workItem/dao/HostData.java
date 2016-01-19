package com.northernwall.hadrian.workItem.dao;

import com.northernwall.hadrian.domain.Host;

public class HostData {
    public String hostId;
    public String hostName;
    public String dataCenter;
    public String network;
    public String env;
    public String size;
    public String version;

    public static HostData create(Host host) {
        if (host == null) {
            return null;
        }
        HostData temp = new HostData();
        temp.hostId = host.getHostId();
        temp.hostName = host.getHostName();
        temp.dataCenter = host.getDataCenter();
        temp.network = host.getNetwork();
        temp.env = host.getEnv();
        temp.size = host.getSize();
        temp.version = null;
        return temp;
    }

}
