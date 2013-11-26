package com.nicocorp.nr1;

/**
 * Created by Nico on 29/06/13.
 */
public class downloadItemObject {
    private long id;
    private String status;
    private String description;
    private String title;
    private String localUrl;
    private String remoteUrl;

    public void setLocalUrl(String localUrl) {
        this.localUrl = localUrl;
    }

    public String getLocalUrl() {
        return localUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }
}

