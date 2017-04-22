/*
 * Copyright 2016 Richard Thurston.
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
package com.northernwall.hadrian.messaging;

import com.google.gson.Gson;
import com.northernwall.hadrian.config.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.handlers.utility.HealthWriter;
import com.squareup.okhttp.OkHttpClient;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rthursto
 */
public class MessagingCoodinator {

    private final static Logger LOGGER = LoggerFactory.getLogger(MessagingCoodinator.class);

    private final DataAccess dataAccess;
    private final List<MessageProcessor> messageProcessors;

    public MessagingCoodinator(DataAccess dataAccess, Parameters parameters, OkHttpClient client, Gson gson) {
        this.dataAccess = dataAccess;
        messageProcessors = new LinkedList<>();

        String processors = parameters.getString(Const.MESSAGE_PROCESSORS, Const.MESSAGE_PROCESSORS_DEFAULT);
        if (processors != null && !processors.isEmpty()) {
            String[] parts = processors.split(",");
            for (String part : parts) {
                try {
                    Class c = Class.forName(part);
                    MessageProcessor processor = (MessageProcessor) c.newInstance();
                    processor.init(parameters, gson, client);
                    messageProcessors.add(processor);
                } catch (ClassNotFoundException ex) {
                    LOGGER.warn("Could not find MessageProcessor class {}", part);
                } catch (InstantiationException ex) {
                    LOGGER.warn("Could not instantiation MessageProcessor class {}", part);
                } catch (IllegalAccessException ex) {
                    LOGGER.warn("Could not access MessageProcessor class {}", part);
                }
            }
        }
    }

    public void sendMessage(String text, String teamId) {
        if (teamId == null || teamId.isEmpty()) {
            return;
        }
        Team team = dataAccess.getTeam(teamId);
        if (team == null) {
            return;
        }
        sendMessage(text, team);
    }

    public void sendMessage(String text, Team team) {
        for (MessageProcessor messageProcessor : messageProcessors) {
            messageProcessor.process(text, team);
        }
    }

    public void getHealth(HealthWriter writer) throws IOException {
        for (MessageProcessor messageProcessor : messageProcessors) {
            writer.addStringLine("MessageProcessor", messageProcessor.getClass().getCanonicalName());
        }
    }

}
