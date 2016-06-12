package com.northernwall.hadrian.details.simple.dao;

import com.google.gson.annotations.SerializedName;

public class VipMemberDao {
    public int port;
    public int priority;
    @SerializedName(value="hostName", alternate={"server"})
    public String hostName;
    public int status;
    @SerializedName(value="connections", alternate={"cur_conns"})
    public int connections;

}
