package com.northernwall.hadrian.graph;

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.domain.Service;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.servlet.http.HttpServletResponse;

public class Graph {

    private final BufferedWriter writer;
    private final boolean brief;

    public Graph(HttpServletResponse response, boolean brief) throws IOException {
        response.setContentType(Const.TEXT);
        writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
        writer.append("digraph G {");
        writer.newLine();
        this.brief = brief;
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

    public void writeService(Service service, String shape) throws IOException {
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
            writer.append("<br/>");
            writer.append("Business Impact: ");
            writer.append(service.getBusinessImpact());
            writer.append("<br/>");
            writer.append(service.getPiiUsage());
        }
        writer.append(">");
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
