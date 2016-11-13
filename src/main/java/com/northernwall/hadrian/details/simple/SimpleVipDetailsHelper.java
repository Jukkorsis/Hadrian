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
package com.northernwall.hadrian.details.simple;

import com.google.gson.Gson;
import com.northernwall.hadrian.ConfigHelper;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.details.VipDetailsHelper;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.handlers.service.dao.GetVipDetailsData;
import com.squareup.okhttp.OkHttpClient;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;

public class SimpleVipDetailsHelper implements VipDetailsHelper {

    private final OkHttpClient client;
    private final Parameters parameters;
    private final ConfigHelper configHelper;
    private final Gson gson;
    private final ExecutorService executorService;

    public SimpleVipDetailsHelper(OkHttpClient client, Parameters parameters, ConfigHelper configHelper) {
        this.client = client;
        this.parameters = parameters;
        this.configHelper = configHelper;
        
        gson = new Gson();
        executorService = Executors.newFixedThreadPool(20);
    }

    @Override
    public GetVipDetailsData getDetails(Vip vip) {
        String vipUrl = parameters.getString(Const.VIP_DETAILS_URL, null);
        String poolUrl = parameters.getString(Const.VIP_POOL_DETAILS_URL, null);

        if (vipUrl == null || vipUrl.isEmpty()) {
            return null;
        }
        vipUrl = vipUrl.replace("{vip}", vip.getDns());
        GetVipDetailsData data = new GetVipDetailsData();
        
        List<Future> futures = new LinkedList<>();
        for (String dataCenter : configHelper.getConfig().dataCenters) {
            futures.add(executorService.submit(new SimpleVipDetailsRunnable(gson, client, vip, data, vipUrl, poolUrl, dataCenter)));
        }

        waitForFutures(futures);
        return data;
    }

    protected void waitForFutures(List<Future> futures) {
        for (int i = 0; i < 151; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
            futures.removeIf(new Predicate<Future>() {
                @Override
                public boolean test(Future t) {
                    return t.isDone();
                }
            });
            if (futures.isEmpty()) {
                return;
            }
        }
    }

}
