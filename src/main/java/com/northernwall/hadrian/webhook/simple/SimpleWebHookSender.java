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
package com.northernwall.hadrian.webhook.simple;

import com.google.gson.Gson;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.webhook.WebHookSender;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Richard Thurston
 */
public class SimpleWebHookSender extends WebHookSender {
    private final String url;

    private final Gson gson;
    private final OkHttpClient client;

    public SimpleWebHookSender(Properties properties, OkHttpClient client) {
        super(properties);
        
        this.client = client;
        gson = new Gson();

        url = properties.getProperty(Const.SIMPLE_WEB_HOOK_URL, Const.SIMPLE_WEB_HOOK_URL_DEFAULT);
    }
    
    @Override
    public void sendWorkItem(WorkItem workItem) throws IOException {
        RequestBody body = RequestBody.create(Const.JSON_MEDIA_TYPE, gson.toJson(workItem));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).execute();
    }

}
