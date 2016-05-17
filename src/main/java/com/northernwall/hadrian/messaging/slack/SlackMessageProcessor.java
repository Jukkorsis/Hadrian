package com.northernwall.hadrian.messaging.slack;

import com.google.gson.Gson;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.messaging.MessageProcessor;
import com.northernwall.hadrian.messaging.MessageType;
import com.northernwall.hadrian.parameters.Parameters;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlackMessageProcessor extends MessageProcessor {
    private final static Logger logger = LoggerFactory.getLogger(SlackMessageProcessor.class);

    private Gson gson;
    private OkHttpClient client;
    private String slackUrl;
    private String slackUser;

    @Override
    public void init(Parameters parameters, Gson gson, OkHttpClient client) {
        this.gson = gson;
        this.client = client;
        slackUrl = parameters.getString("slackUrl", null);
        slackUser = parameters.getString("slackUser", "Hadrian");
    }

    @Override
    public void process(MessageType messageType, Team team, Map<String, String> data) {
        if (messageType == null
                || messageType.slackBody == null
                || messageType.slackBody.isEmpty()
                || slackUrl == null
                || slackUrl.isEmpty()) {
            return;
        }
        
        SlackMessage msg = new SlackMessage();
        msg.channel = team.getTeamSlack();
        msg.username = slackUser;
        msg.text = replaceTerms(messageType.slackBody, data);
        msg.icon_emoji = messageType.slackIcon;
        
        RequestBody body = RequestBody.create(Const.JSON_MEDIA_TYPE, gson.toJson(msg));
        
        Request request = new Request.Builder()
                .url(slackUrl)
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();
            logger.info("{} {} {}", response.isSuccessful(), response.code(), response.body().string());
        } catch (IOException e) {
            logger.error("Exception which contacting Slack", e);
        }
    }

}
