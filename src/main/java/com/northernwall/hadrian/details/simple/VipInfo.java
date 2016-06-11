package com.northernwall.hadrian.details.simple;

import com.google.gson.annotations.SerializedName;
import java.util.LinkedList;
import java.util.List;

public class VipInfo {
    public String address;
    public String name;
    @SerializedName(value="dataCenter", alternate={"site"})
    public String dataCenter;
    @SerializedName(value="ports", alternate={"vport_list"})
    public List<VipPortInfo> ports = new LinkedList<>();;
}
