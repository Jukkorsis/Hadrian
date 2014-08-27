package com.northernwall.hadrian.formData;

import com.northernwall.hadrian.domain.Endpoint;
import com.northernwall.hadrian.domain.Link;
import java.util.List;

public class ServiceFormData {

    public String _id;
    public String name;
    public String team;
    public String description;
    public String access;
    public String type;
    public String state;
    public String busImportance;
    public String pii;
    public List<Endpoint> endpoints;
    public List<Link> links;
    public String api;
    public String impl;
    public String status;

}
