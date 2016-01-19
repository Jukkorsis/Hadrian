package com.northernwall.hadrian.workItem.noop;

import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.WorkItemSender;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoopWorkItemSender implements WorkItemSender {

    private final static Logger logger = LoggerFactory.getLogger(NoopWorkItemSender.class);

    @Override
    public boolean sendWorkItem(WorkItem workItem) throws IOException {
        logger.info("Work Item {} {} for service {}", workItem.getType(), workItem.getType(), workItem.getService().serviceName);
        return true;
    }

}
