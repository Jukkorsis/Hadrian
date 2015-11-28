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
package com.northernwall.hadrian.proxy;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.proxy.dao.GetProxyData;
import com.squareup.okhttp.OkHttpClient;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
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
public class PostProxyHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(PostProxyHandler.class);

    private final OkHttpClient client;
    private final Gson gson;

    public PostProxyHandler(OkHttpClient client) {
        this.client = client;
        gson = new Gson();
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies != null && cookies.length > 0) {
                for (Cookie cookie : request.getCookies()) {
                    if (cookie.getName().equals(Const.COOKIE_PORTAL_NAME)) {
                        getContent(request, response, cookie.getName());
                        request.setHandled(true);
                        return;
                    }
                }
            }
            if (request.getMethod().equals(Const.HTTP_GET) && target.equals("/v1/portal")) {
                getPortals(request, response);
                request.setHandled(true);
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void getContent(Request request, HttpServletResponse response, String portalName) throws IOException {
        String url = getProtocolDomainPort(portalName) + request.getRequestURI();
        logger.info("url={}", url);

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

            byte[] buffer = new byte[50 * 1024];
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

    private String getProtocolDomainPort(String portalName) {
        //TODO: lookup the website name to get the protocol, domain, and Port
        return "http://127.0.0.1:9090";
    }

    private void getPortals(Request request, HttpServletResponse response) throws IOException {
        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            jw.beginArray();
            GetProxyData k = new GetProxyData();
            k.name = "kibana";
            k.url = "/portal/" + request.getAttribute(Const.ATTR_SESSION) + "/kibana";
            gson.toJson(k, GetProxyData.class, jw);
            GetProxyData g = new GetProxyData();
            g.name = "gitlab";
            g.url = "/portal/" + request.getAttribute(Const.ATTR_SESSION) + "/gitlab";
            gson.toJson(g, GetProxyData.class, jw);
            jw.endArray();
        }
    }

}
