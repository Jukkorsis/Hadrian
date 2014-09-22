package com.northernwall.hadrian.domain;

import java.util.LinkedList;
import java.util.List;
import org.lightcouch.Document;

public class Config extends Document {
    public List<ConfigItem> dataCenters = new LinkedList<>();
    public List<ConfigItem> teams = new LinkedList<>();
    public List<ConfigItem> products = new LinkedList<>();
    public List<ConfigItem> haDimensions = new LinkedList<>();
    public List<ConfigItem> classDimensions = new LinkedList<>();
    public String manageHostUrl = "/manage";

}
