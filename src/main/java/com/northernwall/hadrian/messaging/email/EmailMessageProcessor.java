package com.northernwall.hadrian.messaging.email;

import com.google.gson.Gson;
import com.northernwall.hadrian.config.Const;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.messaging.MessageProcessor;
import com.northernwall.hadrian.parameters.Parameters;
import com.squareup.okhttp.OkHttpClient;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailMessageProcessor extends MessageProcessor {

    private final static Logger LOGGER = LoggerFactory.getLogger(EmailMessageProcessor.class);

    private String smtpHostname;
    private int smtpPort;
    private boolean smtpSsl;
    private DefaultAuthenticator authenticator = null;

    @Override
    public void init(Parameters parameters, Gson gson, OkHttpClient client) {
        smtpHostname = parameters.getString(Const.EMAIL_SMTP_HOSTNAME, null);
        smtpPort = parameters.getInt(Const.EMAIL_SMTP_POST, Const.EMAIL_SMTP_POST_DEFAULT);
        smtpSsl = parameters.getBoolean(Const.EMAIL_SMTP_SSL, Const.EMAIL_SMTP_SSL_DEFAULT);
        String smtpUsername = parameters.getString(Const.EMAIL_SMTP_USERNAME, null);
        String smtpPassword = parameters.getString(Const.EMAIL_SMTP_PASSWORD, null);
        if (smtpUsername != null && !smtpUsername.isEmpty() && smtpPassword != null && !smtpPassword.isEmpty()) {
            authenticator = new DefaultAuthenticator(smtpUsername, smtpPassword);
        }
    }

    @Override
    public void process(String text, Team team) {
        if (smtpHostname == null || smtpHostname.isEmpty()) {
            return;
        }
        if (text == null
                || text.isEmpty()
                || team == null
                || team.getTeamSlack() == null
                || team.getTeamSlack().isEmpty()) {
            return;
        }
        
        try {
            Email email = new SimpleEmail();
            if (smtpHostname != null) {
                email.setHostName(smtpHostname);
            }
            email.setSmtpPort(smtpPort);
            if (authenticator != null) {
                email.setAuthenticator(authenticator);
            }
            email.setSSLOnConnect(smtpSsl);
            email.setFrom(team.getTeamEmail());
            email.setSubject(text);
            email.setMsg(text);
            email.addTo(team.getTeamEmail());
            email.send();
        } catch (EmailException ex) {
            LOGGER.warn("Failure emailing work item, {}", ex.getMessage());
        }
    }

}
