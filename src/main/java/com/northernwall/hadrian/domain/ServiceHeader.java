package com.northernwall.hadrian.domain;

import org.lightcouch.Document;

public class ServiceHeader extends Document {
    public long date;
    public String name;
    public String team;
    public String description;
    public String access = "Internal";
    public String type = "service";
    public String imageLogo;

}
