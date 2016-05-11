package com.northernwall.hadrian.service;

import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.messaging.MessagingCoodinator;
import com.northernwall.hadrian.service.dao.PostMessageData;
import java.io.IOException;
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

        Service service = getService(data.serviceId, data.serviceName, data.serviceAbbr);
        accessHelper.checkIfUserCanAudit(request, service.getTeamId());
        Team team = getDataAccess().getTeam(service.getTeamId());
        
        data.data.put("serviceName", service.getServiceName());
        data.data.put("serviceAbbr", service.getServiceAbbr());
        data.data.put("moduleName", data.moduleName);
        data.data.put("hostName", data.hostName);
        data.data.put("teamName", team.getTeamName());
        messagingCoodinator.sendMessage(data.messageTypeName, team, data.data);
    }

}
