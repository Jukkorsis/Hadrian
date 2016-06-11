package com.northernwall.hadrian.details.simple;

import com.google.gson.annotations.SerializedName;

public class VipPortInfo {
    public int port;
    @SerializedName(value="poolName", alternate={"service_group"})
    public String poolName;
}
