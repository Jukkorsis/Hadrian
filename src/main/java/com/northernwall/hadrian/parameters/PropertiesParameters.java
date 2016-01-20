/*
 * Copyright 2015 Richard Thurston.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
