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
package com.northernwall.hadrian.tree;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.tree.dao.TreeNode;
import com.northernwall.hadrian.tree.dao.TreeNodeData;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
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
public class TreeHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(TreeHandler.class);

    private final DataAccess dataAccess;
    private final Gson gson;

    public TreeHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        gson = new Gson();
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.equals("/v1/tree")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                switch (request.getMethod()) {
                    case "GET":
                        listComponents(request, response);
                        break;
                    default:
                        throw new RuntimeException("Unknown tree operation");
                }
                response.setStatus(200);
                request.setHandled(true);
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void listComponents(Request request, HttpServletResponse response) throws IOException {
        User user = (User) request.getAttribute(Const.ATTR_USER);
        response.setContentType(Const.JSON);
        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            jw.beginArray();
            listDevTeams(jw);
            listGraph(jw);
            listPortal(jw);
            if (user.isOps()) {
                listOpsTeam(jw);
            }
            if (user.isAdmin()) {
                listAdminTeam(jw);
            }
            listHelp(jw);
            jw.endArray();
        }
    }

    private void listDevTeams(final JsonWriter jw) throws JsonIOException {
        TreeNode devTeamsTreenode = new TreeNode();
        devTeamsTreenode.setLabel("Dev Teams");
        devTeamsTreenode.setData(new TreeNodeData("0", "DevTeams"));
        List<Team> teams  = dataAccess.getTeams();
        Collections.sort(teams);
        for (Team team : teams) {
            TreeNode teamTreenode = new TreeNode();
            teamTreenode.setLabel(team.getTeamName());
            teamTreenode.setData(new TreeNodeData(team.getTeamId(), "Team"));
            List<Service> services = dataAccess.getServices(team.getTeamId());
            Collections.sort(services);
            for (Service service : services) {
                TreeNode serviceTreeNode = new TreeNode();
                serviceTreeNode.setLabel(service.getServiceName());
                serviceTreeNode.setData(new TreeNodeData(service.getServiceId(), "Service"));
                teamTreenode.getChildren().add(serviceTreeNode);
            }
            devTeamsTreenode.getChildren().add(teamTreenode);
        }
        gson.toJson(devTeamsTreenode, TreeNode.class, jw);
    }

    private void listGraph(final JsonWriter jw) throws JsonIOException {
        TreeNode graphTreenode = new TreeNode();
        graphTreenode.setLabel("Graph");
        graphTreenode.setData(new TreeNodeData("0", "Graph"));
        gson.toJson(graphTreenode, TreeNode.class, jw);
    }

    private void listPortal(final JsonWriter jw) throws JsonIOException {
        TreeNode graphTreenode = new TreeNode();
        graphTreenode.setLabel("Portal");
        graphTreenode.setData(new TreeNodeData("0", "Portal"));
        gson.toJson(graphTreenode, TreeNode.class, jw);
    }

    private void listOpsTeam(final JsonWriter jw) throws JsonIOException {
        TreeNode opsTeamTreenode = new TreeNode();
        opsTeamTreenode.setLabel("Ops Team");
        opsTeamTreenode.setData(new TreeNodeData("0", "OpsTeam"));
        
        TreeNode optionsTreenode = new TreeNode();
        optionsTreenode.setLabel("Parameters");
        optionsTreenode.setData(new TreeNodeData("0", "Parameters"));
        opsTeamTreenode.getChildren().add(optionsTreenode);
        
        TreeNode tasksTreenode = new TreeNode();
        tasksTreenode.setLabel("Tasks");
        tasksTreenode.setData(new TreeNodeData("0", "Tasks"));
        opsTeamTreenode.getChildren().add(tasksTreenode);
        
        TreeNode webhooksTreenode = new TreeNode();
        webhooksTreenode.setLabel("Webhooks");
        webhooksTreenode.setData(new TreeNodeData("0", "Webhooks"));
        opsTeamTreenode.getChildren().add(webhooksTreenode);
        
        TreeNode backfillTreenode = new TreeNode();
        backfillTreenode.setLabel("Backfill");
        backfillTreenode.setData(new TreeNodeData("0", "Backfill"));
        opsTeamTreenode.getChildren().add(backfillTreenode);
        
        gson.toJson(opsTeamTreenode, TreeNode.class, jw);
    }

    private void listAdminTeam(final JsonWriter jw) throws JsonIOException {
        TreeNode adminTreenode = new TreeNode();
        adminTreenode.setLabel("Admin");
        adminTreenode.setData(new TreeNodeData("0", "Admin"));
        gson.toJson(adminTreenode, TreeNode.class, jw);
    }

    private void listHelp(final JsonWriter jw) throws JsonIOException {
        TreeNode adminTreenode = new TreeNode();
        adminTreenode.setLabel("Help");
        adminTreenode.setData(new TreeNodeData("0", "Help"));
        gson.toJson(adminTreenode, TreeNode.class, jw);
    }

}
