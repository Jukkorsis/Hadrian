package com.northernwall.hadrian.service;

import com.google.gson.Gson;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.service.dao.GetHostDetailsData;
import com.northernwall.hadrian.service.dao.GetPairData;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

public class HostDetailsHelper {

    private final OkHttpClient client;
    private final String urlTemplate;
    private final Gson gson;

    public HostDetailsHelper(OkHttpClient client, Properties properties) {
        this.client = client;
        this.urlTemplate = properties.getProperty(Const.HOST_DETAILS_URL);
        gson = new Gson();
    }

    public GetHostDetailsData getDetails(Host host) {
        GetPairData[] pairs = null;
        if (urlTemplate != null) {
            String url = urlTemplate.replace(Const.HOST, host.getHostName());

            Request httpRequest = new Request.Builder().url(url).build();
            try {
                Response resp = client.newCall(httpRequest).execute();
                Reader reader = new InputStreamReader(resp.body().byteStream());

                pairs = gson.fromJson(reader, GetPairData[].class);
            } catch (IOException ex) {
            }
        }

        GetHostDetailsData details = new GetHostDetailsData();
        if (pairs != null && pairs.length > 0) {
            for (int i=0;i<pairs.length;i++) {
                if (i%2 == 0) {
                    details.left.add(pairs[i]);
                } else {
                    details.right.add(pairs[i]);
                }
            }
        }
        
        return details;
    }

}
