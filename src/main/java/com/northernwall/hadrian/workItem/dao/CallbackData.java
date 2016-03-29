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
package com.northernwall.hadrian.workItem.dao;

import com.northernwall.hadrian.workItem.Result;

public class CallbackData {

    /**
     * The value of the X-Request-Id header used to trigger the deployment.
     */
    public String requestId;

    /**
     * one of SUCCESS/FAILURE
     */
    public Result status;

    /**
     * An optional code, provided by the service to assist in diagnosis.
     */
    public int errorCode;

    /**
     * An optional description provide by the service to assist in diagnosis.
     */
    public String errorDescription;
}
