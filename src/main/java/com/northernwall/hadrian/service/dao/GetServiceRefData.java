package com.northernwall.hadrian.service.dao;

import com.northernwall.hadrian.domain.ServiceRef;

public class GetServiceRefData {
    public String clientServiceId;
    public String serverServiceId;
    public String serviceName;

    public static GetServiceRefData create(ServiceRef ref) {
        GetServiceRefData temp = new GetServiceRefData();
        temp.clientServiceId = ref.getClientServiceId();
        temp.serverServiceId = ref.getServerServiceId();
        return temp;
    }

}
