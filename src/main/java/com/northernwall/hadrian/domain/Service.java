package com.northernwall.hadrian.domain;

import java.util.LinkedList;
import java.util.List;

public class Service extends ServiceHeader {
    public String state = "Statefull";
    public List<Endpoint> endpoints;
    public List<Link> links;
    public List<String> images;
    public List<Version> versions;
    
    public Version findVersion(String api) {
        if (versions == null || versions.isEmpty()) {
            return null;
        }
        for (Version version : versions) {
            if (version.api.equals(api)) {
                return version;
            }
        }
        return null;
    }
    
    public void addVersion(Version version) {
        if (versions == null) {
            versions = new LinkedList<>();
        }
        versions.add(version);
    }

}
