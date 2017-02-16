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
import com.northernwall.hadrian.Const;
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

public class InfoHelper {

    private final LoadingCache<String, Integer> availabilityCache;
    private final LoadingCache<String, String> versionCache;

    public InfoHelper(Parameters parameters, OkHttpClient client) {
        availabilityCache = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.SECONDS)
                .maximumSize(10_000)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) throws Exception {
                        try {
                            Builder builder = new Request.Builder().url(key);
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
                .expireAfterWrite(10, TimeUnit.SECONDS)
                .maximumSize(10_000)
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String key) throws Exception {
                        try {
                            Builder builder = new Request.Builder().url(key);
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
            return availabilityCache.get(Const.HTTP + url.replace(Const.HOST, host));
        } catch (ExecutionException ex) {
            return -1;
        }
    }

    public String readVersion(String host, String url) {
        if (url == null || url.isEmpty()) {
            return "No Version URL";
        }
        try {
            return versionCache.get(Const.HTTP + url.replace(Const.HOST, host));
        } catch (ExecutionException ex) {
            return "Internal Error";
        }
    }

}
