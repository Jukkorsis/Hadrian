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
import com.northernwall.hadrian.domain.Service;
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
    
    private final String name;
    private final String text;
    private final long len;

    ScriptSourceFile(String app, DataAccess dataAccess) throws Exception {
        this.name = app + ".sh";
        Service service = dataAccess.getService(app);
        if (service == null) {
            logger.warn("Update to find service {}", app);
            throw new Exception("Update to find service " + app);
        }
        if (!service.enableManage) {
            logger.warn("Service {} is not enabled for manage/deploy", app);
            throw new Exception("Service " + app + " is not enabled for manage/deploy");
        }
        if (service.script == null || service.script.isEmpty()) {
            logger.warn("Script for service {} is empty", app);
            throw new Exception("Script for service " + app + " is empty");
        }
        text = service.script;
        len = text.getBytes().length;
        logger.info("Script for {} loaded, {} bytes", app, len);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getLength() {
        return len;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(text.getBytes());
    }

}
