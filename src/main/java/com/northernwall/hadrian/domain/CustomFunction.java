package com.northernwall.hadrian.domain;

public class CustomFunction {
    private String name;
    private String helpText;

    public CustomFunction(String name, String helpText) {
        this.name = name;
        this.helpText = helpText;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }
    
    
}
