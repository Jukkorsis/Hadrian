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

/**
 *
 * @author Richard Thurston
 */
public class TreeHandler extends AbstractHandler {

    private final DataAccess dataAccess;
    private final Gson gson;

    public TreeHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        gson = new Gson();
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        User user = (User) request.getAttribute(Const.ATTR_USER);
        response.setContentType(Const.JSON);
        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            jw.beginArray();
            listDevTeams(jw);
            listGraph(jw);
            if (user.isAdmin()) {
                listAdminTeam(jw);
            }
            listHelp(jw);
            jw.endArray();
        }
        response.setStatus(200);
        request.setHandled(true);
    }

    private void listDevTeams(final JsonWriter jw) throws JsonIOException {
        TreeNode devTeamsTreenode = new TreeNode();
        devTeamsTreenode.setLabel("Dev Teams");
        devTeamsTreenode.setData(new TreeNodeData("-1", "DevTeams"));
        List<Team> teams = dataAccess.getTeams();
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
        graphTreenode.setData(new TreeNodeData("-2", "Graph"));
        gson.toJson(graphTreenode, TreeNode.class, jw);
    }

    private void listAdminTeam(final JsonWriter jw) throws JsonIOException {
        TreeNode adminTreenode = new TreeNode();
        adminTreenode.setLabel("Admin");
        adminTreenode.setData(new TreeNodeData("-9", "Admin"));

        TreeNode usersTreenode = new TreeNode();
        usersTreenode.setLabel("Users");
        usersTreenode.setData(new TreeNodeData("-5", "Users"));
        adminTreenode.getChildren().add(usersTreenode);

        TreeNode workItemsTreenode = new TreeNode();
        workItemsTreenode.setLabel("Work Items");
        workItemsTreenode.setData(new TreeNodeData("-6", "WorkItems"));
        adminTreenode.getChildren().add(workItemsTreenode);

        TreeNode optionsTreenode = new TreeNode();
        optionsTreenode.setLabel("Parameters");
        optionsTreenode.setData(new TreeNodeData("-8", "Parameters"));
        adminTreenode.getChildren().add(optionsTreenode);

        gson.toJson(adminTreenode, TreeNode.class, jw);
    }

    private void listHelp(final JsonWriter jw) throws JsonIOException {
        TreeNode adminTreenode = new TreeNode();
        adminTreenode.setLabel("Help");
        adminTreenode.setData(new TreeNodeData("-10", "Help"));
        gson.toJson(adminTreenode, TreeNode.class, jw);
    }

}
