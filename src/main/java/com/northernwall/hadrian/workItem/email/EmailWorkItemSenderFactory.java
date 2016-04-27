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
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.workItem.WorkItemSender;
import com.northernwall.hadrian.workItem.WorkItemSenderFactory;
import com.squareup.okhttp.OkHttpClient;

public class EmailWorkItemSenderFactory implements WorkItemSenderFactory {

    @Override
    public WorkItemSender create(Parameters parameters, DataAccess dataAccess, OkHttpClient client, MetricRegistry metricRegistry) {
        return new EmailWorkItemSender(parameters, metricRegistry);
    }

}
