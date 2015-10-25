package com.northernwall.hadrian.domain;

import java.util.UUID;

public class CustomFunction implements Comparable<CustomFunction> {
    private String serviceId;
    private String customFunctionId;
    private String name;
    private String protocol;
    private String url;
    private String style;
    private String helpText;

    public CustomFunction() {
        this.customFunctionId = UUID.randomUUID().toString();
        this.serviceId = null;
        this.name = null;
        this.protocol = null;
        this.url = null;
        this.style = null;
        this.helpText = null;
    }

    public CustomFunction(String serviceId, String name, String protocol, String url, String style, String helpText) {
        this.customFunctionId = UUID.randomUUID().toString();
        this.serviceId = serviceId;
        this.name = name;
        this.protocol = protocol;
        this.url = url;
        this.style = style;
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

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
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
