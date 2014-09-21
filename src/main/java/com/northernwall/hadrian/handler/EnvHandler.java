package com.northernwall.hadrian.handler;

import com.google.gson.Gson;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Env;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.formData.EnvFormData;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvHandler extends SoaAbstractHandler {
    private final static Logger logger = LoggerFactory.getLogger(EnvHandler.class);

    private final DataAccess dataAccess;

    public EnvHandler(DataAccess dataAccess, Gson gson) {
        super(gson);
        this.dataAccess = dataAccess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.matches("/services/\\w+/envs.json")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                switch (request.getMethod()) {
                    case "GET":
                        break;
                    case "POST":
                        createEnv(request);
                        break;
                }
                response.setStatus(200);
                request.setHandled(true);
            } else if (target.matches("/services/\\w+/envs/\\w+.json")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                switch (request.getMethod()) {
                    case "GET":
                        break;
                    case "POST":
                        updateEnv(request);
                        break;
                }
                response.setStatus(200);
                request.setHandled(true);
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void createEnv(Request request) throws IOException {
        EnvFormData envData = fromJson(request, EnvFormData.class);
        Service cur = dataAccess.getService(envData._id);

        if (cur == null) {
            return;
        }

        if (cur.findEnv(envData.name) != null) {
            return;
        }
        Env env = new Env();
        env.name = envData.name;
        env.vip = envData.vip;
        cur.addEnv(env);
        dataAccess.save(cur);
    }

    private void updateEnv(Request request) throws IOException  {
        EnvFormData envData = fromJson(request, EnvFormData.class);
        Service cur = dataAccess.getService(envData._id);

        if (cur == null) {
            return;
        }

        Env env = cur.findEnv(envData.name);
        if (env == null) {
            return;
        }
        
        env.vip = envData.vip;
        env.hosts = envData.hosts;
        env.hosts = new LinkedList<>();
        for (Host host : envData.hosts) {
            if (host.name != null && !host.name.isEmpty()) {
                env.hosts.add(host);
            }
        }
        Collections.sort(env.hosts, new Comparator<Host>(){
            @Override
            public int compare(Host o1, Host o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        dataAccess.save(cur);
    }

}
