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
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.CustomFunction;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http400BadRequestException;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http404NotFoundException;
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

/**
 *
 * @author Richard Thurston
 */
public class CustomFuntionExecHandler extends BasicHandler {

    private final AccessHelper accessHelper;
    private final OkHttpClient client;

    public CustomFuntionExecHandler(AccessHelper accessHelper, DataAccess dataAccess, OkHttpClient client) {
        super(dataAccess);
        this.accessHelper = accessHelper;
        this.client = client;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        Service service = getService(request);

        String customFunctionId = request.getParameter("cfId");
        CustomFunction customFunction = getDataAccess().getCustomFunction(service.getServiceId(), customFunctionId);
        if (customFunction == null) {
            throw new Http404NotFoundException("Could not find custom function");
        }
        if (customFunction.isTeamOnly()) {
            accessHelper.checkIfUserCanModify(request, service.getTeamId(), "execute a private custom function");
        }

        Host host = getHost(request, service);
        
        if (!customFunction.getServiceId().equals(host.getServiceId())) {
            throw new Http400BadRequestException("Custom Function and Host do not belong to the same service");
        }
        if (!customFunction.getModuleId().equals(host.getModuleId())) {
            throw new Http400BadRequestException("Custom Function and Host do not belong to the same module");
        }
        
        Builder builder = new Builder();
        builder.url(Const.HTTP + customFunction.getUrl().replace(Const.HOST, host.getHostName()));
        if (customFunction.getMethod().equalsIgnoreCase("POST")) {
            RequestBody body = RequestBody.create(Const.JSON_MEDIA_TYPE, "{}");
            builder.post(body);
        }
        com.squareup.okhttp.Request cfRequest = builder.build();
        try {
            com.squareup.okhttp.Response resp = client.newCall(cfRequest).execute();
            try (InputStream inputStream = resp.body().byteStream()) {
                byte[] buffer = new byte[50 * 1024];
                int len = inputStream.read(buffer);
                while (len != -1) {
                    response.getOutputStream().write(buffer, 0, len);
                    len = inputStream.read(buffer);
                }
                response.getOutputStream().flush();
            }
        } catch (UnknownHostException ex) {
            response.getOutputStream().print("Error: Unknown host!");
        } catch (ConnectException | SocketTimeoutException ex) {
            response.getOutputStream().print("Error: Time out!");
        }
        response.setStatus(200);
        request.setHandled(true);
    }

}
