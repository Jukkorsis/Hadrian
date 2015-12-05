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

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.Util;
import com.northernwall.hadrian.domain.WorkItem;
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
    private final static int PAUSE = 1500;

    private final OkHttpClient client;
    private final ScheduledExecutorService scheduledExecutorService;

    public WebHookHandler(OkHttpClient client) {
        this.client = client;
        scheduledExecutorService = Executors.newScheduledThreadPool(5);
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (request.getMethod().equals(Const.HTTP_POST)
                    && (target.equals("/webhook/service")
                    || target.equals("/webhook/host")
                    || target.equals("/webhook/vip")
                    || target.equals("/webhook/hostvip"))) {
                process(request);
                response.setStatus(200);
                request.setHandled(true);
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void process(Request request) throws IOException {
        WorkItem workItem = Util.fromJson(request, WorkItem.class);

        scheduledExecutorService.schedule(
                new WebHookRunnable(workItem.getCallbackUrl(), "200"),
                PAUSE,
                TimeUnit.SECONDS);
    }

    public class WebHookRunnable implements Runnable {

        private final String url;
        private final String response;

        public WebHookRunnable(String url, String response) {
            this.url = url;
            this.response = response;
        }

        @Override
        public void run() {
            try {
                RequestBody body = RequestBody.create(Const.JSON_MEDIA_TYPE, response);
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
