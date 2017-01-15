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
package com.northernwall.hadrian.workItem;

import com.google.gson.Gson;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.workItem.dao.CallbackData;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Thurston
 */
public class WorkItemCallbackHandler extends BasicHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(WorkItemCallbackHandler.class);

    private final WorkItemProcessor workItemProcessor;

    public WorkItemCallbackHandler(Gson gson, WorkItemProcessor workItemProcessor) {
        super(null, gson);
        this.workItemProcessor = workItemProcessor;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        CallbackData data = fromJson(request, CallbackData.class);
        LOGGER.info("Received {} callback {}", data.status, data.requestId);
        workItemProcessor.processCallback(data);
        response.setStatus(200);
        request.setHandled(true);
    }

}
