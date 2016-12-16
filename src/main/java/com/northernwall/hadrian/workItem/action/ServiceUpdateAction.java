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
package com.northernwall.hadrian.workItem.action;

import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.Result;
import java.util.Map;

public class ServiceUpdateAction extends Action {

    @Override
    public Result process(WorkItem workItem) {
        return Result.success;
    }

    @Override
    public void recordAudit(WorkItem workItem, Map<String, String> notes, Result result, String output) {
        if (workItem.getReason() != null && !workItem.getReason().isEmpty()) {
            notes.put("Reason", workItem.getReason());
        }
        writeAudit(workItem, result, notes, output);
    }

}
