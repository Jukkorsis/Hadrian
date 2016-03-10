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
import com.northernwall.hadrian.domain.CustomFunction;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http404NotFoundException;
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
public class CustomFuntionDeleteHandler extends AbstractHandler {

    private final AccessHelper accessHelper;
    private final DataAccess dataAccess;

    public CustomFuntionDeleteHandler(AccessHelper accessHelper, DataAccess dataAccess) {
        this.accessHelper = accessHelper;
        this.dataAccess = dataAccess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String serviceId = target.substring(7, 43);
        String customFunctionId = target.substring(44);

        CustomFunction customFunction = dataAccess.getCustomFunction(serviceId, customFunctionId);
        if (customFunction == null) {
            throw new Http404NotFoundException("Could not find custom function");
        }
        Service service = dataAccess.getService(customFunction.getServiceId());
        if (service == null) {
            throw new Http404NotFoundException("Could not find service");
        }
        accessHelper.checkIfUserCanModify(request, service.getTeamId(), "delete custom function");

        dataAccess.deleteCustomFunction(serviceId, customFunctionId);

        response.setStatus(200);
        request.setHandled(true);
    }

}
