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
package com.northernwall.hadrian.service;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.service.dao.GetWorkItemData;
import com.squareup.okhttp.OkHttpClient;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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
public class WorkItemHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(WorkItemHandler.class);

    private final DataAccess dataAccess;
    private final Gson gson;

    public WorkItemHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        gson = new Gson();
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (request.getMethod().equals(Const.HTTP_GET) && target.equals("/v1/workitems")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                getWorkItems(response);
                response.setStatus(200);
                request.setHandled(true);
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void getWorkItems(HttpServletResponse response) throws IOException {
        response.setContentType(Const.JSON);
        GetWorkItemData getWorkItemData = new GetWorkItemData();
        getWorkItemData.workItems = dataAccess.getWorkItems();
        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            gson.toJson(getWorkItemData, GetWorkItemData.class, jw);
        }
    }

}
