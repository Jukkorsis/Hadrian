package com.northernwall.hadrian.parameters;

import java.util.List;

public interface ParameterChangeListener {
    void onChange(List<String> keys);

}
