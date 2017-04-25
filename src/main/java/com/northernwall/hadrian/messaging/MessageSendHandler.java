/*
 * Copyright 2016 Richard Thurston.
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
package com.northernwall.hadrian.messaging;

import com.google.gson.Gson;
import com.northernwall.hadrian.messaging.dao.PostMessageData;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.db.SearchResult;
import com.northernwall.hadrian.db.SearchSpace;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.handlers.routing.Http400BadRequestException;
import com.northernwall.hadrian.handlers.routing.Http404NotFoundException;
import com.northernwall.hadrian.parameters.Parameters;
import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author rthursto
 */
public class MessageSendHandler extends BasicHandler {

    private final Parameters parameters;
    private final MessagingCoodinator messagingCoodinator;

    public MessageSendHandler(DataAccess dataAccess, Parameters parameters, Gson gson, MessagingCoodinator messagingCoodinator) {
        super(dataAccess, gson);
        this.parameters = parameters;
        this.messagingCoodinator = messagingCoodinator;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostMessageData data = fromJson(request, PostMessageData.class);

        String pattern = parameters.getString("messageType." + data.messageTypeName + ".pattern", null);
        if (pattern == null) {
            throw new Http400BadRequestException("Could not find pattern for name " + data.messageTypeName);
        }

        if (data.gitGroup != null
                && !data.gitGroup.isEmpty()
                && data.gitProject != null
                && !data.gitProject.isEmpty()) {
            processByGit(data, pattern);
        } else {
            processByService(data, pattern);
        }

        response.setStatus(200);
        request.setHandled(true);
    }

    private void processByGit(PostMessageData data, String pattern) {
        SearchResult searchResult = getDataAccess().doSearch(SearchSpace.gitProject, data.gitProject);
        if (searchResult == null) {
            throw new Http404NotFoundException("Could not find git project " + data.gitProject);
        }
        Team team = getDataAccess().getTeam(searchResult.teamId);
        if (team == null) {
            throw new RuntimeException("Could not find team associated to git project");
        }
        if (!team.getGitGroup().equals(data.gitGroup)) {
            throw new Http404NotFoundException("Could not find git group " + data.gitGroup + " for project " + data.gitProject);
        }
        messagingCoodinator.sendMessage(replaceTerms(pattern, data.data), team);
    }

    private void processByService(PostMessageData data, String pattern) {
        Service service = getService(data.serviceId, data.serviceName);
        Team team = getDataAccess().getTeam(service.getTeamId());
        
        messagingCoodinator.sendMessage(replaceTerms(pattern, data.data), team);
    }

    private String replaceTerms(String pattern, Map<String, String> data) {
        if (pattern == null || pattern.isEmpty()) {
            return "";
        }
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isEmpty() && entry.getValue() != null) {
                String target = "{"+entry.getKey()+"}";
                if (pattern.contains(target)) {
                    pattern = pattern.replace(target, entry.getValue());
                }
            }
        }
        return pattern;
    }
}
