package com.northernwall.hadrian.details.simple.dao;

import com.google.gson.annotations.SerializedName;
import java.util.LinkedList;
import java.util.List;

public class VipPoolDao {
    public String name;
    @SerializedName(value="dataCenter", alternate={"site"})
    public String dataCenter;
    @SerializedName(value="members", alternate={"member_list"})
    public List<VipMemberDao> members = new LinkedList<>();;

}
