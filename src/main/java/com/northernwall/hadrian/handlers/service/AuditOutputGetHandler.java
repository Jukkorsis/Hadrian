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
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.db.DataAccess;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Thurston
 */
public class AuditOutputGetHandler extends BasicHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(AuditOutputGetHandler.class);

    public AuditOutputGetHandler(DataAccess dataAccess) {
        super(dataAccess);
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String serviceId = request.getParameter("serviceId");
        String auditId = request.getParameter("auditId");

        LOGGER.info("Request for Audit Output for {} on service {}", auditId, serviceId);

        response.setContentType(Const.TEXT);
        try (Writer w = new OutputStreamWriter(response.getOutputStream())) {
            String output = getDataAccess().getAuditOutput(serviceId, auditId);
            if (output == null) {
                output = "";
            }
            w.write(output);
        }
        response.setStatus(200);
        request.setHandled(true);
    }

}
