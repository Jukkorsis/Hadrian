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
import com.northernwall.hadrian.utilityHandlers.HealthWriter;
import com.squareup.okhttp.OkHttpClient;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagingCoodinator implements ParameterChangeListener {

    private final static Logger logger = LoggerFactory.getLogger(MessagingCoodinator.class);

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
                    logger.warn("Could not find MessageProcessor class {}", part);
                } catch (InstantiationException ex) {
                    logger.warn("Could not instantiation MessageProcessor class {}", part);
                } catch (IllegalAccessException ex) {
                    logger.warn("Could not access MessageProcessor class {}", part);
                }
            }
        }

        parameters.registerChangeListener(this);
    }

    @Override
    public synchronized void onChange(List<String> keys) {
        messageTypes.clear();
        logger.info("Cache of MessageTypes has been cleared.");
    }

    public void sendMessage(MessageType messageType, Team team, Service service, Module module, Map<String, String> data) {
        logger.info("sendMessage {} to {} {} {}", messageType.name, team.getTeamName(), service.getServiceAbbr(), module.getModuleName());
        data.put("serviceName", service.getServiceName());
        data.put("serviceAbbr", service.getServiceAbbr());
        data.put("moduleName", module.getModuleName());
        data.put("teamName", team.getTeamName());

        Set<Team> teams = new HashSet<>();
        teams.add(team);

        if (messageType.includeUsedBy) {
            List<ModuleRef> refs = dataAccess.getModuleRefsByServer(module.getServiceId(), module.getModuleId());
            for (ModuleRef ref : refs) {
                Service tempService = dataAccess.getService(ref.getClientServiceId());
                Team tempTeam = dataAccess.getTeam(tempService.getTeamId());
                logger.info("also sending message {} to {}", messageType.name, tempTeam.getTeamName());
                teams.add(tempTeam);
            }
        }

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
            logger.warn("Could not find MessageType {}", messageTypeName);
            return null;
        }
        MessageType messageType = gson.fromJson(temp, MessageType.class);
        messageTypes.add(messageType);
        return messageType;
    }

    public void getHealth(HealthWriter writer) throws IOException {
        for (MessageProcessor messageProcessor : messageProcessors) {
            writer.addLine("MessageProcessor", messageProcessor.getClass().getCanonicalName());
        }
    }

}
