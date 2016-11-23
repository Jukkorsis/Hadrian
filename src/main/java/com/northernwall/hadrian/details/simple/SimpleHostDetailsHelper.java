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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.details.HostDetailsHelper;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.parameters.ParameterChangeListener;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.handlers.service.dao.GetHostDetailsData;
import com.northernwall.hadrian.handlers.service.dao.GetPairData;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleHostDetailsHelper implements HostDetailsHelper, ParameterChangeListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(HostDetailsHelper.class);

    private final OkHttpClient client;
    private final Parameters parameters;
    private final JsonParser parser;
    private final List<String> urlTemplates;
    private final List<String> attributes;

    public SimpleHostDetailsHelper(OkHttpClient client, Parameters parameters) {
        this.client = client;
        this.parameters = parameters;
        this.parser = new JsonParser();
        this.urlTemplates = new LinkedList<>();
        this.attributes = new LinkedList<>();
        load();
        parameters.registerChangeListener(this);
    }

    @Override
    public void onChange(List<String> keys) {
        load();
        LOGGER.info("Reloading parameters.");
    }

    private void load() {
        String temp = parameters.getString(Const.HOST_DETAILS_URL, null);
        urlTemplates.clear();
        if (temp != null && !temp.isEmpty()) {
            String[] parts = temp.split(",");
            for (String part : parts) {
                part = part.trim();
                if (!part.isEmpty()) {
                    urlTemplates.add(part);
                }
            }
        }

        temp = parameters.getString(Const.HOST_DETAILS_ATTRIBUTES, null);
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

    @Override
    public GetHostDetailsData getDetails(Host host) {
        List<GetPairData> pairs = new LinkedList<>();
        pairs.add(new GetPairData("host_uuid", host.getHostId()));

        if (!urlTemplates.isEmpty()) {
            for (String urlTemplate : urlTemplates) {
                String url = urlTemplate.replace(Const.HOST, host.getHostName());
                getDetailsFromUrl(host, url, pairs);
            }
        }

        Collections.sort(pairs);
        GetHostDetailsData details = new GetHostDetailsData();
        for (GetPairData pair : pairs) {
            details.addPair(pair);
        }

        return details;
    }

    private void getDetailsFromUrl(Host host, String url, List<GetPairData> pairs) {
        Request httpRequest = new Request.Builder().url(url).build();
        try {
            Response resp = client.newCall(httpRequest).execute();
            try (Reader reader = new InputStreamReader(resp.body().byteStream())) {
                if (resp.isSuccessful()) {
                    JsonElement jsonElement = parser.parse(reader);
                    if (jsonElement.isJsonObject()) {
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                            processAttribute(null, entry, pairs);
                        }
                    }
                } else {
                    LOGGER.warn("Call to {} failed with code {}", url, resp.code());
                }
            }
        } catch (Exception ex) {
            LOGGER.warn("Error while getting secondary host details for {}, error {}", host.getHostName(), ex.getMessage());
        }
    }

    private void processAttribute(String prefix, Map.Entry<String, JsonElement> entry, List<GetPairData> pairs) {
        if (entry.getValue().isJsonPrimitive()) {
            addPair(prefix, entry.getKey(), entry.getValue().getAsString(), pairs);
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
                addPair(prefix, entry.getKey(), buffer.toString(), pairs);
            }
        } else if (entry.getValue().isJsonObject()) {
            JsonObject jsonObject = entry.getValue().getAsJsonObject();
            for (Map.Entry<String, JsonElement> innerEntry : jsonObject.entrySet()) {
                processAttribute(entry.getKey(), innerEntry, pairs);
            }
        }
    }

    private void addPair(String prefix, String label, String value, List<GetPairData> data) {
        if (label == null || label.isEmpty() || value == null || value.isEmpty()) {
            return;
        }
        if (prefix != null && !prefix.isEmpty()) {
            label = prefix + "_" + label;
        }
        if (attributes.isEmpty() || attributes.contains(label)) {
            label = label.replace("-", " ").replace("_", " ");
            data.add(new GetPairData(label, value));
        }
    }

}
