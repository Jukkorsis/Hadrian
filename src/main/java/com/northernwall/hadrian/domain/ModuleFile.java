package com.northernwall.hadrian.domain;

public class ModuleFile {
    private String serviceId;
    private String moduleId;
    private String environment;
    private String name;
    private String contents;

    public ModuleFile(String serviceId, String moduleId, String environment, String name, String contents) {
        this.serviceId = serviceId;
        this.moduleId = moduleId;
        this.environment = environment;
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

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
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

    @Override
    public int hashCode() {
        int hash = 1;
        hash += 13 + serviceId.hashCode();
        hash += 17 + moduleId.hashCode();
        hash += 31 + environment.hashCode();
        hash += 37 + name.hashCode();

        return hash;
    }

}
