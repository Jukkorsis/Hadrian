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
    public int getInt(String key, int value) {
        String temp = properties.getProperty(key);
        if (temp == null) {
            return value;
        }
        return Integer.parseInt(temp);
    }

    @Override
    public boolean getBoolean(String key, boolean value) {
        String temp = properties.getProperty(key);
        if (temp == null) {
            return value;
        }
        return Boolean.parseBoolean(temp);
    }

}
