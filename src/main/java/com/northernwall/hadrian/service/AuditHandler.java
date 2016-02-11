package com.northernwall.hadrian.service;

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.Util;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Audit;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.service.dao.PostAudit;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(AuditHandler.class);
    
    private final DataAccess dataAccess;

    public AuditHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.startsWith("/v1/audit") && request.getMethod().equals(Const.HTTP_POST)) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                createAudit(request);
                response.setStatus(200);
                request.setHandled(true);
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void createAudit(Request request) throws IOException {
        PostAudit postAudit = Util.fromJson(request, PostAudit.class);
        
        Service service = findService(postAudit);
        if (service == null) {
            return;
        }

        Audit audit = new Audit();
        audit.serviceId = service.getServiceId();
        audit.timePerformed = Util.getGmt();
        audit.timeRequested = Util.getGmt();
        audit.requestor = postAudit.username;
        audit.type = postAudit.type;
        audit.operation = postAudit.operation;
        if (postAudit.hostName != null) {
            audit.hostName = postAudit.hostName;
        }
        if (postAudit.vipName != null) {
            audit.vipName = postAudit.vipName;
        }
        audit.notes = postAudit.notes;
        dataAccess.saveAudit(audit, "");
    }
    
    private Service findService(PostAudit postAudit) {
        List<Service> services = dataAccess.getServices();
        for (Service service : services) {
            if (postAudit.serviceId != null && !postAudit.serviceId.isEmpty() && service.getServiceId().equals(postAudit.serviceId)) {
                return service;
            }
            if (postAudit.serviceAbbr != null && !postAudit.serviceAbbr.isEmpty() && service.getServiceAbbr().equals(postAudit.serviceAbbr)) {
                return service;
            }
            if (postAudit.serviceName != null && !postAudit.serviceName.isEmpty() && service.getServiceName().equals(postAudit.serviceName)) {
                return service;
            }
        }
        if (postAudit.serviceId != null && !postAudit.serviceId.isEmpty()) {
            logger.warn("Could not find service {}, so can not record audit", postAudit.serviceId);
        }
        if (postAudit.serviceAbbr != null && !postAudit.serviceAbbr.isEmpty()) {
            logger.warn("Could not find service {}, so can not record audit", postAudit.serviceAbbr);
        }
        if (postAudit.serviceName != null && !postAudit.serviceId.isEmpty()) {
            logger.warn("Could not find service {}, so can not record audit", postAudit.serviceName);
        }
        return null;
    }

}
