/*
 * Copyright 2017 Richard Thurston.
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
package com.northernwall.hadrian.handlers.ssh;

import com.google.gson.Gson;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.handlers.routing.Http404NotFoundException;
import com.northernwall.hadrian.handlers.ssh.dao.SshData;
import com.northernwall.hadrian.sshAccess.SshAccess;
import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard
 */
public class SshGetHandler extends BasicHandler {
    private final SshAccess sshAccess;

    public SshGetHandler(DataAccess dataAccess, Gson gson, SshAccess sshAccess) {
        super(dataAccess, gson);
        this.sshAccess = sshAccess;
    }
    
    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        Team team = getTeam(request);
        if (team == null) {
            throw new Http404NotFoundException("Team not found");
        }
        
        SshData sshData = new SshData();
        sshData.teamId = team.getTeamId();
        sshData.title = team.getTeamName();
        sshData.sshEntries = sshAccess.getSshEntries();

        toJson(response, sshData);
        response.setStatus(200);
        request.setHandled(true);
    }
    
}
