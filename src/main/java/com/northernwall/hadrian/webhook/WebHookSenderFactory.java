package com.northernwall.hadrian.webhook;

import com.squareup.okhttp.OkHttpClient;
import java.util.Properties;

/**
 *
 * @author rthursto
 */
public interface WebHookSenderFactory {
    public WebHookSender create(Properties properties, OkHttpClient client);
    
}
