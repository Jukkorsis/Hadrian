package com.northernwall.hadrian.service;

import com.northernwall.hadrian.Const;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Properties;

public class InfoHelper {
    private final OkHttpClient client;
    private final String domain;

    public InfoHelper(Properties properties, OkHttpClient client) {
        this.client = client;
        
        String temp = properties.getProperty(Const.HOST_DOMAIN, Const.HOST_DOMAIN_DEFAULT);
        if (temp.startsWith(".")) {
            domain = temp;
        } else {
            domain = "." + temp;
        }
    }

    public int readAvailability(String host, String url) throws IOException {
        try {
            Request request = new Request.Builder()
                    .url(Const.HTTP + host + domain + url)
                    .build();
            Response response = client.newCall(request).execute();
            return response.code();
        } catch (ConnectException | UnknownHostException | SocketTimeoutException ex) {
            return -1;
        }
    }

    public String readVersion(String host, String url) throws IOException {
        try {
            Request request = new Request.Builder()
                    .url(Const.HTTP + host + domain + url)
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (UnknownHostException ex) {
            return "Unknown Host";
        } catch (ConnectException | SocketTimeoutException ex) {
            return "Time Out";
        }
    }

}
