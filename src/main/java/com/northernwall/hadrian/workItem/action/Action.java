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

import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.Result;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import java.io.IOException;

public abstract class Action {
    protected final DataAccess dataAccess;
    protected final WorkItemProcessor workItemProcessor;

    public Action(DataAccess dataAccess, WorkItemProcessor workItemProcessor) {
        this.dataAccess = dataAccess;
        this.workItemProcessor = workItemProcessor;
    }

    public void process(WorkItem workItem, Result result) throws IOException {
        if (result == Result.success) {
            success(workItem);
        } else if (result == Result.error) {
            error(workItem);
        }        
    }

    protected abstract void success(WorkItem workItem) throws IOException;

    protected abstract void error(WorkItem workItem) throws IOException;

}
