package com.northernwall.hadrian.details.simple.dao;

import com.google.gson.annotations.SerializedName;
import java.util.LinkedList;
import java.util.List;

public class VipDao {
    public String address;
    public String name;
    @SerializedName(value="dataCenter", alternate={"site"})
    public String dataCenter;
    @SerializedName(value="ports", alternate={"vport_list"})
    public List<VipPortDao> ports = new LinkedList<>();;
}
