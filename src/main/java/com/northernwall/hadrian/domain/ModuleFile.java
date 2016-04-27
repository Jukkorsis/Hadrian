package com.northernwall.hadrian.domain;

public class ModuleFile {
    private String serviceId;
    private String moduleId;
    private String network;
    private String name;
    private String contents;

    public ModuleFile(String serviceId, String moduleId, String network, String name, String contents) {
        this.serviceId = serviceId;
        this.moduleId = moduleId;
        this.network = network;
        this.name = name;
        this.contents = contents;
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

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

}
