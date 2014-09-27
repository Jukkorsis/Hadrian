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

package com.northernwall.hadrian.handler;

import com.northernwall.hadrian.db.DataAccess;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import net.schmizz.sshj.xfer.InMemorySourceFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Thurston
 */
public class ScriptSourceFile extends InMemorySourceFile {
    private final static Logger logger = LoggerFactory.getLogger(ScriptSourceFile.class);
    
    private final String app;
    private final DataAccess dataAccess;
    private final String text = "Hello World";

    ScriptSourceFile(String app, DataAccess dataAccess) {
        this.app = app;
        this.dataAccess = dataAccess;
    }

    @Override
    public String getName() {
        return app + ".sh";
    }

    @Override
    public long getLength() {
        return text.getBytes().length;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(text.getBytes());
    }

}
