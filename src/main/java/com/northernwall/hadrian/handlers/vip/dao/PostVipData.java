/*
 * Copyright 2014 Richard Thurston.
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
package com.northernwall.hadrian.handlers.vip.dao;

import java.util.List;

public class PostVipData {
    public String serviceId;
    public String moduleId;
    public String dns;
    public String domain;
    public boolean external;
    public String environment;
    public String inboundProtocol;
    public List<String> inboundModifiers;
    public String outboundProtocol;
    public List<String> outboundModifiers;
    public String priorityMode;
    public int vipPort;
    public int servicePort;
    public int httpCheckPort;

}
