package com.northernwall.hadrian.messaging.dao;

import java.util.HashMap;
import java.util.Map;

public class PostMessageData {
    public String messageTypeName;
    public String serviceId;
    public String serviceName;
    public String serviceAbbr;
    public String moduleName;
    public String gitlabNamespace;
    public String gitlabProject;
    public Map<String, String> data = new HashMap<>();

}
