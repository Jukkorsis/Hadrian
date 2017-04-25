package com.northernwall.hadrian.messaging.dao;

import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import java.util.Map;

public class PostMessageData {
    public String messageTypeName;
    public String serviceId;
    public String serviceName;
    @SerializedName(value="gitGroup", alternate={"gitlabNamespace"})
    public String gitGroup;
    @SerializedName(value="gitProject", alternate={"gitlabProject"})
    public String gitProject;
    public Map<String, String> data = new HashMap<>();

}
