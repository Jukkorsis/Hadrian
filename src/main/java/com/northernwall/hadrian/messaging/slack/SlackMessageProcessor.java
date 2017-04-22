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
package com.northernwall.hadrian.messaging.slack;

import com.google.gson.Gson;
import com.northernwall.hadrian.config.Const;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.messaging.MessageProcessor;
import com.northernwall.hadrian.parameters.Parameters;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlackMessageProcessor extends MessageProcessor {
    private final static Logger LOGGER = LoggerFactory.getLogger(SlackMessageProcessor.class);

    private Gson gson;
    private OkHttpClient client;
    private String slackUrl;
    private String slackUser;
    private String slackIcon;

    @Override
    public void init(Parameters parameters, Gson gson, OkHttpClient client) {
        this.gson = gson;
        this.client = client;
        slackUrl = parameters.getString("slackUrl", null);
        slackUser = parameters.getString("slackUser", "Hadrian");
        slackIcon = parameters.getString("slackIcon", " :rocket:");
    }

    @Override
    public void process(String text, Team team) {
        if (slackUrl == null || slackUrl.isEmpty()) {
            return;
        }
        if (text == null
                || text.isEmpty()
                || team == null
                || team.getTeamSlack() == null
                || team.getTeamSlack().isEmpty()) {
            return;
        }
        
        SlackMessage msg = new SlackMessage();
        msg.channel = team.getTeamSlack();
        msg.username = slackUser;
        msg.text = text;
        msg.icon_emoji = slackIcon;
        
        RequestBody body = RequestBody.create(Const.JSON_MEDIA_TYPE, gson.toJson(msg));
        
        Request request = new Request.Builder()
                .url(slackUrl)
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();
            LOGGER.info("{} {} {}", response.isSuccessful(), response.code(), response.body().string());
            response.body().close();
        } catch (IOException e) {
            LOGGER.error("Exception which contacting Slack", e);
        }
    }

}
