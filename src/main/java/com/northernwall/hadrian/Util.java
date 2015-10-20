package com.northernwall.hadrian;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {
    private final static Logger logger = LoggerFactory.getLogger(Util.class);
    
    private static final Gson gson = new Gson();
    
    public static final <T> T fromJson(org.eclipse.jetty.server.Request request, Class<T> classOfT) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String s = reader.readLine();
        logger.debug("JSON input -> {}", s);
        return gson.fromJson(s, classOfT);
    }

    private Util() {
    }

}
