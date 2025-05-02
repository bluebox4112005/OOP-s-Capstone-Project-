package com.elocker;

import java.util.Date;

public class Document {
    private String documentId;
    private String documentName;
    private String documentType;
    private Date uploadDate;
    private String content;
    private String ownerId;

    public Document(String documentId, String documentName, String documentType, String content, String ownerId) {
        this.documentId = documentId;
        this.documentName = documentName;
        this.documentType = documentType;
        this.uploadDate = new Date();
        this.content = content;
        this.ownerId = ownerId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getDocumentName() {
        return documentName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public String getContent() {
        return content;
    }

    public String getOwnerId() {
        return ownerId;
    }
} 