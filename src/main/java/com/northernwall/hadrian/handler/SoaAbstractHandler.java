package com.northernwall.hadrian.handler;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SoaAbstractHandler extends AbstractHandler {
    private final static Logger logger = LoggerFactory.getLogger(SoaAbstractHandler.class);

    protected final Gson gson;

    public SoaAbstractHandler(Gson gson) {
        this.gson = gson;
    }

    protected final <T> T fromJson(Request request, Class<T> classOfT) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String s = reader.readLine();
        logger.debug("JSON input -> {}", s);
        return gson.fromJson(s, classOfT);
    }

}
