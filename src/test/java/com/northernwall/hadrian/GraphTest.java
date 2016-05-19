/*
 * Copyright 2016 Richard Thurston.
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
package com.northernwall.hadrian;

import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleType;
import com.northernwall.hadrian.graph.Graph;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import junit.framework.Assert;
import org.junit.Test;


/**
 *
 * @author rthursto
 */
public class GraphTest {
    
    public GraphTest() {
    }
    
    @Test
    public void testGraph() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Graph graph = new Graph(outputStream, false);
            Module service = new Module("service", "abc-123", 1, ModuleType.Deployable, null, null, null, null, null, null, null, null, null, null, null, null, 0, null, 0, null);
            Module library = new Module("lib-rary", "def-567", 1, ModuleType.Library, null, null, null, null, null, null, null, null, null, null, null, null, 0, null, 0, null);
            graph.writeModule(service);
            graph.writeModule(library);
            graph.close();
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray())));
            Assert.assertEquals("digraph {", inputStream.readLine());
            Assert.assertEquals("service [shape=rectangle URL=\"#/Service/abc-123\" label=<service>];", inputStream.readLine());
            Assert.assertEquals("lib_rary [shape=ellipse URL=\"#/Service/def-567\" label=<lib-rary>];", inputStream.readLine());
            Assert.assertEquals("}", inputStream.readLine());
        } catch (IOException ex) {
            Assert.fail(ex.getMessage());
        }
    }
    
}
