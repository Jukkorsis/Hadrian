package com.northernwall.hadrian.messaging.email;

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.messaging.MessageProcessor;
import com.northernwall.hadrian.messaging.MessageType;
import com.northernwall.hadrian.parameters.Parameters;
import java.util.Map;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

public class EmailMessageProcessor extends MessageProcessor {

    private String smtpHostname;
    private int smtpPort;
    private boolean smtpSsl;
    private DefaultAuthenticator authenticator = null;

    @Override
    public void init(Parameters parameters) {
        smtpHostname = parameters.getString(Const.EMAIL_WORK_ITEM_SMTP_HOSTNAME, null);
        smtpPort = parameters.getInt(Const.EMAIL_WORK_ITEM_SMTP_POST, Const.EMAIL_WORK_ITEM_SMTP_POST_DEFAULT);
        smtpSsl = parameters.getBoolean(Const.EMAIL_WORK_ITEM_SMTP_SSL, Const.EMAIL_WORK_ITEM_SMTP_SSL_DEFAULT);
        String smtpUsername = parameters.getString(Const.EMAIL_WORK_ITEM_SMTP_USERNAME, null);
        String smtpPassword = parameters.getString(Const.EMAIL_WORK_ITEM_SMTP_PASSWORD, null);
        if (smtpUsername != null && !smtpUsername.isEmpty() && smtpPassword != null && !smtpPassword.isEmpty()) {
            authenticator = new DefaultAuthenticator(smtpUsername, smtpPassword);
        }
    }

    @Override
    public void process(MessageType messageType, Team team, Map<String, String> data) {
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
            email.setSubject(replaceTerms(messageType.emailSubject, data));
            email.setMsg(replaceTerms(messageType.emailBody, data));
            email.addTo(team.getTeamEmail());
            email.send();
        } catch (EmailException ex) {
            throw new RuntimeException("Failure emailing work item, {}", ex);
        }
    }

}
