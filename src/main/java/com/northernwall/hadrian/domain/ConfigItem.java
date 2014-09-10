package com.northernwall.hadrian.domain;

import java.util.LinkedList;
import java.util.List;

public class ConfigItem {
    public String code;
    public String description;
    public String url;
    public String email;
    public List<ConfigItem> subItems = new LinkedList<>();

}
