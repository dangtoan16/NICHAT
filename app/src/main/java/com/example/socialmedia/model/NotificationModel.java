package com.example.socialmedia.model;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class NotificationModel {
    String id, notification;

    DocumentReference target, user;
    @ServerTimestamp
    Date timestamp;

    public NotificationModel() {
    }

    public NotificationModel(String id, String notification,  DocumentReference target, DocumentReference user, Date timestamp) {
        this.id = id;
        this.notification = notification;
        this.target = target;
        this.user = user;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }


    public DocumentReference getTarget() {
        return target;
    }

    public void setTarget(DocumentReference target) {
        this.target = target;
    }

    public DocumentReference getUser() {
        return user;
    }

    public void setUser(DocumentReference user) {
        this.user = user;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
