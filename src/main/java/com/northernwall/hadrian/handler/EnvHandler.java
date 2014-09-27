/*
 * Copyright 2014 Richard Thurston.
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

package com.northernwall.hadrian.handler;

import com.google.gson.Gson;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Env;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.formData.EnvFormData;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Thurston
 */
public class EnvHandler extends SoaAbstractHandler {
    private final static Logger logger = LoggerFactory.getLogger(EnvHandler.class);

    private final DataAccess dataAccess;
    private final CloseableHttpClient client;

    public EnvHandler(DataAccess dataAccess, Gson gson, CloseableHttpClient client) {
        super(gson);
        this.dataAccess = dataAccess;
        this.client = client;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.matches("/services/\\w+/envs.json")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                switch (request.getMethod()) {
                    case "POST":
                        createEnv(request);
                        break;
                }
                response.setStatus(200);
                request.setHandled(true);
            } else if (target.matches("/services/\\w+/envs/\\w+.json")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                switch (request.getMethod()) {
                    case "POST":
                        updateEnv(request);
                        break;
                }
                response.setStatus(200);
                request.setHandled(true);
            } else if (target.matches("/services/\\w+/envs/\\w+/hosts/.+.json")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                switch (request.getMethod()) {
                    case "GET":
                        String temp = target.substring(10, target.length() - 5);
                        int serviceEnd = temp.indexOf("/");
                        int envStart = temp.indexOf("/", serviceEnd+1)+1;
                        int envEnd = temp.indexOf("/", envStart+1);
                        int hostStart = temp.indexOf("/", envEnd+1)+1;
                        getImplVersion(
                                response, 
                                temp.substring(0, serviceEnd), 
                                temp.substring(envStart, envEnd), 
                                temp.substring(hostStart));
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

    private void getImplVersion(HttpServletResponse response, String serviceId, String envId, String hostName) throws IOException {
        response.setContentType("text/plain;charset=utf-8");
        logger.info("serviceId {} envId {} hostName {}", serviceId, envId, hostName);
        Service service = dataAccess.getService(serviceId);
        Env env = service.findEnv(envId);
        if (env == null) {
            return;
        }
        Host host = env.findHost(hostName);
        if (host == null) {
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new java.io.OutputStreamWriter(response.getOutputStream()))) {
            String version = urlGet(host.name, host.port, service.versionUrl);
            writer.write(version, 0, version.length());
        }
    }

    private String urlGet(String name, int port, String versionUrl) {
        CloseableHttpResponse response = null;
        try {
            HttpHost host = new HttpHost(name, port);
            HttpGet request = new HttpGet(versionUrl);
            response = client.execute(host, request);
            if (response.getStatusLine().getStatusCode() >= 300) {
                return "E" + response.getStatusLine().getStatusCode();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String content = reader.readLine();
            if (content == null || content.isEmpty()) {
                return "NULL";
            }
            if (content.length() > 12) {
                return content.substring(0, 12);
            }
            return content;
        } catch (IOException ex) {
            return "Unknown";
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException ex) {
                    logger.error("Could not close http version connection");
                }
            }
        }
    }

}
