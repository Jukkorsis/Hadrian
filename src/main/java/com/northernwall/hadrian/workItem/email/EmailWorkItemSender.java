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
package com.northernwall.hadrian.workItem.email;

import com.codahale.metrics.MetricRegistry;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.workItem.WorkItemSender;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailWorkItemSender extends WorkItemSender {

    private final static Logger logger = LoggerFactory.getLogger(EmailWorkItemSender.class);

    private final String smtpHostname;
    private final int smtpPort;
    private final boolean smtpSsl;
    private final String smtpUsername;
    private final String smtpPassword;
    private final List<String> emailTos;
    private final String emailFrom;

    public EmailWorkItemSender(Parameters parameters, MetricRegistry metricRegistry) {
        super(parameters);
        smtpHostname = parameters.getString(Const.EMAIL_WORK_ITEM_SMTP_HOSTNAME, null);
        smtpPort = parameters.getInt(Const.EMAIL_WORK_ITEM_SMTP_POST, Const.EMAIL_WORK_ITEM_SMTP_POST_DEFAULT);
        smtpSsl = parameters.getBoolean(Const.EMAIL_WORK_ITEM_SMTP_SSL, Const.EMAIL_WORK_ITEM_SMTP_SSL_DEFAULT);
        smtpUsername = parameters.getString(Const.EMAIL_WORK_ITEM_SMTP_USERNAME, null);
        smtpPassword = parameters.getString(Const.EMAIL_WORK_ITEM_SMTP_PASSWORD, null);

        String temp = parameters.getString(Const.EMAIL_WORK_ITEM_EMAIL_TO, null);
        emailTos = new LinkedList<>();
        if (temp == null) {
            logger.warn("Property '{}' not set, so no emails will be sent", Const.EMAIL_WORK_ITEM_EMAIL_TO);
        } else {
            String[] parts = temp.split(",");
            for (String part : parts) {
                if (part != null && !part.isEmpty()) {
                    emailTos.add(part.trim());
                }
            }
        }

        String fromDefault = null;
        if (!emailTos.isEmpty()) {
            fromDefault = emailTos.get(0);
        }
        emailFrom = parameters.getString(Const.EMAIL_WORK_ITEM_EMAIL_From, fromDefault);
    }

    @Override
    public boolean sendWorkItem(WorkItem workItem) {
        switch (workItem.getType()) {
            case Const.TYPE_MODULE:
                sendModuleEmail(workItem);
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
        return false;
    }

    protected void sendModuleEmail(WorkItem workItem) {
        logger.info("Processing Module {} on {} with opertion {}", workItem.getMainModule().moduleName, workItem.getService().serviceName, workItem.getOperation());

        String subject = workItem.getOperation() + " module " + workItem.getMainModule().moduleName;

        StringBuffer body = new StringBuffer();
        addEmailHeader(workItem, body);
        body.append("\n");
        addLine("Module Name", workItem.getMainModule().moduleName, body);
        addLine("Module Type", workItem.getMainModule().moduleType, body);
        body.append("\n");
        addLine("Git URL", getGitUrl(workItem), body);
        addLine("Git Folder", workItem.getMainModule().gitFolder, body);
        addLine("Maven Group", workItem.getMainModule().mavenGroupId, body);
        addLine("Maven Artifact ID", workItem.getMainModule().mavenArtifactId, body);
        addLine("Artifact Type", workItem.getMainModule().artifactType, body);
        addLine("Artifact Suffix", workItem.getMainModule().artifactSuffix, body);

        emailWorkItem(subject, body.toString());
    }
    
    protected void sendHostEmail(WorkItem workItem) {
        logger.info("Processing Host {} on {} with opertion {}", workItem.getHost().hostName, workItem.getService().serviceName, workItem.getOperation());

        String subject = workItem.getOperation() + " host " + workItem.getHost().hostName;

        StringBuffer body = new StringBuffer();
        addEmailHeader(workItem, body);
        body.append("\n");
        addLine("Host Name", workItem.getHost().hostName, body);
        addLine("Data Center", workItem.getHost().dataCenter, body);
        addLine("Network", workItem.getHost().network, body);
        addLine("Environment", workItem.getHost().env, body);
        addLine("Size", workItem.getHost().size, body);
        addLine("Version", workItem.getHost().version, body);

        emailWorkItem(subject, body.toString());
    }

    protected void sendVipEmail(WorkItem workItem) {
        logger.info("Processing Vip {} on {} with opertion {}", workItem.getVip().vipName, workItem.getService().serviceName, workItem.getOperation());

        String subject = workItem.getOperation() + " vip " + workItem.getVip().vipName;

        StringBuffer body = new StringBuffer();
        addEmailHeader(workItem, body);
        body.append("\n");
        addLine("VIP Name", workItem.getVip().vipName, body);
        addLine("DNS", workItem.getVip().dns + "." + workItem.getVip().domain, body);
        addLine("Network", workItem.getVip().network, body);
        addLine("Public", Boolean.toString(workItem.getVip().external), body);
        addLine("Protocol", workItem.getVip().protocol, body);
        addLine("VIP Port", Integer.toString(workItem.getVip().vipPort), body);
        addLine("Service Port", Integer.toString(workItem.getVip().servicePort), body);

        emailWorkItem(subject, body.toString());
    }

    protected void sendHostVipEmail(WorkItem workItem) {
        logger.info("Processing Host Vip {} {} on {} with opertion {}", workItem.getHost().hostName, workItem.getVip().vipName, workItem.getService().serviceName, workItem.getOperation());

        String subject;
        switch (workItem.getOperation()) {
            case Const.OPERATION_CREATE:
                subject = "Add Host to Vip";
                break;
            case Const.OPERATION_DELETE:
                subject = "Remove Host from Vip";
                break;
            default:
                logger.warn("Unknown workItem operation {} for host {}", workItem.getOperation(), workItem.getHost().hostName);
                return;
        }

        StringBuffer body = new StringBuffer();
        addEmailHeader(workItem, body);
        body.append("\n");
        addLine("Host Name", workItem.getHost().hostName, body);
        addLine("Data Center", workItem.getHost().dataCenter, body);
        addLine("Network", workItem.getHost().network, body);
        body.append("\n");
        addLine("VIP Name", workItem.getVip().vipName, body);
        addLine("DNS", workItem.getVip().dns + "." + workItem.getVip().domain, body);

        emailWorkItem(subject, body.toString());
    }

    private void addEmailHeader(WorkItem workItem, StringBuffer body) {
        addLine("Type", workItem.getType(), body);
        addLine("Operation", workItem.getOperation(), body);
        addLine("Requestor", workItem.getUsername(), workItem.getFullname(), body);
        addLine("Team", workItem.getTeam().teamName, body);
        body.append("\n");
        addLine("Service Abbr", workItem.getService().serviceAbbr, workItem.getService().serviceName, body);
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
            if (emailTos.isEmpty()) {
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
            email.setSSLOnConnect(smtpSsl);
            email.setFrom(emailFrom);
            email.setSubject(subject);
            email.setMsg(body);
            for (String emailTo : emailTos) {
                email.addTo(emailTo);
            }
            email.send();

            if (emailTos.size() == 1) {
                logger.info("Emailing work item to {} with subject {}", emailTos.get(0), subject);
            } else {
                logger.info("Emailing work item to {} and {} other email addresses with subject {}", emailTos.get(0), (emailTos.size() - 1), subject);
            }
        } catch (EmailException ex) {
            throw new RuntimeException("Failure emailing work item, {}", ex);
        }
    }

}
