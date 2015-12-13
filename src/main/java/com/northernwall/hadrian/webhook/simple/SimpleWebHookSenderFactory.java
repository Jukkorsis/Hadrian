package com.northernwall.hadrian.webhook.simple;

import com.northernwall.hadrian.webhook.WebHookSender;
import com.northernwall.hadrian.webhook.WebHookSenderFactory;
import com.squareup.okhttp.OkHttpClient;
import java.util.Properties;

public class SimpleWebHookSenderFactory implements WebHookSenderFactory {

    @Override
    public WebHookSender create(Properties properties, OkHttpClient client) {
        return new SimpleWebHookSender(properties, client);
    }

}
