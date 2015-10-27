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
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.tree.dao.TreeNode;
import com.northernwall.hadrian.tree.dao.TreeNodeData;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
                        listComponents(response);
                        break;
                }
                response.setStatus(200);
                request.setHandled(true);
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void listComponents(HttpServletResponse response) throws IOException {
        response.setContentType(Const.JSON);
        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            jw.beginArray();
            for (Team team : dataAccess.getTeams()) {
                TreeNode teamTreenode = new TreeNode();
                teamTreenode.setLabel(team.getTeamName());
                teamTreenode.setData(new TreeNodeData(team.getTeamId(), "Team"));
                for (Service service : dataAccess.getServices(team.getTeamId())) {
                    TreeNode serviceTreeNode = new TreeNode();
                    serviceTreeNode.setLabel(service.getServiceName());
                    serviceTreeNode.setData(new TreeNodeData(service.getServiceId(), "Service"));
                    teamTreenode.getChildren().add(serviceTreeNode);
                }
                gson.toJson(teamTreenode, TreeNode.class, jw);
            }
            jw.endArray();
        }
    }

}
