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
package com.northernwall.hadrian.webhook.email;

import com.northernwall.hadrian.webhook.WebHookSender;
import com.northernwall.hadrian.webhook.WebHookSenderFactory;
import com.squareup.okhttp.OkHttpClient;
import java.util.Properties;

public class EmailWebHookSenderFactory implements WebHookSenderFactory {

    @Override
    public WebHookSender create(Properties properties, OkHttpClient client) {
        return new EmailWebHookSender(properties);
    }

}
