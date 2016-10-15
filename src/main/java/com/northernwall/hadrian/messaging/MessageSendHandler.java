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

import com.northernwall.hadrian.messaging.dao.PostMessageData;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.handlers.utility.routingHandler.Http400BadRequestException;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author rthursto
 */
public class MessageSendHandler extends BasicHandler {

    private final AccessHelper accessHelper;
    private final MessagingCoodinator messagingCoodinator;

    public MessageSendHandler(DataAccess dataAccess, AccessHelper accessHelper, MessagingCoodinator messagingCoodinator) {
        super(dataAccess);
        this.accessHelper = accessHelper;
        this.messagingCoodinator = messagingCoodinator;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostMessageData data = fromJson(request, PostMessageData.class);

        MessageType messageType = messagingCoodinator.getMessageType(data.messageTypeName);
        if (messageType == null) {
            throw new Http400BadRequestException("Could not find MessageType " + data.messageTypeName);
        }

        if (data.gitGroup != null
                && !data.gitGroup.isEmpty()
                && data.gitProject != null
                && !data.gitProject.isEmpty()) {
            processByGit(data, request, messageType);
        } else {
            processByService(data, request, messageType);
        }

        response.setStatus(200);
        request.setHandled(true);
    }

    private void processByGit(PostMessageData data, Request request, MessageType messageType) {
        List<Service> services = getDataAccess().getActiveServices();
        for (Team team : getDataAccess().getTeams()) {
            if (team.getGitGroup().equalsIgnoreCase(data.gitGroup)) {
                accessHelper.checkIfUserCanAudit(request, team);
                List<Service> teamServices = Service.filterTeam(team.getTeamId(), services);
                for (Service service : teamServices) {
                    if (service.getGitProject().equalsIgnoreCase(data.gitProject)) {
                        messagingCoodinator.sendMessage(messageType, team, service, data.data);
                        return;
                    }
                }
            }
        }
    }

    private void processByService(PostMessageData data, Request request, MessageType messageType) {
        Service service = getService(data.serviceId, data.serviceName);
        Team team = getDataAccess().getTeam(service.getTeamId());
        accessHelper.checkIfUserCanAudit(request, team);
        Module module = getModule(null, data.moduleName, service);
        
        messagingCoodinator.sendMessage(messageType, team, service, module, data.data);
    }

}
