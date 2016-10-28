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
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleRef;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.parameters.ParameterChangeListener;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.handlers.utility.HealthWriter;
import com.squareup.okhttp.OkHttpClient;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rthursto
 */
public class MessagingCoodinator implements ParameterChangeListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(MessagingCoodinator.class);

    private final DataAccess dataAccess;
    private final Parameters parameters;
    private final List<MessageProcessor> messageProcessors;
    private final List<MessageType> messageTypes;
    private final Gson gson;

    public MessagingCoodinator(DataAccess dataAccess, Parameters parameters, OkHttpClient client) {
        this.dataAccess = dataAccess;
        this.parameters = parameters;
        messageProcessors = new LinkedList<>();
        messageTypes = new LinkedList<>();
        gson = new Gson();

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

        parameters.registerChangeListener(this);
    }

    @Override
    public synchronized void onChange(List<String> keys) {
        messageTypes.clear();
        LOGGER.info("Cache of MessageTypes has been cleared.");
    }

    public void sendMessage(MessageType messageType, Team team, Service service, Module module, Map<String, String> data) {
        LOGGER.info("sendMessage {} to {} {} {}", messageType.name, team.getTeamName(), service.getServiceName(), module.getModuleName());
        data.put("serviceName", service.getServiceName());
        data.put("moduleName", module.getModuleName());
        data.put("teamName", team.getTeamName());

        Set<Team> teams = new HashSet<>();
        teams.add(team);

        if (messageType.includeUsedBy) {
            processModuleRefs(module, messageType, teams);
        }

        processEachTeam(teams, messageType, data);
    }

    public void sendMessage(MessageType messageType, Team team, Service service, Map<String, String> data) {
        LOGGER.info("sendMessage {} to {} {}", messageType.name, team.getTeamName(), service.getServiceName());
        data.put("serviceName", service.getServiceName());
        data.put("teamName", team.getTeamName());

        Set<Team> teams = new HashSet<>();
        teams.add(team);

        String moduleNames = "(no modules)";
        if (messageType.includeUsedBy) {
            List<Module> modules = dataAccess.getModules(service.getServiceId());
            for (int i=0; i<modules.size()-1; i++) {
                Module module = modules.get(i);
                if (i == 0) {
                    moduleNames = module.getModuleName();
                } else if (i == modules.size()-1) {
                    if (i == 1) {
                        moduleNames = moduleNames + " and " + module.getModuleName();
                    } else {
                        moduleNames = moduleNames + ", and " + module.getModuleName();
                    }
                } else {
                    moduleNames = moduleNames + ", " + module.getModuleName();
                }
                processModuleRefs(module, messageType, teams);
            }
        }
        data.put("moduleName", moduleNames);
        
        processEachTeam(teams, messageType, data);
    }

    private void processModuleRefs(Module module, MessageType messageType, Set<Team> teams) {
        List<ModuleRef> refs = dataAccess.getModuleRefsByServer(module.getServiceId(), module.getModuleId());
        for (ModuleRef ref : refs) {
            Service tempService = dataAccess.getService(ref.getClientServiceId());
            Team tempTeam = dataAccess.getTeam(tempService.getTeamId());
            LOGGER.info("also sending message {} to {}", messageType.name, tempTeam.getTeamName());
            teams.add(tempTeam);
        }
    }

    private void processEachTeam(Set<Team> teams, MessageType messageType, Map<String, String> data) {
        for (Team tempTeam : teams) {
            for (MessageProcessor messageProcessor : messageProcessors) {
                messageProcessor.process(messageType, tempTeam, data);
            }
        }
    }

    public synchronized MessageType getMessageType(String messageTypeName) {
        for (MessageType messageType : messageTypes) {
            if (messageType.name.equalsIgnoreCase(messageTypeName)) {
                return messageType;
            }
        }
        String temp = parameters.getString("messageType." + messageTypeName, null);
        if (temp == null) {
            LOGGER.warn("Could not find MessageType {}", messageTypeName);
            return null;
        }
        MessageType messageType = gson.fromJson(temp, MessageType.class);
        messageTypes.add(messageType);
        return messageType;
    }

    public void getHealth(HealthWriter writer) throws IOException {
        for (MessageProcessor messageProcessor : messageProcessors) {
            writer.addStringLine("MessageProcessor", messageProcessor.getClass().getCanonicalName());
        }
    }

}
