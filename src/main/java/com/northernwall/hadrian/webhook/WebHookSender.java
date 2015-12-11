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
package com.northernwall.hadrian.webhook;

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.domain.WorkItem;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Richard Thurston
 */
public abstract class WebHookSender {
    private final String callbackUrl;

    public WebHookSender(Properties properties) {
        int port = Integer.parseInt(properties.getProperty(Const.JETTY_PORT, Const.JETTY_PORT_DEFAULT));

        callbackUrl = properties.getProperty(Const.WEB_HOOK_CALLBACK_HOST, Const.WEB_HOOK_CALLBACK_HOST_DEFAULT) + ":" + port + "/webhook/callback/";
    }

    public final void applyCallbackUrl(WorkItem workItem) {
        workItem.setCallbackUrl(callbackUrl + workItem.getId());
    }

    public abstract void sendWorkItem(WorkItem workItem) throws IOException;

}
