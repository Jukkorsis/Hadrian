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
package com.northernwall.hadrian.handlers.user;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.handlers.user.dao.GetUsersData;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 *
 * @author Richard Thurston
 */
public class UserGetHandler extends AbstractHandler {


    private final DataAccess dataAccess;
    private final Gson gson;

    public UserGetHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        this.gson = new Gson();
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType(Const.JSON);
        GetUsersData getUsersData = new GetUsersData();

        List<User> users = dataAccess.getUsers();
        Collections.sort(users);
        for (User user : users) {
            getUsersData.users.add(user);
        }

        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            gson.toJson(getUsersData, GetUsersData.class, jw);
        }
        response.setStatus(200);
        request.setHandled(true);
    }

}
