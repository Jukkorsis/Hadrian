/*
 * Copyright 2014 Richard Thurston.
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
package com.northernwall.hadrian.domain;

import java.util.UUID;

public class Document implements Comparable<Document> {
    private String docId;
    private DocumentType documentType;
    private String title;
    private String link;

    public Document(DocumentType documentType, String title, String link) {
        this.docId = UUID.randomUUID().toString();
        this.documentType = documentType;
        this.title = title;
        this.link = link;
    }

    public String getDocId() {
        return docId;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public int compareTo(Document o) {
        return title.compareTo(o.title);
    }

}
