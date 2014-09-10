package com.northernwall.hadrian.domain;

import java.util.LinkedList;
import java.util.List;

public class Service extends ServiceHeader {
    public static final String DEFAULT_IMAGE = "/ui/img/serviceLogo.png";

    public String tech;
    public String busValue = "Medium";
    public String pii = "None";
    public List<Endpoint> endpoints = new LinkedList<>();
    public List<Link> links = new LinkedList<>();
    public List<String> images;
    public List<ListItem> haRatings = new LinkedList<>();
    public List<Version> versions = new LinkedList<>();
    public List<DataCenter> dataCenters = new LinkedList<>();
    public List<Warning> warnings = new LinkedList<>();

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

    public boolean isImageLogoBlank() {
        return (imageLogo == null || imageLogo.isEmpty() || imageLogo.equals(DEFAULT_IMAGE));

    }

}
