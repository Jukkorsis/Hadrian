package com.northernwall.hadrian.messaging;

import com.northernwall.hadrian.messaging.dao.PostMessageData;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.service.BasicHandler;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http400BadRequestException;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

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
            List<Service> services = getDataAccess().getActiveServices();
            for (Team team : getDataAccess().getTeams()) {
                if (team.getGitGroup().equalsIgnoreCase(data.gitGroup)) {
                    accessHelper.checkIfUserCanAudit(request, team);
                    List<Service> teamServices = Service.filterTeam(team.getTeamId(), services);
                    for (Service service : teamServices) {
                        for (Module module : getDataAccess().getModules(service.getServiceId())) {
                            if (module.getGitProject().equalsIgnoreCase(data.gitProject)) {
                                messagingCoodinator.sendMessage(messageType, team, service, module, data.data);
                            }
                        }
                    }
                }
            }
        } else {
            Service service = getService(data.serviceId, data.serviceName);
            Team team = getDataAccess().getTeam(service.getTeamId());
            accessHelper.checkIfUserCanAudit(request, team);
            Module module = getModule(null, data.moduleName, service);

            messagingCoodinator.sendMessage(messageType, team, service, module, data.data);
        }

        response.setStatus(200);
        request.setHandled(true);
    }

}
