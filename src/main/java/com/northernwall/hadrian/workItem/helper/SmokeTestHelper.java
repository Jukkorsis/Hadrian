/*
 * Copyright 2017 Richard Thurston.
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
package com.northernwall.hadrian.workItem.helper;

import com.google.gson.Gson;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.workItem.dao.SmokeTestData;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard
 */
public class SmokeTestHelper {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(SmokeTestHelper.class);
    
    private final Parameters parameters;
    private final Gson gson;
    private final OkHttpClient client;

    public SmokeTestHelper(Parameters parameters, Gson gson) {
        this.parameters = parameters;
        this.gson = gson;
        
        client = new OkHttpClient();
        client.setConnectTimeout(2, TimeUnit.SECONDS);
        client.setReadTimeout(10, TimeUnit.MINUTES);
        client.setWriteTimeout(2, TimeUnit.SECONDS);
        client.setFollowSslRedirects(false);
        client.setFollowRedirects(false);
        client.setConnectionPool(new ConnectionPool(15, 60 * 1000));
    }
    
    public SmokeTestData ExecuteSmokeTest(String smokeTestUrl, String endPoint) {
        if (smokeTestUrl == null || smokeTestUrl.isEmpty() || endPoint == null || endPoint.isEmpty()) {
            return null;
        }

        LOGGER.info("Smoke testing EP {} with {}", endPoint, smokeTestUrl);

        String url = Const.HTTP + smokeTestUrl.replace(Const.END_POINT, endPoint);
        try {
            Request.Builder builder = new Request.Builder().url(url);
            if (parameters.getUsername() != null
                    && parameters.getUsername().isEmpty()
                    && parameters.getPassword() != null
                    && parameters.getPassword().isEmpty()) {
                builder.addHeader(
                        "Authorization",
                        Credentials.basic(parameters.getUsername(), parameters.getPassword()));
            }
            Request request = builder.build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                try (InputStream stream = response.body().byteStream()) {
                    Reader reader = new InputStreamReader(stream);
                    return gson.fromJson(reader, SmokeTestData.class);
                }
            } else {
                LOGGER.warn("Call to {} failed with code {}", url, response.code());
                return null;
            }
        } catch (Exception ex) {
            LOGGER.warn("Call to {} failed with exception {}", url, ex.getMessage());
            return null;
        }
    }

}
