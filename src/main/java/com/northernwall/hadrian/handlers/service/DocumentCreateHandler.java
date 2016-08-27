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
package com.northernwall.hadrian.handlers.service;

import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Document;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.handlers.service.dao.PostDocumentData;
import com.northernwall.hadrian.handlers.utility.routingHandler.Http400BadRequestException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

public class DocumentCreateHandler extends BasicHandler {

    private final AccessHelper accessHelper;

    public DocumentCreateHandler(AccessHelper accessHelper, DataAccess dataAccess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostDocumentData data = fromJson(request, PostDocumentData.class);
        Service service = getService(data.serviceId, null);
        accessHelper.checkIfUserCanModify(request, service.getTeamId(), "add a document");

        if (data.title == null) {
            throw new Http400BadRequestException("Failed to add new document, as title is null");
        }
        if (data.link == null) {
            throw new Http400BadRequestException("Failed to add new document, as link is null");
        }
        data.title = data.title.trim();
        data.link = data.link.trim();
        if (data.title.isEmpty()) {
            throw new Http400BadRequestException("Failed to add new document, as title is empty");
        }
        if (data.link.isEmpty()) {
            throw new Http400BadRequestException("Failed to add new document, as link is empty");
        }
        for (Document temp : service.getDocuments()) {
            if (temp.getTitle().equalsIgnoreCase(data.title)) {
                throw new Http400BadRequestException("Failed to add new document, a document with that title already exists");
            }
        }
        if (!data.link.toLowerCase().startsWith(Const.HTTP)
                && !data.link.toLowerCase().startsWith(Const.HTTPS)) {
            data.link = Const.HTTP + data.link;
        }

        Document document = new Document(data.documentType, data.title, data.link);
        service.getDocuments().add(document);
        getDataAccess().updateService(service);

        response.setStatus(200);
        request.setHandled(true);
    }

}
