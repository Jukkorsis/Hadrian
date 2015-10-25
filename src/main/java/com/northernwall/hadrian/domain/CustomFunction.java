package com.northernwall.hadrian.domain;

import java.util.UUID;

public class CustomFunction implements Comparable<CustomFunction> {
    private String serviceId;
    private String customFunctionId;
    private String name;
    private String method;
    private String url;
    private String helpText;

    public CustomFunction() {
        this.customFunctionId = UUID.randomUUID().toString();
        this.serviceId = null;
        this.name = null;
        this.method = null;
        this.url = null;
        this.helpText = null;
    }

    public CustomFunction(String serviceId, String name, String method, String url, String helpText) {
        this.customFunctionId = UUID.randomUUID().toString();
        this.serviceId = serviceId;
        this.name = name;
        this.method = method;
        this.url = url;
        this.helpText = helpText;
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
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    @Override
    public int compareTo(CustomFunction o) {
        return name.compareTo(o.name);
    }
    
}
