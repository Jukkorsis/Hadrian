package com.northernwall.hadrian.domain;

import org.lightcouch.Document;

public class ServiceHeader extends Document {
    public String name;
    public long date;
    public String team;
    public String description;
    public String access = "Internal";
    public String type = "service";
    public String imageLogo;

}
