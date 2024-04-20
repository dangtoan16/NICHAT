package com.example.socialmedia.model;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ExecutionException;

public class FollowModel {
    DocumentReference target,user;

    public FollowModel() {
    }

    public FollowModel(DocumentReference target, DocumentReference user) {
        this.target = target;
        this.user = user;
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
    public Users getUserModel(){
        String userId = this.user.getId();

        FirebaseFirestore refenrence= FirebaseFirestore.getInstance();
        Task<DocumentSnapshot> task = refenrence.collection("Users")
                .document(userId).get();
        try {
            // Wait synchronously for the task to complete
            Tasks.await(task);
        } catch ( InterruptedException | ExecutionException e) {
            Log.e("my_app_FollowModel",e.getMessage()); // Handle the exception appropriately
            return null;
        }
        if (task.isSuccessful()) {
            DocumentSnapshot document = task.getResult();
            if (!document.exists())
                return null; // or handle the case when the document doesn't exist
            return document.toObject(Users.class);
        }
        return null;
    }
    public Users getTargetModel(){
        String targetId = this.target.getId();

        FirebaseFirestore refenrence= FirebaseFirestore.getInstance();
        Task<DocumentSnapshot> task = refenrence.collection("Users")
                .document(targetId).get();
        try {
            // Wait synchronously for the task to complete
            Tasks.await(task);
        } catch ( InterruptedException | ExecutionException e) {
            Log.e("my_app_FollowModel",e.getMessage()); // Handle the exception appropriately
            return null;
        }

        return null;
    }
}
