package com.northernwall.hadrian.domain;

import com.northernwall.hadrian.Const;
import java.util.UUID;

public class CustomFunction implements Comparable<CustomFunction> {

    private String customFunctionId;
    private String serviceId;
    private String moduleId;
    private String name;
    private String method;
    private String url;
    private boolean teamOnly;

    public CustomFunction() {
        this.customFunctionId = UUID.randomUUID().toString();
        this.serviceId = null;
        this.moduleId = null;
        this.name = null;
        this.method = null;
        this.url = null;
        this.teamOnly = true;
    }

    public CustomFunction(String serviceId, String moduleId, String name, String method, String url, boolean teamOnly) {
        this.customFunctionId = UUID.randomUUID().toString();
        this.serviceId = serviceId;
        this.moduleId = moduleId;
        this.name = name;
        this.method = method;
        this.url = url;
        this.teamOnly = teamOnly;
    }

    public String getCustomFunctionId() {
        return customFunctionId;
    }

    public void setCustomFunctionId(String customFunctionId) {
        this.customFunctionId = customFunctionId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        if (url == null || url.isEmpty()) {
            return url;
        }
        String temp = url.toLowerCase();
        if (temp.startsWith(Const.HTTP) || temp.startsWith(Const.HTTPS)) {
            return url;
        }
        return Const.HTTP + url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isTeamOnly() {
        return teamOnly;
    }

    public void setTeamOnly(boolean teamOnly) {
        this.teamOnly = teamOnly;
    }

    @Override
    public int compareTo(CustomFunction o) {
        return name.compareTo(o.name);
    }

}
