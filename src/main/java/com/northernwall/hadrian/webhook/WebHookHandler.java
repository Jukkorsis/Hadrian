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

import com.northernwall.hadrian.Util;
import com.northernwall.hadrian.webhook.dao.CallbackResponse;
import com.northernwall.hadrian.webhook.dao.PostVipContainer;
import com.northernwall.hadrian.webhook.dao.PutHostContainer;
import com.northernwall.hadrian.webhook.dao.PostHostVipContainer;
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

    private final WebHookSender webHookHelper;
    private final ScheduledExecutorService scheduledExecutorService;

    public WebHookHandler(WebHookSender webHookHelper) {
        this.webHookHelper = webHookHelper;
        scheduledExecutorService = Executors.newScheduledThreadPool(5);
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.startsWith("/webhook/")) {
                switch (request.getMethod()) {
                    case "POST":
                        if (target.matches("/webhook/host")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            postHost(request);
                            response.setStatus(200);
                            request.setHandled(true);
                        } else if (target.matches("/webhook/vip")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            postVip(request);
                            response.setStatus(200);
                            request.setHandled(true);
                        } else if (target.matches("/webhook/hostvip")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            postHostVip(request);
                            response.setStatus(200);
                            request.setHandled(true);
                        }
                        break;
                    case "PUT":
                        if (target.matches("/webhook/host")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            putHost(request);
                            response.setStatus(200);
                            request.setHandled(true);
                        } else if (target.matches("/webhook/vip")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            putVip(request);
                            response.setStatus(200);
                            request.setHandled(true);
                        }
                        break;
                    case "DELETE":
                        if (target.matches("/webhook/host")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            deleteHost(request);
                            response.setStatus(200);
                            request.setHandled(true);
                        } else if (target.matches("/webhook/vip")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            deleteVip(request);
                            response.setStatus(200);
                            request.setHandled(true);
                        } else if (target.matches("/webhook/hostvip")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            deleteHostVip(request);
                            response.setStatus(200);
                            request.setHandled(true);
                        }
                        break;
                }
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void postHost(Request request) throws IOException {
        PutHostContainer data = Util.fromJson(request, PutHostContainer.class);
        
        CallbackResponse response = new CallbackResponse();
        response.type = "host";
        response.operation = "POST";
        response.hostId = data.host.hostId;
        response.status = 200;
        
        scheduledExecutorService.schedule(
                new WebHookRunnable(data.callbackUrl, response, webHookHelper), 
                PAUSE, 
                TimeUnit.SECONDS);
    }
    
    private void putHost(Request request) throws IOException {
        PutHostContainer data = Util.fromJson(request, PutHostContainer.class);
        
        CallbackResponse response = new CallbackResponse();
        response.type = "host";
        response.operation = "PUT";
        response.hostId = data.host.hostId;
        response.status = 200;
        
        scheduledExecutorService.schedule(
                new WebHookRunnable(data.callbackUrl, response, webHookHelper), 
                PAUSE, 
                TimeUnit.SECONDS);
    }
    
    private void deleteHost(Request request) throws IOException {
        PutHostContainer data = Util.fromJson(request, PutHostContainer.class);
        
        CallbackResponse response = new CallbackResponse();
        response.type = "host";
        response.operation = "DELETE";
        response.hostId = data.host.hostId;
        response.status = 200;
        
        scheduledExecutorService.schedule(
                new WebHookRunnable(data.callbackUrl, response, webHookHelper), 
                PAUSE, 
                TimeUnit.SECONDS);
    }
    
    private void postVip(Request request) throws IOException {
        PostVipContainer data = Util.fromJson(request, PostVipContainer.class);
        
        CallbackResponse response = new CallbackResponse();
        response.type = "vip";
        response.operation = "POST";
        response.vipId = data.vip.vipId;
        response.status = 200;
        
        scheduledExecutorService.schedule(
                new WebHookRunnable(data.callbackUrl, response, webHookHelper), 
                PAUSE, 
                TimeUnit.SECONDS);
    }
    
    private void putVip(Request request) throws IOException {
        PostVipContainer data = Util.fromJson(request, PostVipContainer.class);
        
        CallbackResponse response = new CallbackResponse();
        response.type = "vip";
        response.operation = "PUT";
        response.vipId = data.vip.vipId;
        response.status = 200;
        
        scheduledExecutorService.schedule(
                new WebHookRunnable(data.callbackUrl, response, webHookHelper), 
                PAUSE, 
                TimeUnit.SECONDS);
    }
    
    private void deleteVip(Request request) throws IOException {
        PostVipContainer data = Util.fromJson(request, PostVipContainer.class);
        
        CallbackResponse response = new CallbackResponse();
        response.type = "vip";
        response.operation = "DELETE";
        response.vipId = data.vip.vipId;
        response.status = 200;
        
        scheduledExecutorService.schedule(
                new WebHookRunnable(data.callbackUrl, response, webHookHelper), 
                PAUSE, 
                TimeUnit.SECONDS);
    }
    
    private void postHostVip(Request request) throws IOException {
        PostHostVipContainer data = Util.fromJson(request, PostHostVipContainer.class);
        
        CallbackResponse response = new CallbackResponse();
        response.type = "hostvip";
        response.operation = "POST";
        response.hostId = data.host.hostId;
        response.vipId = data.vip.vipId;
        response.status = 200;
        
        scheduledExecutorService.schedule(
                new WebHookRunnable(data.callbackUrl, response, webHookHelper), 
                PAUSE, 
                TimeUnit.SECONDS);
    }
    
    private void deleteHostVip(Request request) throws IOException {
        PostHostVipContainer data = Util.fromJson(request, PostHostVipContainer.class);
        
        CallbackResponse response = new CallbackResponse();
        response.type = "hostvip";
        response.operation = "DELETE";
        response.hostId = data.host.hostId;
        response.vipId = data.vip.vipId;
        response.status = 200;
        
        scheduledExecutorService.schedule(
                new WebHookRunnable(data.callbackUrl, response, webHookHelper), 
                PAUSE, 
                TimeUnit.SECONDS);
    }
    
    public class WebHookRunnable implements Runnable {
        private final WebHookSender urlHelper;
        private final String url;
        private final CallbackResponse response;

        public WebHookRunnable(String url, CallbackResponse response, WebHookSender urlHelper) {
            this.url = url;
            this.response = response;
            this.urlHelper = urlHelper;
        }

        @Override
        public void run() {
            try {
                urlHelper.post(url, response);
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }

    }

}
