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
package com.northernwall.hadrian.webhook.simple;

import com.google.gson.Gson;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.Util;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.webhook.dao.CallbackData;
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
public class SimpleWebHookHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(SimpleWebHookHandler.class);

    private final OkHttpClient client;
    private final Gson gson;
    private final int pause;
    private final ScheduledExecutorService scheduledExecutorService;
    private final String url;

    public SimpleWebHookHandler(OkHttpClient client, Parameters paramerters) {
        this.client = client;
        this.gson = new Gson();
        this.pause = paramerters.getInt(Const.SIMPLE_WEB_HOOK_DELAY, Const.SIMPLE_WEB_HOOK_DELAY_DEFAULT);
        this.scheduledExecutorService = Executors.newScheduledThreadPool(5);
        this.url = paramerters.getString(Const.SIMPLE_WEB_HOOK_CALLBACK_URL, Const.SIMPLE_WEB_HOOK_CALLBACK_URL_DEFAULT);
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (request.getMethod().equals(Const.HTTP_POST) && target.equals("/webhook/simple")) {
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
        
        CallbackData callbackData = new CallbackData();
        callbackData.status = Const.WEB_HOOK_STATUS_SUCCESS;
        callbackData.errorCode = 0;
        callbackData.errorDescription = " ";
        callbackData.output = "No output";
        callbackData.requestId = request.getHeader("X-Request-Id");

        scheduledExecutorService.schedule(
                new WebHookRunnable(callbackData),
                pause,
                TimeUnit.SECONDS);
    }

    public class WebHookRunnable implements Runnable {

        private final CallbackData callbackData;

        public WebHookRunnable(CallbackData callbackData) {
            this.callbackData = callbackData;
        }

        @Override
        public void run() {
            try {
                RequestBody body = RequestBody.create(Const.JSON_MEDIA_TYPE, gson.toJson(callbackData));
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
