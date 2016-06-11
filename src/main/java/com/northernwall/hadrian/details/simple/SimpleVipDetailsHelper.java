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
import com.northernwall.hadrian.service.dao.GetVipDetailCellData;
import com.northernwall.hadrian.service.dao.GetVipDetailRowData;
import com.northernwall.hadrian.service.dao.GetVipDetailsData;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.Comparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleVipDetailsHelper implements VipDetailsHelper {

    private final static Logger logger = LoggerFactory.getLogger(SimpleVipDetailsHelper.class);

    private final OkHttpClient client;
    private final Parameters parameters;
    private final ConfigHelper configHelper;
    private final Gson gson;

    public SimpleVipDetailsHelper(OkHttpClient client, Parameters parameters, ConfigHelper configHelper) {
        this.client = client;
        this.parameters = parameters;
        this.configHelper = configHelper;
        this.gson = new Gson();
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
        for (String dataCenter : configHelper.getConfig().dataCenters) {
            getDetailsForDataCenter(vip, data, vipUrl, poolUrl, dataCenter);
        }
        Collections.sort(data.rows, new Comparator<GetVipDetailRowData>() {
            @Override
            public int compare(GetVipDetailRowData o1, GetVipDetailRowData o2) {
                return o1.hostName.compareTo(o2.hostName);
            }
        });
        return data;
    }

    private void getDetailsForDataCenter(Vip vip, GetVipDetailsData data, String vipUrl, String poolUrl, String dataCenter) {
        VipInfo vipInfo = getVipInfo(vipUrl, dataCenter);
        if (vipInfo == null) {
            return;
        }
        data.address.put(dataCenter, vipInfo.address);

        for (VipPortInfo vipPortInfo : vipInfo.ports) {
            if (vipPortInfo.port == vip.getVipPort()) {
                VipPoolInfo vipPoolInfo = getPoolInfo(poolUrl, vipPortInfo.poolName, dataCenter);
                if (vipPoolInfo != null) {
                    for (VipMemberInfo member : vipPoolInfo.members) {
                        GetVipDetailCellData cell = new GetVipDetailCellData();
                        cell.priority = member.priority;
                        cell.connections = member.connections;
                        if (member.status == 0) {
                            cell.status = "Off";
                        } else if (member.status == 1) {
                            cell.status = "On";
                        } else {
                            cell.status = "Error";
                        }
                        data.find(member.hostName).details.put(dataCenter, cell);
                    }
                }
            }
        }
    }

    private VipInfo getVipInfo(String url, String dataCenter) {
        url = url.replace("{dc}", dataCenter.toUpperCase());
        Request httpRequest = new Request.Builder().url(url).build();
        try {
            Response resp = client.newCall(httpRequest).execute();
            try (InputStream stream = resp.body().byteStream()) {
                if (resp.isSuccessful()) {
                    Reader reader = new InputStreamReader(stream);
                    VipsInfo vipsInfo = gson.fromJson(reader, VipsInfo.class);
                    return vipsInfo.vips.get(0);
                } else {
                    logger.warn("Call to {} failed with code {}", url, resp.code());
                }
            }
        } catch (Exception ex) {
            logger.warn("Error while getting secondary vip details with {}, error {}", url, ex.getMessage());
        }
        return null;
    }

    private VipPoolInfo getPoolInfo(String url, String poolName, String dataCenter) {
        url = url.replace("{pool}", poolName);
        url = url.replace("{dc}", dataCenter.toUpperCase());
        Request httpRequest = new Request.Builder().url(url).build();
        try {
            Response resp = client.newCall(httpRequest).execute();
            try (InputStream stream = resp.body().byteStream()) {
                if (resp.isSuccessful()) {
                    Reader reader = new InputStreamReader(stream);
                    VipPoolsInfo vipPoolsInfo = gson.fromJson(reader, VipPoolsInfo.class);
                    return vipPoolsInfo.pools.get(0);
                } else {
                    logger.warn("Call to {} failed with code {}", url, resp.code());
                }
            }
        } catch (Exception ex) {
            logger.warn("Error while getting secondary vip details with {}, error {}", url, ex.getMessage());
        }
        return null;
    }

}
