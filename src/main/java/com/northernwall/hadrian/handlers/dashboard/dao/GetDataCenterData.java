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
package com.northernwall.hadrian.handlers.dashboard.dao;

/**
 *
 * @author Richard
 */
public class GetDataCenterData {
    public int good = 0;
    public int bad = 0;
    public int off = 0;
    public int total = 0;

    public synchronized void incGood() {
        good++;
        total++;
    }

    public synchronized void incOff() {
        off++;
        total++;
    }

    public synchronized void incBad() {
        bad++;
        total++;
    }
    
}
