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
package com.northernwall.hadrian.service;

import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Document;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.service.dao.DeleteDocumentData;
import com.northernwall.hadrian.service.dao.PostDocumentData;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http400BadRequestException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

public class DocumentDeleteHandler extends BasicHandler {

    private final AccessHelper accessHelper;

    public DocumentDeleteHandler(AccessHelper accessHelper, DataAccess dataAccess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        DeleteDocumentData data = fromJson(request, DeleteDocumentData.class);
        Service service = getService(data.serviceId, null);
        accessHelper.checkIfUserCanModify(request, service.getTeamId(), "remove document");

        Document doc = null;
        for (Document temp : service.getDocuments()) {
            if (temp.getDocId().equals(data.docId)) {
                doc = temp;
            }
        }
        
        if (doc != null) {
            service.getDocuments().remove(doc);
            getDataAccess().updateService(service);
        }

        response.setStatus(200);
        request.setHandled(true);
    }

}
