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
import com.northernwall.hadrian.workItem.dao.CallbackData;
import java.util.Map;

public class ModuleCreateAction extends Action {

    @Override
    public Result process(WorkItem workItem) {
        Result result = Result.success;
        recordAudit(workItem, null, result, null);
        return result;
    }

    @Override
    public Result processCallback(WorkItem workItem, CallbackData callbackData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void recordAudit(WorkItem workItem, CallbackData callbackData, Result result, String output) {
        Map<String, String> notes = createNotesFromCallback(callbackData);
        notes.put("Template", workItem.getMainModule().template);
        notes.put("Type", workItem.getMainModule().moduleType.toString());
        recordAudit(workItem, result, notes, output);
    }

}
