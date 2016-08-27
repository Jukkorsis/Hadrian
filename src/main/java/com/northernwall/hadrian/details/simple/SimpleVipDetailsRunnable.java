package com.northernwall.hadrian.details.simple;

import com.google.gson.Gson;
import com.northernwall.hadrian.details.simple.dao.VipDao;
import com.northernwall.hadrian.details.simple.dao.VipMemberDao;
import com.northernwall.hadrian.details.simple.dao.VipPoolDao;
import com.northernwall.hadrian.details.simple.dao.VipPoolsDao;
import com.northernwall.hadrian.details.simple.dao.VipPortDao;
import com.northernwall.hadrian.details.simple.dao.VipsDao;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.handlers.service.dao.GetVipDetailCellData;
import com.northernwall.hadrian.handlers.service.dao.GetVipDetailsData;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleVipDetailsRunnable implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(SimpleVipDetailsRunnable.class);

    private final Gson gson;
    private final OkHttpClient client;
    private final Vip vip;
    private final GetVipDetailsData data;
    private final String vipUrl;
    private final String poolUrl;
    private final String dataCenter;

    public SimpleVipDetailsRunnable(Gson gson, OkHttpClient client, Vip vip, GetVipDetailsData data, String vipUrl, String poolUrl, String dataCenter) {
        this.gson = gson;
        this.client = client;
        this.vip = vip;
        this.data = data;
        this.vipUrl = vipUrl;
        this.poolUrl = poolUrl;
        this.dataCenter = dataCenter;
    }

    @Override
    public void run() {
        VipDao vipInfo = getVipInfo(vipUrl, dataCenter);
        if (vipInfo == null) {
            return;
        }
        data.address.put(dataCenter, vipInfo.address);
        data.name.put(dataCenter, vipInfo.name);

        for (VipPortDao vipPortInfo : vipInfo.ports) {
            if (vipPortInfo.port == vip.getVipPort()) {
                VipPoolDao vipPoolInfo = getPoolInfo(poolUrl, vipPortInfo.poolName, dataCenter);
                if (vipPoolInfo != null) {
                    data.connections.put(dataCenter, Integer.toString(vipPoolInfo.connections));
                    for (VipMemberDao member : vipPoolInfo.members) {
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
                return;
            }
        }
    }

    private VipDao getVipInfo(String url, String dataCenter) {
        url = url.replace("{dc}", dataCenter.toUpperCase());
        Request httpRequest = new Request.Builder().url(url).build();
        try {
            Response resp = client.newCall(httpRequest).execute();
            try (InputStream stream = resp.body().byteStream()) {
                if (resp.isSuccessful()) {
                    Reader reader = new InputStreamReader(stream);
                    VipsDao vipsInfo = gson.fromJson(reader, VipsDao.class);
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

    private VipPoolDao getPoolInfo(String url, String poolName, String dataCenter) {
        url = url.replace("{pool}", poolName);
        url = url.replace("{dc}", dataCenter.toUpperCase());
        Request httpRequest = new Request.Builder().url(url).build();
        try {
            Response resp = client.newCall(httpRequest).execute();
            try (InputStream stream = resp.body().byteStream()) {
                if (resp.isSuccessful()) {
                    Reader reader = new InputStreamReader(stream);
                    VipPoolsDao vipPoolsInfo = gson.fromJson(reader, VipPoolsDao.class);
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
