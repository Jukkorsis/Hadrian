package com.northernwall.hadrian.service;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.access.Access;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.service.dao.GetTeamData;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeamHandler extends AbstractHandler {
    private final static Logger logger = LoggerFactory.getLogger(TeamHandler.class);

    private final Access access;
    private final DataAccess dataAccess;
    private final Gson gson;

    public TeamHandler(Access access, DataAccess dataAccess) {
        this.access = access;
        this.dataAccess = dataAccess;
        gson = new Gson();
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.startsWith("/v1/team/")) {
                switch (request.getMethod()) {
                    case "GET":
                        if (target.matches("/v1/team/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            getTeam(request, response, target.substring(9, target.length()));
                            response.setStatus(200);
                            request.setHandled(true);
                        }
                        break;
                }
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void getTeam(Request request, HttpServletResponse response, String id) throws IOException {
        response.setContentType(Const.JSON);
        Team team = dataAccess.getTeam(id);
        if (team == null) {
            throw new RuntimeException("Could not find team with id '" + id + "'");
        }

        GetTeamData getTeamData = GetTeamData.create(team);
        getTeamData.canModify = access.canUserModify(request, id);
        
        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            gson.toJson(getTeamData, GetTeamData.class, jw);
        }
    }

}
