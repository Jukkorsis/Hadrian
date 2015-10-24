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
package com.northernwall.hadrian.service;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.DataStore;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
public class DataStoreHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(DataStoreHandler.class);

    private final DataAccess dataAccess;
    private final Gson gson;

    public DataStoreHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        this.gson = new Gson();
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.matches("/v1/datastore/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                switch (request.getMethod()) {
                    case "GET":
                        getDataStore(response, target.substring(14, target.length()));
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

    private void getDataStore(HttpServletResponse response, String id) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        DataStore dataStore = dataAccess.getDataStore(id);
        if (dataStore == null) {
            throw new RuntimeException("Could not find dataStore with id '" + id + "'");
        }
        
        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            gson.toJson(dataStore, DataStore.class, jw);
        }
    }

}