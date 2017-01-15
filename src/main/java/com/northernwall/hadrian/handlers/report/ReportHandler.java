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
package com.northernwall.hadrian.handlers.report;

import com.northernwall.hadrian.ConfigHelper;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.handlers.routing.Http400BadRequestException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 *
 * @author Richard Thurston
 */
public class ReportHandler extends AbstractHandler {

    private final AccessHelper accessHelper;
    private final DataAccess dataAccess;
    private final ConfigHelper configHelper;

    public ReportHandler(AccessHelper accessHelper, DataAccess dataAccess, ConfigHelper configHelper) {
        this.accessHelper = accessHelper;
        this.dataAccess = dataAccess;
        this.configHelper = configHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        accessHelper.checkIfUserIsAdmin(request, "Report");

        String type = request.getParameter("type");
        if (type != null && !type.isEmpty()) {
            type = type.toLowerCase();
            Report report = null;
            switch (type) {
                case "hostsummary":
                    report = new HostSummaryReport(
                            dataAccess,
                            configHelper.getConfig(),
                            response.getWriter());
                    break;
                case "hostfull":
                    report = new HostFullReport(
                            dataAccess,
                            response.getWriter());
                    break;
                default:
                    throw new Http400BadRequestException("Unknown report type");
            }
            report.runReport();
            response.getWriter().flush();
        }

        response.setStatus(200);
        request.setHandled(true);
    }

}
