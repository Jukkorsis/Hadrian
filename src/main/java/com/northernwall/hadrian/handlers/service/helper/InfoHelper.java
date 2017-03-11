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
package com.northernwall.hadrian.handlers.service.helper;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.northernwall.hadrian.config.Const;
import com.northernwall.hadrian.parameters.Parameters;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.dshops.metrics.MetricRegistry;

public class InfoHelper {

    private final MetricRegistry metricRegistry;
    private final LoadingCache<CacheKey, Integer> availabilityCache;
    private final LoadingCache<CacheKey, String> versionCache;

    public InfoHelper(Parameters parameters, OkHttpClient client, MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        
        availabilityCache = CacheBuilder.newBuilder()
                .expireAfterWrite(15, TimeUnit.SECONDS)
                .maximumSize(10_000)
                .build(new CacheLoader<CacheKey, Integer>() {
                    @Override
                    public Integer load(CacheKey key) throws Exception {
                        try {
                            metricRegistry.event("readAvailability.miss", 
                                "targetHost", key.getHost());
                            String temp = Const.HTTP + key.getUrl().replace(Const.HOST, key.getHost());
                            Builder builder = new Request.Builder().url(temp);
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
                            return response.code();
                        } catch (IOException ex) {
                            return -1;
                        }
                    }
                });

        versionCache = CacheBuilder.newBuilder()
                .expireAfterWrite(15, TimeUnit.SECONDS)
                .maximumSize(10_000)
                .build(new CacheLoader<CacheKey, String>() {
                    @Override
                    public String load(CacheKey key) throws Exception {
                        try {
                            metricRegistry.event("readVersion.miss", 
                                "targetHost", key.getHost());
                            String temp = Const.HTTP + key.getUrl().replace(Const.HOST, key.getHost());
                            Builder builder = new Request.Builder().url(temp);
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
                                return response.body().string();
                            } else {
                                return "Error: " + response.code();
                            }
                        } catch (UnknownHostException ex) {
                            return "Unknown Host";
                        } catch (ConnectException | SocketTimeoutException ex) {
                            return "Time Out";
                        } catch (IOException ex) {
                            return "IO Exception";
                        }
                    }
                });
    }

    public int readAvailability(String host, String url) {
        if (url == null || url.isEmpty()) {
            return -1;
        }
        try {
            metricRegistry.event("readAvailability.request", 
                    "targetHost", host);
            return availabilityCache.get(new CacheKey(host, url));
        } catch (ExecutionException ex) {
            return -1;
        }
    }

    public String readVersion(String host, String url) {
        if (url == null || url.isEmpty()) {
            return "No Version URL";
        }
        try {
            metricRegistry.event("readVersion.request", 
                    "targetHost", host);
            return versionCache.get(new CacheKey(host, url));
        } catch (ExecutionException ex) {
            return "Internal Error";
        }
    }

}
