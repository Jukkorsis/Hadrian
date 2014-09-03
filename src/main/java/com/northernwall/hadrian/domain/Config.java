package com.northernwall.hadrian.domain;

import java.util.LinkedList;
import java.util.List;
import org.lightcouch.Document;

public class Config extends Document {
    public List<HaDimension> haDimensions = new LinkedList<>();
    public List<ConfigItem> dataCenters = new LinkedList<>();

}
