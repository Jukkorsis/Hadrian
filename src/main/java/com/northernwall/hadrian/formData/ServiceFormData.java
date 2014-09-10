package com.northernwall.hadrian.formData;

import com.northernwall.hadrian.domain.DataCenter;
import com.northernwall.hadrian.domain.Endpoint;
import com.northernwall.hadrian.domain.ListItem;
import com.northernwall.hadrian.domain.Link;
import java.util.LinkedList;
import java.util.List;

public class ServiceFormData {

    public String _id;
    public String name;
    public String team;
    public String description;
    public String access;
    public String type;
    public String state;
    public String tech;
    public String busValue;
    public String pii;
    public List<Endpoint> endpoints;
    public List<Link> links;
    public List<DataCenter> dataCenters;
    public List<ListItem> haRatings = new LinkedList<>();
    public String api;
    public String impl;
    public String status;

}
