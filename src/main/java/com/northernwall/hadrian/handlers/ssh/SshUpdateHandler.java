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
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.handlers.ssh.dao.PostSshData;
import com.northernwall.hadrian.sshAccess.SshAccess;
import com.northernwall.hadrian.sshAccess.SshEntry;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Richard
 */
public class SshUpdateHandler extends BasicHandler {
    private final SshAccess sshAccess;
    private final AccessHelper accessHelper;

    public SshUpdateHandler(DataAccess dataAccess, Gson gson, SshAccess sshAccess, AccessHelper accessHelper) {
        super(dataAccess, gson);
        this.sshAccess = sshAccess;
        this.accessHelper = accessHelper;
    }
    
    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostSshData data = fromJson(request, PostSshData.class);
        Team team = getTeam(request);
        accessHelper.canUserModify(request, team);
        
        List<SshEntry> sshEntries = sshAccess.getSshEntries();
        
        if (data.sshGrants != null && !data.sshGrants.isEmpty()) {
            team.getSshEntries().clear();
            for (SshEntry entry : data.sshGrants) {
                addEntry(entry, sshEntries, team);
            }
            getDataAccess().saveTeam(team);
        }

        response.setStatus(200);
        request.setHandled(true);
    }

    private void addEntry(SshEntry entry, List<SshEntry> sshEntries, Team team) {
        for (SshEntry sshEntry : sshEntries) {
            if (sshEntry.equals(entry)) {
                return;
            }
        }
    }
    
}
