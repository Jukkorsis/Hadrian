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

package com.northernwall.hadrian.handlers.utility;

import com.northernwall.hadrian.calendar.CalendarHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.module.ModuleArtifactHelper;
import com.northernwall.hadrian.messaging.MessagingCoodinator;
import com.northernwall.hadrian.module.ModuleConfigHelper;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.workItem.WorkItemSender;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 *
 * @author Richard Thurston
 */
public class HealthHandler extends AbstractHandler {

    private final Handler accessHandler;
    private final CalendarHelper calendarHelper;
    private final DataAccess dataAccess;
    private final ModuleArtifactHelper moduleArtifactHelper;
    private final ModuleConfigHelper moduleConfigHelper;
    private final Parameters parameters;
    private final WorkItemSender workItemSender;
    private final MessagingCoodinator messagingCoodinator;
    private final String version;
    
    public HealthHandler(Handler accessHandler, CalendarHelper calendarHelper, DataAccess dataAccess, ModuleArtifactHelper moduleArtifactHelper, ModuleConfigHelper moduleConfigHelper, Parameters parameters, WorkItemSender workItemSender, MessagingCoodinator messagingCoodinator) {
        this.accessHandler = accessHandler;
        this.calendarHelper = calendarHelper;
        this.dataAccess = dataAccess;
        this.moduleArtifactHelper = moduleArtifactHelper;
        this.moduleConfigHelper = moduleConfigHelper;
        this.parameters = parameters;
        this.workItemSender = workItemSender;
        this.messagingCoodinator = messagingCoodinator;
        String temp = getClass().getPackage().getImplementationVersion();
        if (temp == null) {
            version = "unknown";
        } else {
            version = temp;
        }
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
                response.setStatus(200);
                showHealth(response);
                request.setHandled(true);
    }

    private void showHealth(HttpServletResponse response) throws IOException {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        
        HealthWriter writer = new HealthWriter(response.getOutputStream());
        writer.open();
        writer.addLine("App Version", version);
        writer.addLine("Host Name", InetAddress.getLocalHost().getHostName());
        writer.addLine("JVM Name", runtimeMXBean.getSpecName());
        writer.addLine("JVM Version", runtimeMXBean.getSpecVersion());
        writer.addLine("JVM Vendor", runtimeMXBean.getSpecVendor());
        writer.addLine("JVM Threads", threadMXBean.getThreadCount());
        writer.addLine("JVM Peak Threads", threadMXBean.getPeakThreadCount());
        writer.addLine("Current Time", new Date());
        writer.addLine("Start Time", new Date(runtimeMXBean.getStartTime()));
        writer.addClassLine("Class - Access Handler", accessHandler);
        writer.addClassLine("Class - Calendar Helper", calendarHelper);
        writer.addClassLine("Class - Data Access", dataAccess);
        writer.addClassLine("Class - Module Artifact Helper", moduleArtifactHelper);
        writer.addClassLine("Class - Module Config Helper", moduleConfigHelper);
        writer.addClassLine("Class - Parameters", parameters);
        writer.addClassLine("Class - Work Item Sender", workItemSender);
        dataAccess.getHealth(writer);
        messagingCoodinator.getHealth(writer);
        writer.close();
    }

}
