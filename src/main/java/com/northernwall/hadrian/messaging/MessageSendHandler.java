package com.northernwall.hadrian.messaging;

import com.northernwall.hadrian.messaging.dao.PostMessageData;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleRef;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.service.BasicHandler;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
            response.setStatus(200);
            request.setHandled(true);
            return;
        }

        Service service = getService(data.serviceId, data.serviceName, data.serviceAbbr);
        accessHelper.checkIfUserCanAudit(request, service.getTeamId());
        Module module = getModule(null, data.moduleName, service);
        Team team = getDataAccess().getTeam(service.getTeamId());

        data.data.put("serviceName", service.getServiceName());
        data.data.put("serviceAbbr", service.getServiceAbbr());
        data.data.put("moduleName", data.moduleName);
        data.data.put("teamName", team.getTeamName());

        Set<Team> teams = new HashSet<>();
        teams.add(team);

        if (messageType.includeUsedBy) {
            List<ModuleRef> refs = getDataAccess().getModuleRefsByServer(module.getServiceId(), module.getModuleId());
            for (ModuleRef ref : refs) {
                Service temp = getDataAccess().getService(ref.getClientServiceId());
                teams.add(getDataAccess().getTeam(temp.getTeamId()));
            }
        }

        for (Team temp : teams) {
            messagingCoodinator.sendMessage(messageType, temp, data.data);
        }

        response.setStatus(200);
        request.setHandled(true);
    }

}
