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

import com.northernwall.hadrian.Util;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.CustomFunction;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.service.dao.PostCustomFunctionData;
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
public class CustomFuntionModifyHandler extends AbstractHandler {

    private final AccessHelper accessHelper;
    private final DataAccess dataAccess;

    public CustomFuntionModifyHandler(AccessHelper accessHelper, DataAccess dataAccess) {
        this.accessHelper = accessHelper;
        this.dataAccess = dataAccess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String customFunctionId = target.substring(7, target.length());
        PostCustomFunctionData postCFData = Util.fromJson(request, PostCustomFunctionData.class);

        CustomFunction customFunction = dataAccess.getCustomFunction(postCFData.serviceId, customFunctionId);
        if (customFunction == null) {
            throw new Http404NotFoundException("Could not find custom function");
        }
        Service service = dataAccess.getService(customFunction.getServiceId());
        if (service == null) {
            throw new Http404NotFoundException("Could not find service");
        }
        accessHelper.checkIfUserCanModify(request, service.getTeamId(), "modify custom function");

        customFunction.setName(postCFData.name);
        customFunction.setMethod(postCFData.method);
        customFunction.setUrl(postCFData.url);
        customFunction.setHelpText(postCFData.helpText);

        dataAccess.updateCustomFunction(customFunction);

        response.setStatus(200);
        request.setHandled(true);
    }

}
