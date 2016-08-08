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
package com.northernwall.hadrian;

import com.northernwall.hadrian.service.ModuleCreateHandler;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author rthursto
 */
public class ModuleCreateTest {
    
    public ModuleCreateTest() {
    }
    
    @Test
    public void scrubFolderTest() {
        Assert.assertEquals("/", ModuleCreateHandler.scrubFolder(null));
        Assert.assertEquals("/", ModuleCreateHandler.scrubFolder(""));
        Assert.assertEquals("/", ModuleCreateHandler.scrubFolder(" "));
        Assert.assertEquals("/", ModuleCreateHandler.scrubFolder("/"));
        Assert.assertEquals("/foo", ModuleCreateHandler.scrubFolder("foo"));
        Assert.assertEquals("/foo", ModuleCreateHandler.scrubFolder(" foo "));
        Assert.assertEquals("/foo", ModuleCreateHandler.scrubFolder("/foo"));
        Assert.assertEquals("/foo", ModuleCreateHandler.scrubFolder("/foo/"));
        Assert.assertEquals("/foo/bar", ModuleCreateHandler.scrubFolder("/foo/bar"));
        Assert.assertEquals("/foo/bar", ModuleCreateHandler.scrubFolder("/foo/bar/"));
    }
    
    @Test
    public void isSubFolderTest() {
        Assert.assertTrue(ModuleCreateHandler.isSubFolder("/foo/bar", "/"));
        Assert.assertTrue(ModuleCreateHandler.isSubFolder("/foo/bar", "/foo"));
        Assert.assertTrue(ModuleCreateHandler.isSubFolder("/foo/bar", "/foo/bar"));
        
        Assert.assertFalse(ModuleCreateHandler.isSubFolder("/foo/other", "/foo/bar"));
        Assert.assertFalse(ModuleCreateHandler.isSubFolder("/foo", "/foo/bar"));
        Assert.assertFalse(ModuleCreateHandler.isSubFolder("/", "/foo/bar"));
        Assert.assertFalse(ModuleCreateHandler.isSubFolder("/other/bar", "/foo/bar"));
    }
    
}
