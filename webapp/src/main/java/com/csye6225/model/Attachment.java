package com.csye6225.model;

import java.util.UUID;

public class Attachment {

    private UUID attachment_id;
    private String url;

    public Attachment() {

    }
     public Attachment(String url)
     {
         this.url = url;
     }

    public UUID getAttachment_id() {
        return attachment_id;
    }

    public void setAttachment_id(UUID attachment_id) {
        this.attachment_id = attachment_id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
