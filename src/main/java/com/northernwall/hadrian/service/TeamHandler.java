package com.northernwall.hadrian.service;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.Util;
import com.northernwall.hadrian.access.Access;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.service.dao.GetTeamData;
import com.northernwall.hadrian.service.dao.PostTeamData;
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
            if (target.startsWith("/v1/team")) {
                switch (request.getMethod()) {
                    case Const.HTTP_GET:
                        if (target.matches("/v1/team/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            getTeam(request, response, target.substring(9, target.length()));
                            response.setStatus(200);
                            request.setHandled(true);
                        }
                        break;
                    case Const.HTTP_POST:
                        if (target.equals("/v1/team")) {
                            createTeam(request);
                            response.setStatus(200);
                            request.setHandled(true);
                        }
                        break;
                    case Const.HTTP_PUT:
                        if (target.matches("/v1/team/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            String temp = target.substring(9, target.length());
                            int i = temp.indexOf("/");
                            addUserToTeam(request, temp.substring(0, i), temp.substring(i + 1));
                            response.setStatus(200);
                            request.setHandled(true);
                        }
                        break;
                    case Const.HTTP_DELETE:
                        if (target.matches("/v1/team/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            String temp = target.substring(9, target.length());
                            int i = temp.indexOf("/");
                            removeUserFromTeam(request, temp.substring(0, i), temp.substring(i + 1));
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

    private void createTeam(Request request) throws IOException {
        access.checkIfUserIsAdmin(request, "create team");
        
        PostTeamData postTeamData = Util.fromJson(request, PostTeamData.class);

        if (postTeamData.user == null) {
            throw new RuntimeException("Failed to create new team, as user is null");
        }
        if (postTeamData.name == null) {
            throw new RuntimeException("Failed to create new team, as team name is null");
        }
        postTeamData.name = postTeamData.name.trim();
        if (postTeamData.name.isEmpty()) {
            throw new RuntimeException("Failed to create new team, as team name is empty");
        }
        for (Team temp : dataAccess.getTeams()) {
            if (temp.getTeamName().equals(postTeamData.name)) {
                throw new RuntimeException("Failed to create new team, as team with name " + postTeamData.name + " already exists");
            }
        }
        Team team = new Team(postTeamData.name);
        if (dataAccess.getUser(postTeamData.user.getUsername()) == null) {
            throw new RuntimeException("Failed to create new team, could not find initial user " + postTeamData.user.getUsername());
        }
        team.getUsernames().add(postTeamData.user.getUsername());
        dataAccess.saveTeam(team);
    }

    private void addUserToTeam(Request request, String teamId, String username) {
        access.checkIfUserCanModify(request, teamId, "add user to team");
        if (dataAccess.getUser(username) == null) {
            throw new RuntimeException("Failed to add user " + username + " to team " + teamId + ", could not find user");
        }
        Team team = dataAccess.getTeam(teamId);
        if (team == null) {
            throw new RuntimeException("Failed to add user " + username + " to team " + teamId + ", could not find team");
        }
        team.getUsernames().add(username);
        dataAccess.updateTeam(team);
    }

    private void removeUserFromTeam(Request request, String teamId, String username) {
        access.checkIfUserCanModify(request, teamId, "add user to team");
        Team team = dataAccess.getTeam(teamId);
        if (team == null) {
            throw new RuntimeException("Failed to add user " + username + " to team " + teamId + ", could not find team");
        }
        if (team.getUsernames().size() < 2) {
            throw new RuntimeException("Can not remove the last user from team " + team.getTeamName());
        }
        team.getUsernames().remove(username);
        dataAccess.updateTeam(team);
    }

}
