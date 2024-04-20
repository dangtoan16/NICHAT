package com.example.socialmedia.model;

import com.google.firebase.firestore.DocumentReference;

public class LikePostModel {
    DocumentReference user, post;

    public LikePostModel() {
    }

    public LikePostModel(DocumentReference user, DocumentReference post) {

        this.user = user;
        this.post = post;
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
}
