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
package com.northernwall.hadrian.service.helper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.parameters.ParameterChangeListener;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.service.dao.GetHostDetailsData;
import com.northernwall.hadrian.service.dao.GetPairData;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostDetailsHelper implements ParameterChangeListener {
    private final static Logger logger = LoggerFactory.getLogger(HostDetailsHelper.class);

    private final OkHttpClient client;
    private final Parameters parameters;
    private final JsonParser parser;
    private String urlTemplate;
    private final List<String> attributes;

    public HostDetailsHelper(OkHttpClient client, Parameters parameters) {
        this.client = client;
        this.parameters = parameters;
        this.parser = new JsonParser();
        this.attributes = new LinkedList<>();
        load();
        parameters.registerChangeListener(this);
    }

    @Override
    public void onChange(List<String> keys) {
        load();
    }
    
    private void load() {
        urlTemplate = parameters.getString(Const.HOST_DETAILS_URL, null);
        String temp = parameters.getString(Const.HOST_DETAILS_ATTRIBUTES, null);
        attributes.clear();
        if (temp != null && !temp.isEmpty()) {
            String[] parts = temp.split(",");
            for (String part : parts) {
                part = part.trim();
                if (!part.isEmpty()) {
                    attributes.add(part);
                }
            }
        }
    }
    
    public GetHostDetailsData getDetails(Host host) {
        GetHostDetailsData details = new GetHostDetailsData();
        addPair("host id", host.getHostId(), details);
        if (urlTemplate != null) {
            String url = urlTemplate.replace(Const.HOST, host.getHostName());
            Request httpRequest = new Request.Builder().url(url).build();
            try {
                Response resp = client.newCall(httpRequest).execute();
                if (resp.isSuccessful()) {
                    Reader reader = new InputStreamReader(resp.body().byteStream());

                    JsonElement jsonElement = parser.parse(reader);
                    if (jsonElement.isJsonObject()) {
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                            if (entry.getValue().isJsonPrimitive()) {
                                addPair(entry.getKey(), entry.getValue().getAsString(), details);
                            } else if (entry.getValue().isJsonArray()) {
                                StringBuffer buffer = null;
                                JsonArray jsonArray = entry.getValue().getAsJsonArray();
                                for (int i = 0; i < jsonArray.size(); i++) {
                                    JsonElement arrayElement = jsonArray.get(i);
                                    if (arrayElement.isJsonPrimitive()) {
                                        if (buffer == null) {
                                            buffer = new StringBuffer(arrayElement.getAsString());
                                        } else {
                                            buffer.append(", ");
                                            buffer.append(arrayElement.getAsString());
                                        }
                                    }
                                }
                                if (buffer != null) {
                                    addPair(entry.getKey(), buffer.toString(), details);
                                }
                            }
                        }
                    }
                } else {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(resp.body().byteStream()));
                    addPair("Error", reader.readLine(), details);
                }
            } catch (IOException ex) {
                logger.warn("Error while getting secondary host details for {}, error {}", host.getHostName(), ex.getMessage());
                addPair("Error", "Read failure", details);
            }
        }
        return details;
    }

    private void addPair(String label, String value, GetHostDetailsData details) {
        if (label == null || label.isEmpty() || value == null || value.isEmpty()) {
            return;
        }
        if (attributes.isEmpty() || attributes.contains(label)) {
            label = label.replace("-", " ").replace("_", " ");
            if (details.left.size() == details.right.size()) {
                details.left.add(new GetPairData(label, value));
            } else {
                details.right.add(new GetPairData(label, value));
            }
        }
    }

}
