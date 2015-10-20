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

package com.northernwall.hadrian.tree.dao;

import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Thurston
 */
public class TreeNode {
    private final static Logger logger = LoggerFactory.getLogger(TreeNode.class);
    private String label;
    private TreeNodeData data;
    private List<TreeNode> children;
    private List<String> classes;

    public TreeNode() {
        children = new LinkedList<>();
        classes = new LinkedList<>();
    }
    
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public TreeNodeData getData() {
        return data;
    }

    public void setData(TreeNodeData data) {
        this.data = data;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public List<String> getClasses() {
        return classes;
    }

}
