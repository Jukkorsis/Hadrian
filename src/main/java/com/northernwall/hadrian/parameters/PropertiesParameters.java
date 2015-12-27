package com.northernwall.hadrian.parameters;

import java.util.Properties;

public class PropertiesParameters implements Parameters {
    private final Properties properties;

    public PropertiesParameters(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String getString(String key, String value) {
        return properties.getProperty(key, value);
    }

    @Override
    public int getInt(String key, String value) {
        return Integer.parseInt(properties.getProperty(key, value));
    }

    @Override
    public boolean getBoolean(String key, String value) {
        return Boolean.parseBoolean(properties.getProperty(key, value));
    }

}
