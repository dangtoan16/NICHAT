package com.example.socialmedia.model;

import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class PostImageModel {
    private String postId, postImageUrl, description;
    DocumentReference userOwnerOfPost;
    boolean deleted;
    @ServerTimestamp
    private Date timestamp;

    public PostImageModel() {

    }

    public PostImageModel(String postId, String postImageUrl, String description, DocumentReference userOwnerOfPost, boolean deleted, Date timestamp) {
        this.postId = postId;
        this.postImageUrl = postImageUrl;
        this.description = description;
        this.userOwnerOfPost = userOwnerOfPost;
        this.deleted = deleted;
        this.timestamp = timestamp;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostImageUrl() {
        return postImageUrl;
    }

    public void setPostImageUrl(String postImageUrl) {
        this.postImageUrl = postImageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DocumentReference getUserOwnerOfPost() {
        return userOwnerOfPost;
    }

    public void setUserOwnerOfPost(DocumentReference userOwnerOfPost) {
        this.userOwnerOfPost = userOwnerOfPost;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void getName(NameFetchListener listener) {
        if (userOwnerOfPost != null) {
            userOwnerOfPost.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("name");
                        if (listener != null) {
                            listener.onNameFetch(name);
                        }
                    } else {
                        Log.e("PostImageModel", "No such document");
                        if (listener != null) {
                            listener.onNameFetch("name not found");
                        }
                    }
                } else {
                    Log.e("PostImageModel", "Failed with: " + task.getException());
                    if (listener != null) {
                        listener.onNameFetch("name not found");
                    }
                }
            });
        }
    }
    public DocumentReference getPostReference(){
        return FirebaseFirestore.getInstance().collection("Posts").document(postId);
    }
    public void getProfileImage(OnProfileImageLoadCompleteListener listener) {
        userOwnerOfPost.get().addOnCompleteListener(task -> {
            String profileImageUrl = null;
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    profileImageUrl = document.getString("profileImageUrl");
                }
            }
            if (listener != null) {
                listener.onProfileImageLoadComplete(profileImageUrl);
            }
        });
    }

    public String makeShortLikeDescription() {
        String text = description;
        if( description.length() > 30){
            text = description.substring(0,29);
        }
        return  " liked your post \""+ text+"\"...";
    }

    public interface OnProfileImageLoadCompleteListener {
        void onProfileImageLoadComplete(String profileImageUrl);
    }


    public interface NameFetchListener {
        void onNameFetch(String name);
    }

    public void getLikesCount(LikesCountFetchListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference currentPostRef = db.collection("Posts").document(postId);
        // Query the "LikePosts" collection for likes related to the provided postId
        db.collection("LikePosts")
                .whereEqualTo("post", currentPostRef)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Count the number of likes
                        int likesCount = task.getResult().size();
                        if (listener != null) {
                            listener.onLikesCountFetch(likesCount);
                        }
                    } else {
                        Log.e("PostImageModel", "Failed with: " + task.getException());
                        if (listener != null) {
                            listener.onLikesCountFetch(0);
                        }
                    }
                });
    }

    public interface LikesCountFetchListener {
        void onLikesCountFetch(int count);
    }

    public void CheckLikedPost(CheckLikePostListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference currentPostRef = db.collection("Posts").document(postId);
        db.collection("LikePosts")
                .whereEqualTo("post", currentPostRef)
                .whereEqualTo("user", userOwnerOfPost).limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isContained = !task.getResult().isEmpty();
                        if (listener != null) {
                            listener.CheckLikedPost(isContained);
                        }
                    } else {
                        Log.e("PostImageModel", "Failed with: " + task.getException());
                        if (listener != null) {
                            listener.CheckLikedPost(false);
                        }
                    }
                });
    }

    public interface CheckLikePostListener {
        void CheckLikedPost(boolean b);
    }
}
