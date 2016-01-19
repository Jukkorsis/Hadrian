package com.northernwall.hadrian.workItem.noop;

import com.codahale.metrics.MetricRegistry;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.workItem.WorkItemSender;
import com.northernwall.hadrian.workItem.WorkItemSenderFactory;
import com.squareup.okhttp.OkHttpClient;

public class NoopWorkItemSenderFactory implements WorkItemSenderFactory {

    @Override
    public WorkItemSender create(Parameters parameters, OkHttpClient client, MetricRegistry metricRegistry) {
        return new NoopWorkItemSender();
    }

}
