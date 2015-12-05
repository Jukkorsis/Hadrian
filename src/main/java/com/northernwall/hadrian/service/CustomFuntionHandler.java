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
import com.northernwall.hadrian.access.Access;
import com.northernwall.hadrian.access.AccessException;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.CustomFunction;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.service.dao.PostCustomFunctionData;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;
import java.io.IOException;
import java.io.InputStream;
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
    
    private final Access access;
    private final DataAccess dataAccess;
    private final OkHttpClient client;

    public CustomFuntionHandler(Access access, DataAccess dataAccess, OkHttpClient client) {
        this.access = access;
        this.dataAccess = dataAccess;
        this.client = client;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.startsWith("/v1/cf/")) {
                switch (request.getMethod()) {
                    case "GET":
                        if (target.matches("/v1/cf/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            String serviceId = target.substring(7, 43);
                            String cfId = target.substring(44, 80);
                            String hostId = target.substring(81);
                            doCF(request, response, serviceId, cfId, hostId);
                        } else {
                            throw new RuntimeException("Unknown custom function operation");
                        }
                        break;
                    case "POST":
                        if (target.matches("/v1/cf/cf")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            createCF(request);
                        } else {
                            throw new RuntimeException("Unknown custom function operation");
                        }
                        break;
                    case "PUT":
                        if (target.matches("/v1/cf/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            String cfId = target.substring(7, target.length());
                            updateCF(request, cfId);
                        } else {
                            throw new RuntimeException("Unknown custom function operation");
                        }
                        break;
                    case "DELETE":
                        if (target.matches("/v1/cf/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            String serviceId = target.substring(7, 43);
                            String cfId = target.substring(44);
                            deleteCF(request, serviceId, cfId);
                        } else {
                            throw new RuntimeException("Unknown custom function operation");
                        }
                        break;
                    default:
                        throw new RuntimeException("Unknown custom function operation");
                }
                response.setStatus(200);
                request.setHandled(true);
            }
        } catch (AccessException e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target);
            response.setStatus(401);
            request.setHandled(true);
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
            request.setHandled(true);
        }
    }

    private void createCF(Request request) throws IOException {
        PostCustomFunctionData postCFData = Util.fromJson(request, PostCustomFunctionData.class);
        Service service = dataAccess.getService(postCFData.serviceId);
        if (service == null) {
            throw new RuntimeException("Could not find service");
        }
        access.checkIfUserCanModify(request, service.getTeamId(), "create custom function");
        
        CustomFunction customFunction = new CustomFunction( 
                service.getServiceId(),
                postCFData.name,
                postCFData.method, 
                postCFData.url, 
                postCFData.helpText,
                postCFData.teamOnly);
        dataAccess.saveCustomFunction(customFunction);
    }

    private void updateCF(Request request, String customFunctionId) throws IOException {
        PostCustomFunctionData postCFData = Util.fromJson(request, PostCustomFunctionData.class);

        CustomFunction customFunction = dataAccess.getCustomFunction(postCFData.serviceId, customFunctionId);
        if (customFunction == null) {
            throw new RuntimeException("Could not find custom function");
        }
        Service service = dataAccess.getService(customFunction.getServiceId());
        if (service == null) {
            throw new RuntimeException("Could not find service");
        }
        access.checkIfUserCanModify(request, service.getTeamId(), "modify custom function");

        customFunction.setName(postCFData.name);
        customFunction.setMethod(postCFData.method);
        customFunction.setUrl(postCFData.url);
        customFunction.setHelpText(postCFData.helpText);
        
        dataAccess.updateCustomFunction(customFunction);
    }

    private void deleteCF(Request request, String serviceId, String customFunctionId) throws IOException {
        CustomFunction customFunction = dataAccess.getCustomFunction(serviceId, customFunctionId);
        if (customFunction == null) {
            throw new RuntimeException("Could not find custom function");
        }
        Service service = dataAccess.getService(customFunction.getServiceId());
        if (service == null) {
            throw new RuntimeException("Could not find service");
        }
        access.checkIfUserCanModify(request, service.getTeamId(), "delete custom function");

        dataAccess.deleteCustomFunction(serviceId, customFunctionId);
    }
    
    private void doCF(Request request, HttpServletResponse response, String serviceId, String customFunctionId, String hostId) throws IOException {
        CustomFunction customFunction = dataAccess.getCustomFunction(serviceId, customFunctionId);
        if (customFunction == null) {
            throw new RuntimeException("Could not find custom function");
        }
        Host host = dataAccess.getHost(serviceId, hostId);
        if (host == null) {
            throw new RuntimeException("Could not find host");
        }
        Service service = dataAccess.getService(customFunction.getServiceId());
        if (service == null) {
            throw new RuntimeException("Could not find service");
        }
        if (customFunction.isTeamOnly()) {
            access.checkIfUserCanModify(request, service.getTeamId(), "execute a private custom function");
        }
        if (!customFunction.getServiceId().equals(host.getServiceId())) {
            throw new RuntimeException("Custom Function and Host do not belong to the same service");
        }
        Builder builder = new Builder();
        builder.url(Const.HTTP+customFunction.getUrl().replace(Const.HOST, host.getHostName()));
        if (customFunction.getMethod().equalsIgnoreCase("POST")) {
            RequestBody body = RequestBody.create(Const.JSON_MEDIA_TYPE, "{}");
            builder.post(body);
        }
        com.squareup.okhttp.Request httpRequest = builder.build();
        try {
            com.squareup.okhttp.Response resp = client.newCall(httpRequest).execute();
            InputStream inputStream = resp.body().byteStream();

            byte[] buffer = new byte[50*1024];
            int len = inputStream.read(buffer);
            while (len != -1) {
                response.getOutputStream().write(buffer, 0, len);
                len = inputStream.read(buffer);
            }
        } catch (UnknownHostException ex) {
            response.getOutputStream().print("Error: Unknown host!");
        } catch (ConnectException | SocketTimeoutException ex) {
            response.getOutputStream().print("Error: Time out!");
        }
    }

}
