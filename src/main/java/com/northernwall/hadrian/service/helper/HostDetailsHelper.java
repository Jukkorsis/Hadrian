package com.northernwall.hadrian.service.helper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.service.dao.GetHostDetailsData;
import com.northernwall.hadrian.service.dao.GetPairData;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostDetailsHelper {
    private final static Logger logger = LoggerFactory.getLogger(HostDetailsHelper.class);

    private final OkHttpClient client;
    private final String urlTemplate;
    private final JsonParser parser;

    public HostDetailsHelper(OkHttpClient client, Properties properties) {
        this.client = client;
        this.urlTemplate = properties.getProperty(Const.HOST_DETAILS_URL);
        this.parser = new JsonParser();
    }

    public GetHostDetailsData getDetails(Host host) {
        GetHostDetailsData details = new GetHostDetailsData();
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
        label = label.replace("-", " ").replace("_", " ");
        if (details.left.size() == details.right.size()) {
            details.left.add(new GetPairData(label, value));
        } else {
            details.right.add(new GetPairData(label, value));
        }
    }

}
