package com.northernwall.hadrian.webhook.simple;

import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.webhook.WebHookSender;
import com.northernwall.hadrian.webhook.WebHookSenderFactory;
import com.squareup.okhttp.OkHttpClient;

public class SimpleWebHookSenderFactory implements WebHookSenderFactory {

    @Override
    public WebHookSender create(Parameters parameters, OkHttpClient client) {
        return new SimpleWebHookSender(parameters, client);
    }

}
