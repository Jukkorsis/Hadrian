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

import com.northernwall.hadrian.Util;
import com.northernwall.hadrian.webhook.WebHookSender;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.CustomFunction;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.service.dao.PostCFData;
import java.io.IOException;
import java.util.List;
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
public class CFHandler extends AbstractHandler {
    private final static Logger logger = LoggerFactory.getLogger(CFHandler.class);
    
    private final DataAccess dataAccess;
    private final WebHookSender webHookHelper;

    public CFHandler(DataAccess dataAccess, WebHookSender webHookHelper) {
        this.webHookHelper = webHookHelper;
        this.dataAccess = dataAccess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.startsWith("/v1/cf/")) {
                switch (request.getMethod()) {
                    case "POST":
                        if (target.matches("/v1/cf/cf")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            createCF(request);
                            response.setStatus(200);
                            request.setHandled(true);
                        }
                        break;
                    case "PUT":
                        if (target.matches("/v1/cf/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            updateCF(request, target.substring(7, target.length()));
                            response.setStatus(200);
                            request.setHandled(true);
                        }
                        break;
                    case "DELETE":
                        if (target.matches("/v1/cf/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            deleteCF(target.substring(7, target.length()));
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

    private void createCF(Request request) throws IOException {
        PostCFData postCFData = Util.fromJson(request, PostCFData.class);
        Service service = dataAccess.getService(postCFData.serviceId);
        
        
        CustomFunction customFunction = new CustomFunction( 
                service.getServiceId(),
                postCFData.name,
                postCFData.protocol, 
                postCFData.url, 
                postCFData.style, 
                postCFData.helpText);
        dataAccess.saveCustomFunction(customFunction);
    }

    private void updateCF(Request request, String id) throws IOException {
    }

    private void deleteCF(String id) throws IOException {
    }

}
