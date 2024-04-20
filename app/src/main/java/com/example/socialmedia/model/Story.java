package com.example.socialmedia.model;

import com.google.firebase.firestore.DocumentReference;

public class Story {
    DocumentReference user;
    String id,url, type;

    public Story() {
    }

    public Story(DocumentReference user, String id, String url, String type) {
        this.user = user;
        this.id = id;
        this.url = url;
        this.type = type;
    }

    public DocumentReference getUser() {
        return user;
    }

    public void setUser(DocumentReference user) {
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Story{" +
                "user=" + user +
                ", id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
