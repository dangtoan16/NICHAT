package com.example.socialmedia.model;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class HistoryChatModel {

    private String id, lastMessage;
    private List<String> uid;

    @ServerTimestamp
    private Date time;

    public HistoryChatModel() {
    }

    public HistoryChatModel(String id, String lastMessage, List<String> uid, Date time) {
        this.id = id;
        this.lastMessage = lastMessage;
        this.uid = uid;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public List<String> getUid() {
        return uid;
    }

    public void setUid(List<String> uid) {
        this.uid = uid;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "HistoryChatModel{" +

                ", uid=" + uid +
                ", time=" + time +
                '}';
    }
}
