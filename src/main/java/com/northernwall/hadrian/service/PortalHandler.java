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

import com.squareup.okhttp.OkHttpClient;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Enumeration;
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
public class PortalHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(PortalHandler.class);
    
    private final OkHttpClient client;

    public PortalHandler(OkHttpClient client) {
        this.client = client;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.startsWith("/portal/")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                String temp = target.substring(8);
                int i = temp.indexOf("/");
                getContent(request, response, temp.substring(0,i), temp.substring(i));
                request.setHandled(true);
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void getContent(Request request, HttpServletResponse response, String website, String uri) throws IOException {
        String url = getProtocolDomainPort(website)+uri;
        
        com.squareup.okhttp.Request.Builder builder = new com.squareup.okhttp.Request.Builder();
        builder.url(url);
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            builder.addHeader(name, request.getHeader(name));
        }
        com.squareup.okhttp.Request httpRequest = builder.build();
        try {
            com.squareup.okhttp.Response resp = client.newCall(httpRequest).execute();
            response.setStatus(resp.code());
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

    private String getProtocolDomainPort(String website) {
        //TODO: lookup the website name to get the protocol, domain, and Port
        return "http://127.0.0.1:9090";
    }

}
