package com.northernwall.hadrian.domain;

import java.util.List;

public class Service extends ServiceHeader {
    public String state = "Statefull";
    public List<Endpoint> endpoints;
    public List<Link> links;
    public List<String> images;
    public List<Version> versions;

}
