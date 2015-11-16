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

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.Util;
import com.northernwall.hadrian.access.Access;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.service.dao.GetUsersData;
import com.northernwall.hadrian.service.dao.PutServiceData;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Thurston
 */
public class UserHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(UserHandler.class);

    private final Access access;
    private final DataAccess dataAccess;
    private final Gson gson;

    public UserHandler(Access access, DataAccess dataAccess) {
        this.access = access;
        this.dataAccess = dataAccess;
        this.gson = new Gson();
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.matches("/v1/users")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                switch (request.getMethod()) {
                    case "GET":
                        getUsers(response);
                        break;
                }
                response.setStatus(200);
                request.setHandled(true);
            }
            if (target.startsWith("/v1/user/")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                switch (request.getMethod()) {
                    case "PUT":
                        updateUser(request, target.substring(9));
                        break;
                }
                response.setStatus(200);
                request.setHandled(true);
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void getUsers(HttpServletResponse response) throws IOException {
        response.setContentType(Const.JSON);
        GetUsersData users = new GetUsersData();
        for (User user : dataAccess.getUsers()) {
            users.users.add(user);
        }

        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            gson.toJson(users, GetUsersData.class, jw);
        }
    }

    private void updateUser(Request request, String username) throws IOException {
        User user = access.checkIfUserIsAdmin(request, "update user");
        
        User temp = Util.fromJson(request, User.class);
        if (!user.getUsername().equals(temp.getUsername())) {
            dataAccess.updateUser(temp);
        }
    }

}
