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

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.Util;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.CustomFunction;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.service.dao.PostCustomFunctionData;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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
public class CustomFuntionHandler extends AbstractHandler {
    private final static Logger logger = LoggerFactory.getLogger(CustomFuntionHandler.class);
    
    private final DataAccess dataAccess;
    private final OkHttpClient client;

    public CustomFuntionHandler(DataAccess dataAccess, OkHttpClient client) {
        this.dataAccess = dataAccess;
        this.client = client;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.startsWith("/v1/cf/")) {
                switch (request.getMethod()) {
                    case "GET":
                        if (target.matches("/v1/cf/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            doCF(response, target.substring(7, target.length()-37), target.substring(44, target.length()));
                            response.setStatus(200);
                            request.setHandled(true);
                        }
                        break;
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
        PostCustomFunctionData postCFData = Util.fromJson(request, PostCustomFunctionData.class);
        Service service = dataAccess.getService(postCFData.serviceId);
        
        CustomFunction customFunction = new CustomFunction( 
                service.getServiceId(),
                postCFData.name,
                postCFData.method, 
                postCFData.url, 
                postCFData.helpText);
        dataAccess.saveCustomFunction(customFunction);
    }

    private void updateCF(Request request, String id) throws IOException {
        PostCustomFunctionData postCFData = Util.fromJson(request, PostCustomFunctionData.class);

        CustomFunction customFunction = dataAccess.getCustomFunction(id);
        customFunction.setName(postCFData.name);
        customFunction.setMethod(postCFData.method);
        customFunction.setUrl(postCFData.url);
        customFunction.setHelpText(postCFData.helpText);
        
        dataAccess.updateCustomFunction(customFunction);
    }

    private void deleteCF(String id) throws IOException {
        dataAccess.deleteCustomFunction(id);
    }
    
    private void doCF(HttpServletResponse response, String customFunctionId, String hostId) throws IOException {
        CustomFunction customFunction = dataAccess.getCustomFunction(customFunctionId);
        if (customFunction == null) {
            throw new RuntimeException("Could not find custom function");
        }
        Host host = dataAccess.getHost(hostId);
        if (host == null) {
            throw new RuntimeException("Could not find host");
        }
        if (!customFunction.getServiceId().equals(host.getServiceId())) {
            throw new RuntimeException("Custom Function and Host do not belong to the same service");
        }
        com.squareup.okhttp.Request.Builder builder = new com.squareup.okhttp.Request.Builder();
        builder.url(Const.HTTP+customFunction.getUrl().replace(Const.HOST, host.getHostName()));
        if (customFunction.getMethod().equalsIgnoreCase("POST")) {
            RequestBody body = RequestBody.create(Const.JSON_MEDIA_TYPE, "{}");
            builder.post(body);
        }
        com.squareup.okhttp.Request request = builder.build();
        try {
            com.squareup.okhttp.Response resp = client.newCall(request).execute();

            byte[] buffer = new byte[50*1024];
            int len = resp.body().byteStream().read(buffer);
            while (len != -1) {
                response.getOutputStream().write(buffer, 0, len);
                len = resp.body().byteStream().read(buffer);
            }
        } catch (UnknownHostException ex) {
            response.getOutputStream().print("Error: Unknown host!");
        } catch (ConnectException | SocketTimeoutException ex) {
            response.getOutputStream().print("Error: Time out!");
        }
    }

}
