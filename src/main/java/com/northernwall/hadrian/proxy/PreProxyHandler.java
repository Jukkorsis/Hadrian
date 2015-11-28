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
package com.northernwall.hadrian.proxy;

import com.northernwall.hadrian.Const;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
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
public class PreProxyHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(PreProxyHandler.class);

    public PreProxyHandler() {
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (request.getMethod().equals(Const.HTTP_GET) && target.matches("/portal/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                String sessionId = target.substring(8, 43);
                String portalName = target.substring(45);
                logger.info("session={} portalName={}", sessionId, portalName);
                setupPortal(response, sessionId, portalName);
                request.setHandled(true);
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void setupPortal(HttpServletResponse response, String sessionId, String portalName) throws IOException {
        Cookie cookie;

        //TODO: should check to see if the session cookie already exists, if it does then the users did not open in a private tab
        cookie = new Cookie("FOO", "BAR");
        response.addCookie(cookie);

        cookie = new Cookie(Const.COOKIE_PORTAL_NAME, portalName);
        response.addCookie(cookie);

        cookie = new Cookie(Const.COOKIE_SESSION, sessionId);
        cookie.setMaxAge(Const.COOKIE_EXPRIY);
        response.addCookie(cookie);

        response.setContentType("text/html;charset=utf-8");
        response.getOutputStream().print("<html><head><meta http-equiv=\"refresh\" content=\"1;url="+getdefaultUri(portalName)+"\"></head><body></body></html>");
    }

    private String getdefaultUri(String portalName) {
        //TODO: lookup the website name to get the protocol, domain, and Port
        return "/";
    }

}
