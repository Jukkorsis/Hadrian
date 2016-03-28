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
import com.northernwall.hadrian.service.dao.PostCustomFunctionData;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http404NotFoundException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard Thurston
 */
public class CustomFuntionModifyHandler extends BasicHandler {

    private final AccessHelper accessHelper;

    public CustomFuntionModifyHandler(AccessHelper accessHelper, DataAccess dataAccess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String customFunctionId = target.substring(7, target.length());
        PostCustomFunctionData postCFData = fromJson(request, PostCustomFunctionData.class);

        CustomFunction customFunction = getDataAccess().getCustomFunction(postCFData.serviceId, customFunctionId);
        if (customFunction == null) {
            throw new Http404NotFoundException("Could not find custom function");
        }
        Service service = getDataAccess().getService(customFunction.getServiceId());
        if (service == null) {
            throw new Http404NotFoundException("Could not find service");
        }
        accessHelper.checkIfUserCanModify(request, service.getTeamId(), "modify custom function");

        customFunction.setName(postCFData.name);
        customFunction.setMethod(postCFData.method);
        customFunction.setUrl(postCFData.url);

        getDataAccess().updateCustomFunction(customFunction);

        response.setStatus(200);
        request.setHandled(true);
    }

}
