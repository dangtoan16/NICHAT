package com.example.socialmedia.model;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class CommentModel {
    String comment, commentID;
    boolean deleted;
    @ServerTimestamp
    Date timestamp, updateAt;
    DocumentReference user,post;

    public CommentModel() {
    }

    public CommentModel(String comment, String commentID, boolean deleted, Date timestamp, Date updateAt, DocumentReference user, DocumentReference post) {
        this.comment = comment;
        this.commentID = commentID;
        this.deleted = deleted;
        this.timestamp = timestamp;
        this.updateAt = updateAt;
        this.user = user;
        this.post = post;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommentID() {
        return commentID;
    }

    public void setCommentID(String commentID) {
        this.commentID = commentID;
    }


    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }


    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public DocumentReference getUser() {
        return user;
    }

    public void setUser(DocumentReference user) {
        this.user = user;
    }

    public DocumentReference getPost() {
        return post;
    }

    public void setPost(DocumentReference post) {
        this.post = post;
    }

    public Date getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
    }

}
