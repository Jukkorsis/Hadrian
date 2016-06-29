package com.northernwall.hadrian.stubs;

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.parameters.ParameterChangeListener;
import com.northernwall.hadrian.parameters.Parameters;

public class StubParameters implements Parameters {

    @Override
    public String getString(String key, String value) {
        if (key.equalsIgnoreCase(Const.MESSAGE_PROCESSORS)) {
            return StubMessageProcessor.class.getCanonicalName();
        } else if (key.equalsIgnoreCase("messageType.TEST")) {
            return "{\"name\":\"TEST\", \"emailBody\":\"Hi {A}.\", \"slackBody\":\"This is {A} test, by Richard.\", \"slackIcon\":\":ghost:\"}";
        }
        return value;
    }

    @Override
    public int getInt(String key, int value) {
        return value;
    }

    @Override
    public boolean getBoolean(String key, boolean value) {
        return value;
    }

    @Override
    public void registerChangeListener(ParameterChangeListener listener) {
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

}
