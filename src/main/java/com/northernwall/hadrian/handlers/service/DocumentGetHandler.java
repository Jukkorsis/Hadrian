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
package com.northernwall.hadrian.handlers.service;

import com.northernwall.hadrian.handlers.BasicHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.northernwall.hadrian.config.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Document;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.parameters.ParameterChangeListener;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.handlers.routing.Http400BadRequestException;
import com.squareup.okhttp.OkHttpClient;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

public class DocumentGetHandler extends BasicHandler implements ParameterChangeListener {

    private final OkHttpClient client;
    private final Parameters parameters;
    private final Map<String, String> tokens;
    private final Type type;
    private final Gson gson;

    public DocumentGetHandler(DataAccess dataAccess, Gson gson, OkHttpClient client, Parameters parameters) {
        super(dataAccess, gson);
        this.client = client;
        this.parameters = parameters;
        this.tokens = new HashMap<>();
        this.type = new TypeToken<Map<String, String>>(){}.getType();
        this.gson = new Gson();
        
        setup();
        parameters.registerChangeListener(this);
    }

    @Override
    public void onChange(List<String> keys) {
        setup();
    }
    
    private void setup() {
        tokens.clear();
        String s = parameters.getString(Const.DOCUMENT_TOKENS, null);
        if (s == null || s.isEmpty()) {
            return;
        }
        tokens.putAll(gson.fromJson(s, type));
    }
    
    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        Service service = getService(request);

        for (Document doc : service.getDocuments()) {
            if (doc.getDocId().equals(request.getParameter("docId"))) {
                com.squareup.okhttp.Request docRequest = new com.squareup.okhttp.Request.Builder()
                        .url(addToken(doc.getLink()))
                        .build();
                try {
                    com.squareup.okhttp.Response resp = client.newCall(docRequest).execute();
                    if (resp.isSuccessful()) {
                        try (InputStream inputStream = resp.body().byteStream()) {
                            byte[] buffer = new byte[50 * 1024];
                            int len = inputStream.read(buffer);
                            while (len != -1) {
                                response.getOutputStream().write(buffer, 0, len);
                                len = inputStream.read(buffer);
                            }
                            response.getOutputStream().flush();
                        }
                        response.setStatus(200);
                        request.setHandled(true);
                        return;
                    } else {
                        throw new Http400BadRequestException("Could not get document " + doc.getTitle() + " at " + doc.getLink() + " status " + resp.code());
                    }
                } catch (UnknownHostException ex) {
                    throw new Http400BadRequestException("Error: Unknown host!");
                } catch (ConnectException | SocketTimeoutException ex) {
                    throw new Http400BadRequestException("Error: Time out!");
                }
            }
        }

        throw new Http400BadRequestException("Could not find document");
    }

    private String addToken(String url) {
        int start = url.indexOf("/")+2;
        int end = url.indexOf("/", start);
        String domain = url.substring(start, end);
        String token = tokens.get(domain);
        if (token == null) {
            return url;
        }
        if (url.indexOf("?") > 0) {
            return url + "&" + token;
        } else {
            return url + "?" + token;
        }
    }

}
