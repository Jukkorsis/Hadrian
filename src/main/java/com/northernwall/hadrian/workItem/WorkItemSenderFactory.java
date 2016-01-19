package com.northernwall.hadrian.workItem;

import com.codahale.metrics.MetricRegistry;
import com.northernwall.hadrian.parameters.Parameters;
import com.squareup.okhttp.OkHttpClient;

/**
 *
 * @author rthursto
 */
public interface WorkItemSenderFactory {
    public WorkItemSender create(Parameters parameters, OkHttpClient client, MetricRegistry metricRegistry);
    
}
