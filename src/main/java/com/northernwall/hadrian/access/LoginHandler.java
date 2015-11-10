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
import com.northernwall.hadrian.domain.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(LoginHandler.class);

    private final Access access;
    private final int cookieExpiry;

    public LoginHandler(Access access) {
        this.access = access;
        cookieExpiry = 24*60*60*1000;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (request.getMethod().equals(Const.HTTP_GET) && target.equals("/ui/login.html")) {
                request.setHandled(false);
                return;
            }

            if (request.getMethod().equals(Const.HTTP_POST) && target.equals("/login")) {
                String sessionId = access.checkAndStartSession(request, response);
                if (sessionId != null) {
                    User user = access.getUserForSession(sessionId);
                    logger.info("login passed for {}, session {} started", user.getUsername(), sessionId);
                    Cookie cookie = new Cookie(Const.HTTP_SESSION, sessionId);
                    cookie.setMaxAge(cookieExpiry);
                    response.addCookie(cookie);
                    response.setContentType("text/html;charset=utf-8");
                    response.getOutputStream().print("<html><head><meta http-equiv=\"refresh\" content=\"1;url=/ui/\"></head><body></body></html>");
                    request.setHandled(true);
                    return;
                } else {
                    logger.warn("login failed!");
                }
            }

            Cookie[] cookies = request.getCookies();
            if (cookies != null && cookies.length > 0) {
                for (Cookie cookie : request.getCookies()) {
                    if (cookie.getName().equals(Const.HTTP_SESSION)) {
                        User user = access.getUserForSession(cookie.getValue());
                        if (user != null) {
                            request.setAttribute(Const.ATTR_USER, user);
                            //logger.info("found, allowing to continue, user={}", username);
                            request.setHandled(false);
                            return;
                        }
                    }
                }
            }

            logger.info("No session found, redirecting to login");
            access.redirect(response);
            request.setHandled(true);
        } catch (XMLStreamException ex) {
            new RuntimeException(ex);
        }
    }

}
