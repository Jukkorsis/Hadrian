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
package com.northernwall.hadrian.handlers.graph;

import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleType;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class Graph {

    private final BufferedWriter writer;

    public Graph(OutputStream outputStream, boolean isStruct) throws IOException {
        writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        if (isStruct) {
            writer.append("digraph structs {");
            writer.newLine();
            writer.append("node[shape=record]");
        } else {
            writer.append("digraph {");
        }
        writer.newLine();
    }

    public void newLine() throws IOException {
        writer.newLine();
    }

    public void writeModule(Module module, String colour) throws IOException {
        writer.append(sanitize(module.getModuleName()));
        writer.append(" [shape=");
        if (module.getModuleType() == ModuleType.Deployable) {
            writer.append("rectangle");
        } else if (module.getModuleType() == ModuleType.Simulator) {
            writer.append("parallelogram");
        } else {
            writer.append("ellipse");
        }
        writer.append(" URL=\"#/Service/");
        writer.append(module.getServiceId());
        writer.append("\"");
        writer.append(" label=<");
        writer.append(module.getModuleName().trim());
        writer.append(">");
       writer.append(",color=\"");
        writer.append(colour);
        writer.append("\"];");
        writer.newLine();
    }

    public void writeModuleStructure(Module module, List<Module> libraries, String colour) throws IOException {
        writer.append(sanitize(module.getModuleName()));
        writer.append(" [label=\"");
        writer.append(module.getModuleName());
        if (libraries != null && !libraries.isEmpty()) {
            writer.append("|{");
            for (int c = 0; c < libraries.size(); c++) {
                writer.append(libraries.get(c).getModuleName());
                if (c < libraries.size() - 1) {
                    writer.append("|");
                }
            }
            writer.append("}");
        }
        writer.append("\",color=\"");
        writer.append(colour);
        writer.append("\"];");
        writer.newLine();
    }

    public void writeLink(String moduleA, String moduleB) throws IOException {
        writer.append(" ");
        writer.append(sanitize(moduleA));
        writer.append(" -> ");
        writer.append(sanitize(moduleB));
        writer.append(";");
        writer.newLine();
    }

    public void close() throws IOException {
        writer.append("}");
        writer.flush();
    }

    public String sanitize(String text) {
        return text.replace("-", "_").replace(".", "_").replace("=", "_").replace(" ", "_");
    }

}
