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
package com.northernwall.hadrian.handlers.utility.routingHandler;

/**
 *
 * @author rthursto
 */
public enum TargetRule {
    EQUALS,
    STARTS_WITH,
    MATCHES,
    ANY;
    
    public boolean test(String pattern, String target) {
        switch (this) {
            case EQUALS:
                return pattern.equalsIgnoreCase(target);
            case STARTS_WITH:
                return target.startsWith(pattern);
            case MATCHES:
                return target.matches(pattern);
            case ANY:
                return true;
        }
        return false;
    }

}
