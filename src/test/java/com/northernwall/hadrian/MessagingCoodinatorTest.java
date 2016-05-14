/*
 * Copyright 2016 Richard Thurston.
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
package com.northernwall.hadrian;

import com.northernwall.hadrian.messaging.MessagingCoodinator;
import com.northernwall.hadrian.stubs.StubMessageProcessor;
import com.northernwall.hadrian.stubs.StubParameters;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.OkHttpClient;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author rthursto
 */
public class MessagingCoodinatorTest {

    private final OkHttpClient client;

    public MessagingCoodinatorTest() {
        client = new OkHttpClient();
        client.setConnectTimeout(2, TimeUnit.SECONDS);
        client.setReadTimeout(2, TimeUnit.SECONDS);
        client.setWriteTimeout(2, TimeUnit.SECONDS);
        client.setFollowSslRedirects(false);
        client.setFollowRedirects(false);
        client.setConnectionPool(new ConnectionPool(5, 60 * 1000));
    }

    @Test
    public void sendMessageTest() {
        MessagingCoodinator mc = new MessagingCoodinator(new StubParameters(), client);
        Map<String, String> data = new HashMap<>();
        data.put("A", "a");
        data.put("B", "b");
        data.put("C", null);
        mc.sendMessage("TEST", null, data);
        Assert.assertEquals("Hi a.", StubMessageProcessor.text);
    }

}
