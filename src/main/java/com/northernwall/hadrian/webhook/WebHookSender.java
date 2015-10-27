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
package com.northernwall.hadrian.webhook;

import com.google.gson.Gson;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.webhook.dao.HostData;
import com.northernwall.hadrian.webhook.dao.PostVipContainer;
import com.northernwall.hadrian.webhook.dao.PutHostContainer;
import com.northernwall.hadrian.webhook.dao.PostHostVipContainer;
import com.northernwall.hadrian.webhook.dao.PostHostContainer;
import com.northernwall.hadrian.webhook.dao.PutVipContainer;
import com.northernwall.hadrian.webhook.dao.ServiceData;
import com.northernwall.hadrian.webhook.dao.VipData;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Thurston
 */
public class WebHookSender {
    private final static Logger logger = LoggerFactory.getLogger(WebHookSender.class);

    private final String callbackUrl;
    private final String hostUrl;
    private final String vipUrl;
    private final String hostVipUrl;

    private final Gson gson;
    private final OkHttpClient client;

    public WebHookSender(Properties properties, OkHttpClient client) {
        this.client = client;
        gson = new Gson();

        int port = Integer.parseInt(properties.getProperty(Const.JETTY_PORT, Const.JETTY_PORT_DEFAULT));

        callbackUrl = Const.HTTP + properties.getProperty(Const.WEB_HOOK_CALLBACK_HOST, Const.WEB_HOOK_CALLBACK_HOST_DEFAULT) + ":" + port + "/webhook/callback";
        hostUrl = Const.HTTP + properties.getProperty(Const.WEB_HOOK_HOST_URL, Const.WEB_HOOK_HOST_URL_DEFAULT);
        vipUrl = Const.HTTP + properties.getProperty(Const.WEB_HOOK_HOST_VIP_URL, Const.WEB_HOOK_VIP_URL_DEFAULT);
        hostVipUrl = Const.HTTP + properties.getProperty(Const.WEB_HOOK_HOST_VIP_URL, Const.WEB_HOOK_HOST_VIP_URL_DEFAULT);
    }

    public void postHost(Service service, Host host) throws IOException {
        PostHostContainer data = new PostHostContainer();
        data.callbackUrl = callbackUrl;
        data.service = ServiceData.create(service);
        data.host = HostData.create(host);

        post(hostUrl, data);
    }

    public void putHost(Service service, Host host, WorkItem workItem) throws IOException {
        PutHostContainer data = new PutHostContainer();
        data.callbackUrl = callbackUrl;
        data.service = ServiceData.create(service);
        data.host = HostData.create(host);
        data.newEnv = workItem.getEnv();
        data.newSize = workItem.getSize();
        data.newVersion = workItem.getVersion();

        put(hostUrl, data);
    }

    public void deleteHost(Service service, Host host) throws IOException {
        PostHostContainer data = new PostHostContainer();
        data.callbackUrl = callbackUrl;
        data.service = ServiceData.create(service);
        data.host = HostData.create(host);

        delete(hostUrl, data);
    }

    public void postVip(Service service, Vip vip) throws IOException {
        PostVipContainer data = new PostVipContainer();
        data.callbackUrl = callbackUrl;
        data.service = ServiceData.create(service);
        data.vip = VipData.create(vip);

        post(vipUrl, data);
    }

    public void putVip(Service service, Vip vip, WorkItem workItem) throws IOException {
        PutVipContainer data = new PutVipContainer();
        data.callbackUrl = callbackUrl;
        data.service = ServiceData.create(service);
        data.vip = VipData.create(vip);
        data.newExternal = workItem.getExternal();
        data.newServicePort = workItem.getServicePort();

        put(vipUrl, data);
    }

    public void deleteVip(Service service, Vip vip) throws IOException {
        PostVipContainer data = new PostVipContainer();
        data.callbackUrl = callbackUrl;
        data.service = ServiceData.create(service);
        data.vip = VipData.create(vip);

        delete(vipUrl, data);
    }

    public void postHostVip(Service service, Host host, Vip vip) throws IOException {
        PostHostVipContainer data = new PostHostVipContainer();
        data.callbackUrl = callbackUrl;
        data.service = ServiceData.create(service);
        data.host = HostData.create(host);
        data.vip = VipData.create(vip);

        post(hostVipUrl, data);
    }

    public void deleteHostVip(Service service, Host host, Vip vip) throws IOException {
        PostHostVipContainer data = new PostHostVipContainer();
        data.callbackUrl = callbackUrl;
        data.service = ServiceData.create(service);
        data.host = HostData.create(host);
        data.vip = VipData.create(vip);

        delete(hostVipUrl, data);
    }

    public void post(String url, Object data) throws IOException {
        RequestBody body = RequestBody.create(Const.JSON_MEDIA_TYPE, gson.toJson(data));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).execute();
    }

    public void put(String url, Object data) throws IOException {
        RequestBody body = RequestBody.create(Const.JSON_MEDIA_TYPE, gson.toJson(data));
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();
        client.newCall(request).execute();
    }

    public void delete(String url, Object data) throws IOException {
        RequestBody body = RequestBody.create(Const.JSON_MEDIA_TYPE, gson.toJson(data));
        Request request = new Request.Builder()
                .url(url)
                .delete(body)
                .build();
        client.newCall(request).execute();
    }

}
