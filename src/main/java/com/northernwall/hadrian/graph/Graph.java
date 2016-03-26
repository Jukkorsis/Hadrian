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
package com.northernwall.hadrian.graph;

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.domain.Service;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.servlet.http.HttpServletResponse;

public class Graph {

    private final BufferedWriter writer;

    public Graph(HttpServletResponse response) throws IOException {
        response.setContentType(Const.TEXT);
        writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
        writer.append("digraph G {");
        writer.newLine();
    }

    public void startSubGraph(int c) throws IOException {
        writer.append(" subgraph cluster_");
        writer.append(Integer.toString(c));
        writer.append(" {");
        writer.newLine();
        writer.append("  color=blue;");
        writer.newLine();
        writer.append("  node [style=filled];");
        writer.newLine();
    }

    public void finishSubGraph(String teamName) throws IOException {
        writer.append("  label = \"");
        writer.append(teamName);
        writer.append("\";");
        writer.newLine();
        writer.append(" }");
        writer.newLine();
        writer.newLine();
    }

    public void newLine() throws IOException {
        writer.newLine();
    }

    public void writeService(Service service, String shape, boolean brief) throws IOException {
        writeService(service, shape, brief, null);
    }

    public void writeService(Service service, String shape, boolean brief, String toolTip) throws IOException {
        writer.append(service.getServiceAbbr());
        writer.append(" [shape=");
        writer.append(shape);
        writer.append(" URL=\"#/Service/");
        writer.append(service.getServiceId());
        writer.append("\"");
        writer.append(" label=<");
        if (brief) {
            writer.append(service.getServiceAbbr());
        } else {
            writer.append(service.getServiceName());
        }
        writer.append(">");
        if (toolTip != null && !toolTip.isEmpty()) {
            writer.append(" tooltip=<");
            writer.append(toolTip);
            writer.append(">");
        }
        writer.append("];");
        writer.newLine();
    }

    public void writeLink(String serviceA, String serviceB) throws IOException {
        writer.append(" ");
        writer.append(serviceA);
        writer.append(" -> ");
        writer.append(serviceB);
        writer.append(";");
        writer.newLine();
    }

    public void close() throws IOException {
        writer.append("}");
        writer.flush();
    }

}
