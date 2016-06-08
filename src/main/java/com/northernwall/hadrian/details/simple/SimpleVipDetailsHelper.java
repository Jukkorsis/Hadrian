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

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.details.VipDetailsHelper;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.service.dao.GetVipDetailsData;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleVipDetailsHelper implements VipDetailsHelper {

    private final static Logger logger = LoggerFactory.getLogger(SimpleVipDetailsHelper.class);

    private final OkHttpClient client;
    private final Parameters parameters;

    public SimpleVipDetailsHelper(OkHttpClient client, Parameters parameters) {
        this.client = client;
        this.parameters = parameters;
    }

    @Override
    public GetVipDetailsData getDetails(Vip vip) {
        String url = parameters.getString(Const.VIP_DETAILS_URL, null);
        if (url == null || url.isEmpty()) {
            return null;
        }
        url = url.replace("{vip}", vip.getDns());
        Request httpRequest = new Request.Builder().url(url).build();
        try {
            Response resp = client.newCall(httpRequest).execute();
            if (resp.isSuccessful()) {

            }
        } catch (Exception ex) {
            logger.warn("Error while getting secondary vip details for {}, error {}", vip.getVipName(), ex.getMessage());
        }
        return null;
    }

}
