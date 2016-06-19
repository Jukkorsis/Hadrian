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

import com.northernwall.hadrian.Const;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class InfoHelper {
    private final OkHttpClient client;

    public InfoHelper(OkHttpClient client) {
        this.client = client;
    }

    public int readAvailability(String host, String url) {
        try {
            Request request = new Request.Builder()
                    .url(Const.HTTP + url.replace(Const.HOST, host))
                    .build();
            Response response = client.newCall(request).execute();
            return response.code();
        } catch (IOException ex) {
            return -1;
        }
    }

    public String readVersion(String host, String url) {
        try {
            Request request = new Request.Builder()
                    .url(Const.HTTP + url.replace(Const.HOST, host))
                    .build();
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

}
