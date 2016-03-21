/*
 * Copyright 2016 Richard Thurston.
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
package com.northernwall.hadrian.utilityHandlers.routingHandler;

import com.northernwall.hadrian.Const;

/**
 *
 * @author rthursto
 */
public enum MethodRule {
    GET,
    PUT,
    POST, 
    PUTPOST,
    DELETE,
    ANY;
    
    public boolean test(String method) {
        if (this == ANY) {
            return true;
        }
        if (this == PUTPOST) {
            return (method.equals(Const.HTTP_POST) || method.equals(Const.HTTP_PUT));
        }
        return this.toString().equals(method);
    }

}
