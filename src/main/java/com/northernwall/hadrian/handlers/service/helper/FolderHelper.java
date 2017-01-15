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
package com.northernwall.hadrian.handlers.service.helper;

import com.northernwall.hadrian.ConfigHelper;
import com.northernwall.hadrian.handlers.routing.Http400BadRequestException;
import java.util.List;

/**
 *
 * @author Richard
 */
public class FolderHelper {
    private final ConfigHelper configHelper;
    
    public FolderHelper(ConfigHelper configHelper) {
        this.configHelper = configHelper;
    }
    
    public String scrubFolder(String folder, String folderName, boolean nullAllowed) {
        if (folder == null || folder.isEmpty()) {
            if (nullAllowed) {
                return null;
            } else {
                throw new Http400BadRequestException(folderName + " folder can not be null or empty");
            }
        }
        String temp = folder.trim();
        if (temp == null || temp.isEmpty()) {
            if (nullAllowed) {
                return null;
            } else {
                throw new Http400BadRequestException(folderName + " folder can not be null or empty");
            }
        }
        if (temp.equals("/")) {
            throw new Http400BadRequestException(folderName + " folder can not be root");
        }
        if (!temp.startsWith("/")) {
            temp = "/" + temp;
        }
        if (temp.endsWith("/") && temp.length() > 1) {
            temp = temp.substring(0, temp.length()-1);
        }
        return temp;
    }

    /**
     * This method assumes that both folder parameters have already been scrubbed.
     * @param subFolder The sub folder.
     * @param subFolderName
     * @param mainFolder The main folder.
     * @param mainFolderName
     */
    public void isSubFolder(String subFolder, String subFolderName, String mainFolder, String mainFolderName) {
        String tempSubFolder = subFolder;
        if (tempSubFolder.length() > 1) {
            tempSubFolder = tempSubFolder + "/";
        }
        String tempMainFolder = mainFolder;
        if (tempMainFolder.length() > 1) {
            tempMainFolder = tempMainFolder + "/";
        }
        if (tempSubFolder.equals(tempMainFolder)) {
            throw new Http400BadRequestException(subFolderName +" folder can not be the same as the " + mainFolderName + " folder");
        }
        if (tempSubFolder.startsWith(tempMainFolder)) {
            throw new Http400BadRequestException(subFolderName + " folder can not be a sub folder of the " + mainFolderName + " folder");
        }
    }
    
    public void isWhiteListed(String folder, String folderName, String user) {
        if (configHelper == null) {
            throw new RuntimeException("FolderHelper not yet initialized");
        }
        List<String> folderWhiteList = configHelper.getConfig().folderWhiteList;
        if (folderWhiteList == null || folderWhiteList.isEmpty()) {
            return;
        }
        
        String tempFolder = folder;
        if (tempFolder.length() > 1) {
            tempFolder = tempFolder + "/";
        }
        
        for (String whiteFolder : folderWhiteList) {
            String tempWhiteFolder = whiteFolder.replace("{USER}", user);
            if (tempFolder.equals(tempWhiteFolder) || tempFolder.startsWith(tempWhiteFolder)) {
                return;
            }
        }
        throw new Http400BadRequestException(folderName + " folder is not on the whitelist");
    }
    
}
