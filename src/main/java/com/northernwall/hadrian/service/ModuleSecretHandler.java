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
package com.northernwall.hadrian.service;

import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.service.dao.PostSecretModuleData;
import java.io.IOException;
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
public class ModuleSecretHandler extends BasicHandler {

    private final static Logger logger = LoggerFactory.getLogger(ModuleSecretHandler.class);

    private final AccessHelper accessHelper;

    public ModuleSecretHandler(AccessHelper accessHelper, DataAccess dataAccess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostSecretModuleData data = fromJson(request, PostSecretModuleData.class);
        Service service = getService(data.serviceId, null, null);
        Module module = getModule(data.moduleId, null, service);
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "manage secret for module");

        logger.info("secret {} in file {}", data.contents, data.fileName);
        
        response.setStatus(200);
        request.setHandled(true);
    }

}
