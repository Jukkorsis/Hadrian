/*
 * Copyright 2015 Richard Thurston.
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
package com.northernwall.hadrian.webhook.email;

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.webhook.WebHookSender;
import java.util.Properties;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailWebHookSender extends WebHookSender {

    private final static Logger logger = LoggerFactory.getLogger(EmailWebHookSender.class);

    private final String smtpHostname;
    private final int smtpPort;
    private final String smtpUsername;
    private final String smtpPassword;
    private final String emailTo;
    private final String emailFrom;

    public EmailWebHookSender(Properties properties) {
        super(properties);
        smtpHostname = properties.getProperty(Const.EMAIL_WEB_HOOK_SMTP_HOSTNAME, null);
        smtpPort = Integer.parseInt(properties.getProperty(Const.EMAIL_WEB_HOOK_SMTP_POST, Const.EMAIL_WEB_HOOK_SMTP_POST_DEFAULT));
        smtpUsername = properties.getProperty(Const.EMAIL_WEB_HOOK_SMTP_USERNAME, null);
        smtpPassword = properties.getProperty(Const.EMAIL_WEB_HOOK_SMTP_PASSWORD, null);
        emailTo = properties.getProperty(Const.EMAIL_WEB_HOOK_EMAIL_TO, null);
        emailFrom = properties.getProperty(Const.EMAIL_WEB_HOOK_EMAIL_From, emailTo);
        
        if (emailTo == null) {
            logger.warn("Property '{}' not set, so no emails will be sent", Const.EMAIL_WEB_HOOK_EMAIL_TO);
        }
    }

    @Override
    public void sendWorkItem(WorkItem workItem) {
        switch (workItem.getType()) {
            case Const.TYPE_SERVICE:
                sendServiceEmail(workItem);
                break;
            case Const.TYPE_HOST:
                sendHostEmail(workItem);
                break;
            case Const.TYPE_VIP:
                sendVipEmail(workItem);
                break;
            case Const.TYPE_HOST_VIP:
                sendHostVipEmail(workItem);
                break;
            default:
                logger.warn("Unknown workItem type {} with operation {}", workItem.getType(), workItem.getOperation());
        }
    }

    protected void sendServiceEmail(WorkItem workItem) {
        logger.info("Processing Service {} with opertion {}", workItem.getService().serviceName, workItem.getOperation());
        String subject = workItem.getOperation() + " service " + workItem.getService().serviceName;
        StringBuffer body = new StringBuffer();
        addLine("Type", workItem.getType(), body);
        addLine("Operation", workItem.getOperation(), body);
        addLine("Requestor", workItem.getUsername(), workItem.getFullname(), body);
        addLine("Service Abbr", workItem.getService().serviceAbbr, workItem.getService().serviceName, body);
        emailWorkItem(subject, body.toString());
    }

    protected void sendHostEmail(WorkItem workItem) {
        logger.info("Processing Host {} on {} with opertion {}", workItem.getHost().hostName, workItem.getService().serviceName, workItem.getOperation());
        String subject = workItem.getOperation() + " host " + workItem.getHost().hostName;
        StringBuffer body = new StringBuffer();
        addLine("Type", workItem.getType(), body);
        addLine("Operation", workItem.getOperation(), body);
        addLine("Requestor", workItem.getUsername(), workItem.getFullname(), body);
        addLine("Service Abbr", workItem.getService().serviceAbbr, workItem.getService().serviceName, body);
        emailWorkItem(subject, body.toString());
    }

    protected void sendVipEmail(WorkItem workItem) {
        logger.info("Processing Vip {} on {} with opertion {}", workItem.getVip().vipName, workItem.getService().serviceName, workItem.getOperation());
        String subject = workItem.getOperation() + " vip " + workItem.getVip().vipName;
        StringBuffer body = new StringBuffer();
        addLine("Type", workItem.getType(), body);
        addLine("Operation", workItem.getOperation(), body);
        addLine("Requestor", workItem.getUsername(), workItem.getFullname(), body);
        addLine("Service Abbr", workItem.getService().serviceAbbr, workItem.getService().serviceName, body);
        addLine("Name", workItem.getVip().vipName, body);
        addLine("DNS", workItem.getVip().dns, body);
        addLine("Domain", workItem.getVip().domain, body);
        addLine("Network", workItem.getVip().network, body);
        addLine("Protocol", workItem.getVip().protocol, body);
        emailWorkItem(subject, body.toString());
    }

    protected void sendHostVipEmail(WorkItem workItem) {
        logger.info("Processing Host Vip {} {} on {} with opertion {}", workItem.getHost().hostName, workItem.getVip().vipName, workItem.getService().serviceName, workItem.getOperation());
        String subject;
        switch (workItem.getOperation()) {
            case Const.OPERATION_CREATE:
                subject = "add Host to Vip";
                break;
            case Const.OPERATION_DELETE:
                subject = "remove Host from Vip";
                break;
            default:
                logger.warn("Unknown workItem operation {} for host {}", workItem.getOperation(), workItem.getHost().hostName);
                return;
        }
        StringBuffer body = new StringBuffer();
        addLine("Type", workItem.getType(), body);
        addLine("Operation", workItem.getOperation(), body);
        addLine("Requestor", workItem.getUsername(), workItem.getFullname(), body);
        addLine("Service Abbr", workItem.getService().serviceAbbr, workItem.getService().serviceName, body);
        emailWorkItem(subject, body.toString());
    }

    private void addLine(String label, String value, StringBuffer body) {
        body.append(label);
        body.append(": ");
        body.append(value);
        body.append("\n");
    }

    private void addLine(String label, String shortValue, String longValue, StringBuffer body) {
        body.append(label);
        body.append(": (");
        body.append(shortValue);
        body.append(") ");
        body.append(longValue);
        body.append("\n");
    }

    private void emailWorkItem(String subject, String body) {
        try {
            if (emailTo == null) {
                return;
            }
            Email email = new SimpleEmail();
            if (smtpHostname != null) {
                email.setHostName(smtpHostname);
            }
            email.setSmtpPort(smtpPort);
            if (smtpUsername != null && smtpPassword != null) {
                email.setAuthenticator(new DefaultAuthenticator(smtpUsername, smtpPassword));
            }
            email.setSSLOnConnect(true);
            email.setFrom(emailFrom);
            email.setSubject(subject);
            email.setMsg(body);
            email.addTo(emailTo);
            email.send();

            logger.info("Emailing work item to {}  with {} -> {}", emailTo, subject, body);
        } catch (EmailException ex) {
            throw new RuntimeException("Failure emailing work item, {}", ex);
        }
    }

}
