package com.northernwall.hadrian.service.dao;

import com.northernwall.hadrian.domain.VipRef;

public class GetVipRefData {
    public String hostId;
    public String vipId;
    public String status;
    public String vipName;

    public static GetVipRefData create(VipRef ref) {
        GetVipRefData temp = new GetVipRefData();
        temp.hostId = ref.getHostId();
        temp.vipId = ref.getVipId();
        temp.status = ref.getStatus();
        return temp;
    }

}
