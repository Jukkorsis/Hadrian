package com.northernwall.hadrian.details.simple.dao;

import com.google.gson.annotations.SerializedName;

public class VipPortDao {
    public int port;
    @SerializedName(value="poolName", alternate={"service_group"})
    public String poolName;
}
