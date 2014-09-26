package com.northernwall.hadrian.formData;

import com.northernwall.hadrian.domain.Action;
import com.northernwall.hadrian.domain.Link;
import com.northernwall.hadrian.domain.ListItem;
import java.util.LinkedList;
import java.util.List;

public class ServiceFormData {

    public String _id;
    public String name;
    public String team;
    public String product;
    public String description;
    public String access;
    public String type;
    public String state;
    public String tech;
    public List<Link> links = new LinkedList<>();
    public List<ListItem> haRatings = new LinkedList<>();
    public List<ListItem> classRatings = new LinkedList<>();
    public String mavenUrl;
    public String versionUrl;
    public List<Action> actions = new LinkedList<>();
    public String api;
    public String status;

}
