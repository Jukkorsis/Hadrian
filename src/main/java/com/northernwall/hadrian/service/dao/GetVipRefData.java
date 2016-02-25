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
package com.northernwall.hadrian.service.dao;

import com.northernwall.hadrian.domain.VipRef;

public class GetVipRefData {
    public String hostId;
    public String vipId;
    public String status;
    public String vipName;

    public static GetVipRefData create(VipRef ref) {
        GetVipRefData temp = new GetVipRefData();
        temp.hostId = ref.getHostId();
        temp.vipId = ref.getVipId();
        temp.status = ref.getStatus();
        return temp;
    }

}
