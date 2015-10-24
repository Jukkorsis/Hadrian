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

package com.northernwall.hadrian.webhook.dao;

import com.northernwall.hadrian.domain.Vip;

/**
 *
 * @author Richard Thurston
 */
public class VipData {
    public String vipId;
    public String vipName;
    public String dns;
    public boolean external;
    public String network;
    public String protocol;
    public int vipPort;
    public int servicePort;

    public static VipData create(Vip vip) {
        VipData temp = new VipData();
        temp.vipId = vip.getVipId();
        temp.vipName = vip.getVipName();
        temp.dns = vip.getDns();
        temp.external = vip.isExternal();
        temp.network = vip.getNetwork();
        temp.protocol = vip.getProtocol();
        temp.vipPort = vip.getVipPort();
        temp.servicePort = vip.getServicePort();
        return temp;
    }

}