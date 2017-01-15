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

import com.northernwall.hadrian.handlers.service.helper.FolderHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author rthursto
 */
public class FolderHelperTest {

    public FolderHelperTest() {
    }

    @Test
    public void scrubFolderTest() {
        FolderHelper folderHelper = new FolderHelper(null);
        
        Assert.assertNull(folderHelper.scrubFolder(null, "deploy", true));
        try {
            folderHelper.scrubFolder(null, "deploy", false);
            Assert.fail("should have thrown an exception");
        } catch (Exception e) {
        }
        try {
            folderHelper.scrubFolder("", "deploy", false);
            Assert.fail("should have thrown an exception");
        } catch (Exception e) {
        }
        try {
            folderHelper.scrubFolder(" ", "deploy", false);
            Assert.fail("should have thrown an exception");
        } catch (Exception e) {
        }
        try {
            folderHelper.scrubFolder("/", "deploy", false);
            Assert.fail("should have thrown an exception");
        } catch (Exception e) {
        }
        Assert.assertEquals("/foo", folderHelper.scrubFolder("foo", "deploy", false));
        Assert.assertEquals("/foo", folderHelper.scrubFolder(" foo ", "deploy", false));
        Assert.assertEquals("/foo", folderHelper.scrubFolder("/foo", "deploy", false));
        Assert.assertEquals("/foo", folderHelper.scrubFolder("/foo/", "deploy", false));
        Assert.assertEquals("/foo/bar", folderHelper.scrubFolder("/foo/bar", "deploy", false));
        Assert.assertEquals("/foo/bar", folderHelper.scrubFolder("/foo/bar/", "deploy", false));
    }

    @Test
    public void isSubFolderTest() {
        FolderHelper folderHelper = new FolderHelper(null);
        
        try {
            folderHelper.isSubFolder("/foo/bar", "AAA", "/", "BBB");
            Assert.fail("should have thrown an exception");
        } catch (Exception e) {
        }
        try {
            folderHelper.isSubFolder("/foo/bar", "AAA", "/foo", "BBB");
            Assert.fail("should have thrown an exception");
        } catch (Exception e) {
        }
        try {
            folderHelper.isSubFolder("/foo/bar", "AAA", "/foo/bar", "BBB");
            Assert.fail("should have thrown an exception");
        } catch (Exception e) {
        }

        folderHelper.isSubFolder("/foo/other", "AAA", "/foo/bar", "BBB");
        folderHelper.isSubFolder("/foo", "AAA", "/foo/bar", "BBB");
        folderHelper.isSubFolder("/", "AAA", "/foo/bar", "BBB");
        folderHelper.isSubFolder("/other/bar", "AAA", "/foo/bar", "BBB");
    }

}
