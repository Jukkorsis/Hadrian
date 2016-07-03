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
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Document;
import com.northernwall.hadrian.domain.Service;
import com.squareup.okhttp.OkHttpClient;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

public class DocumentGetHandler extends BasicHandler {

    private final OkHttpClient client;

    public DocumentGetHandler(DataAccess dataAccess, OkHttpClient client) {
        super(dataAccess);
        this.client = client;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        Service service = getService(request);

        boolean found = false;
        for (Document doc : service.getDocuments()) {
            if (!found && doc.getDocId().equals(request.getParameter("docId"))) {
                found = true;
                com.squareup.okhttp.Request docRequest = new com.squareup.okhttp.Request.Builder()
                        .url(Const.HTTP + doc.getLink())
                        .build();
                try {
                    com.squareup.okhttp.Response resp = client.newCall(docRequest).execute();
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
            }
        }
        
        if (!found) {
            response.getWriter().append("Could not find document.").flush();
        }

        response.setStatus(200);
        request.setHandled(true);
    }

}
