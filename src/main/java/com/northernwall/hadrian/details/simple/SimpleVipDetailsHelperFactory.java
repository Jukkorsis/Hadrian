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
package com.northernwall.hadrian.details.simple;

import com.northernwall.hadrian.details.VipDetailsHelper;
import com.northernwall.hadrian.details.VipDetailsHelperFactory;
import com.northernwall.hadrian.parameters.Parameters;
import com.squareup.okhttp.OkHttpClient;

public class SimpleVipDetailsHelperFactory implements VipDetailsHelperFactory {

    @Override
    public VipDetailsHelper create(OkHttpClient client, Parameters parameters) {
        return new SimpleVipDetailsHelper(client, parameters);
    }

}
