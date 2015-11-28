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
package com.northernwall.hadrian.access;

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.domain.UserSession;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.User;
import java.io.IOException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleAccess extends Access {

    private final static Logger logger = LoggerFactory.getLogger(SimpleAccess.class);

    public SimpleAccess(DataAccess dataAccess) {
        super(dataAccess);
    }

    @Override
    public boolean checkLoginUI(String target, Request request) throws IOException {
        if (request.getMethod().equals(Const.HTTP_GET) && target.equals("/ui/login.html")) {
            request.setHandled(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean checkLoginCallback(String target, Request request) {
        return request.getMethod().equals(Const.HTTP_POST) && target.equals("/login");
    }

    @Override
    public String checkAndStartSession(Request request, HttpServletResponse response) {
        String username = null;
        String password = null;
        MultiMap<String> mm = new MultiMap<>();
        request.extractFormParameters(mm);
        for (String key : mm.keySet()) {
            if (key.equals("username")) {
                username = mm.getValue(key, 0);
            } else if (key.equals("password")) {
                password = mm.getValue(key, 0);
            }
        }
        //check username and password
        if (checkCreds(username, password)) {
            UserSession session = new UserSession(username, System.currentTimeMillis() + Const.COOKIE_EXPRIY);
            dataAccess.saveUserSession(session);
            return session.getSessionId();
        } else {
            return null;
        }
    }

    protected boolean checkCreds(String username, String password) {
        return (username != null && password != null);
    }

    @Override
    public void redirect(HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        response.getOutputStream().print("<html><head><meta http-equiv=\"refresh\" content=\"1;url=/ui/login.html\"></head><body></body></html>");
    }

}
