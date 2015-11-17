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
package com.northernwall.hadrian.webhook;

import com.google.gson.Gson;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.Util;
import com.northernwall.hadrian.webhook.dao.CallbackResponse;
import com.northernwall.hadrian.webhook.dao.CreateVipContainer;
import com.northernwall.hadrian.webhook.dao.UpdateHostContainer;
import com.northernwall.hadrian.webhook.dao.CreateHostVipContainer;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Thurston
 */
public class WebHookHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(WebHookHandler.class);
    private final static int PAUSE = 15;

    private final OkHttpClient client;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Gson gson;

    public WebHookHandler(OkHttpClient client) {
        this.client = client;
        scheduledExecutorService = Executors.newScheduledThreadPool(5);
        gson = new Gson();
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.startsWith("/webhook/") && request.getMethod().equals(Const.HTTP_POST)) {
                switch (target) {
                    case "/webhook/service":
                        logger.info("Handling {} request {}", request.getMethod(), target);
                        break;
                    case "/webhook/host":
                        logger.info("Handling {} request {}", request.getMethod(), target);
                        processHost(request);
                        break;
                    case "/webhook/vip":
                        logger.info("Handling {} request {}", request.getMethod(), target);
                        processVip(request);
                        break;
                    case "/webhook/hostvip":
                        logger.info("Handling {} request {}", request.getMethod(), target);
                        processHostVip(request);
                        break;
                    default:
                        throw new RuntimeException("Unknown webhook " + target);
                }
                response.setStatus(200);
                request.setHandled(true);

            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void processHost(Request request) throws IOException {
        UpdateHostContainer data = Util.fromJson(request, UpdateHostContainer.class);

        switch (data.operation) {
            case "create":
                createHost(data);
                break;
            case "update":
                updateHost(data);
                break;
            case "delete":
                deleteHost(data);
                break;
            default:
                throw new RuntimeException("Unknown webhook host opertion " + data.operation);
        }
    }

    private void createHost(UpdateHostContainer data) {
        CallbackResponse response = new CallbackResponse();
        response.type = "host";
        response.operation = "create";
        response.hostId = data.host.hostId;
        response.status = 200;

        scheduledExecutorService.schedule(
                new WebHookRunnable(data.callbackUrl, response),
                PAUSE,
                TimeUnit.SECONDS);
    }

    private void updateHost(UpdateHostContainer data) {
        CallbackResponse response = new CallbackResponse();
        response.type = "host";
        response.operation = "update";
        response.hostId = data.host.hostId;
        response.status = 200;

        scheduledExecutorService.schedule(
                new WebHookRunnable(data.callbackUrl, response),
                PAUSE,
                TimeUnit.SECONDS);
    }

    private void deleteHost(UpdateHostContainer data) {
        CallbackResponse response = new CallbackResponse();
        response.type = "host";
        response.operation = "delete";
        response.hostId = data.host.hostId;
        response.status = 200;

        scheduledExecutorService.schedule(
                new WebHookRunnable(data.callbackUrl, response),
                PAUSE,
                TimeUnit.SECONDS);
    }

    private void processVip(Request request) throws IOException {
        CreateVipContainer data = Util.fromJson(request, CreateVipContainer.class);

        switch (data.operation) {
            case "create":
                createtVip(data);
                break;
            case "update":
                updateVip(data);
                break;
            case "delete":
                deleteVip(data);
                break;
            default:
                throw new RuntimeException("Unknown webhook vip opertion " + data.operation);
        }
    }

    private void createtVip(CreateVipContainer data) {
        CallbackResponse response = new CallbackResponse();
        response.type = "vip";
        response.operation = "create";
        response.vipId = data.vip.vipId;
        response.status = 200;

        scheduledExecutorService.schedule(
                new WebHookRunnable(data.callbackUrl, response),
                PAUSE,
                TimeUnit.SECONDS);
    }

    private void updateVip(CreateVipContainer data) {
        CallbackResponse response = new CallbackResponse();
        response.type = "vip";
        response.operation = "update";
        response.vipId = data.vip.vipId;
        response.status = 200;

        scheduledExecutorService.schedule(
                new WebHookRunnable(data.callbackUrl, response),
                PAUSE,
                TimeUnit.SECONDS);
    }

    private void deleteVip(CreateVipContainer data) {
        CallbackResponse response = new CallbackResponse();
        response.type = "vip";
        response.operation = "delete";
        response.vipId = data.vip.vipId;
        response.status = 200;

        scheduledExecutorService.schedule(
                new WebHookRunnable(data.callbackUrl, response),
                PAUSE,
                TimeUnit.SECONDS);
    }

    private void processHostVip(Request request) throws IOException {
        CreateHostVipContainer data = Util.fromJson(request, CreateHostVipContainer.class);

        switch (data.operation) {
            case "add":
                addHostVip(data);
                break;
            case "delete":
                deleteHostVip(data);
                break;
            default:
                throw new RuntimeException("Unknown webhook host vip opertion " + data.operation);
        }
    }

    private void addHostVip(CreateHostVipContainer data) {
        CallbackResponse response = new CallbackResponse();
        response.type = "hostvip";
        response.operation = "add";
        response.hostId = data.host.hostId;
        response.vipId = data.vip.vipId;
        response.status = 200;

        scheduledExecutorService.schedule(
                new WebHookRunnable(data.callbackUrl, response),
                PAUSE,
                TimeUnit.SECONDS);
    }

    private void deleteHostVip(CreateHostVipContainer data) {
        CallbackResponse response = new CallbackResponse();
        response.type = "hostvip";
        response.operation = "delete";
        response.hostId = data.host.hostId;
        response.vipId = data.vip.vipId;
        response.status = 200;

        scheduledExecutorService.schedule(
                new WebHookRunnable(data.callbackUrl, response),
                PAUSE,
                TimeUnit.SECONDS);
    }

    public class WebHookRunnable implements Runnable {

        private final String url;
        private final CallbackResponse response;

        public WebHookRunnable(String url, CallbackResponse response) {
            this.url = url;
            this.response = response;
        }

        @Override
        public void run() {
            try {
                RequestBody body = RequestBody.create(Const.JSON_MEDIA_TYPE, gson.toJson(response));
                com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                client.newCall(request).execute();
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }

    }

}
