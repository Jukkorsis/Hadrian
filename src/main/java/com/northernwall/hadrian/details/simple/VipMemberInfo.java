package com.northernwall.hadrian.details.simple;

import com.google.gson.annotations.SerializedName;

public class VipMemberInfo {
    public int port;
    public int priority;
    @SerializedName(value="hostName", alternate={"server"})
    public String hostName;
    public int status;
    @SerializedName(value="connections", alternate={"cur_conns"})
    public int connections;

}
