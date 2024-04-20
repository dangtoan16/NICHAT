package com.example.socialmedia.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmedia.R;
import com.example.socialmedia.adapter.HomeAdapter;
import com.example.socialmedia.adapter.StoryAdapter;
import com.example.socialmedia.chat.HistoryChatActivity;
import com.example.socialmedia.model.NotificationModel;
import com.example.socialmedia.model.PostImageModel;
import com.example.socialmedia.model.Story;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Home extends Fragment {
    private final MutableLiveData<Integer> commentCount = new MutableLiveData<>();
    //RecyclerView storiesRecyclerView;

    HomeAdapter adapter;
    RecyclerView storiesRecyclerView;
    StoryAdapter storyAdapter;
    List<Story> storiesModelList;
    private List<PostImageModel> list;
    private FirebaseUser user;
    Activity activity;
    private FirebaseFirestore db;

    public Home() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        loadRealtimePost();
        loadRealtimeStory();
        adapter.OnPressed(createOnPressedListener());
        view.findViewById(R.id.sendBtn).setOnClickListener(v -> {
            Intent intent= new Intent(getActivity(), HistoryChatActivity.class);
            startActivity(intent);
        });
    }
    private void loadStoryFromFirestore() {
        if (user != null) {
            String currentUserId = user.getUid();

            queryStoriesForFollowedUsers(currentUserId);
            queryStoriesForCurrentUser(currentUserId);

        }
    }
    private void loadRealtimeStory(){
        db.collection("Stories").addSnapshotListener((value, error) -> {
            if (error!=null) {
                Log.e("err", "" + error.getMessage());
                return;
            }
            if (value.isEmpty()){
                Log.e("value", "" + "ko co thong bao");
                return;
            }
            loadStoryFromFirestore();
        });

    }
    private void queryStoriesForCurrentUser(String currentUserId) {
        DocumentReference userRef = db.collection("Users").document(currentUserId);
        db.collection("Stories")
                .whereEqualTo("user", userRef)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("my_app_Home", "so bai post: " + task.getResult().size());
                        storiesModelList.clear();
                        storiesModelList.add(new Story(null,"","",""));
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (!document.exists())
                                return; // or handle the case when the document doesn't exist
                            Story story = document.toObject(Story.class);
                            storiesModelList.add(story);
                        }
                        storyAdapter.notifyDataSetChanged();
                    } else {
                        // Handle errors
                        Log.e("my_app_home", "Error getting documents: " + task.getException());
                    }
                });
    }
    private void queryStoriesForFollowedUsers(String currentUserId) {
        DocumentReference currentUserRef = db.collection("Users").document(currentUserId);
        db.collection("Follows")
                .whereEqualTo("user", currentUserRef)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String targetUserId = document.getDocumentReference("target").getId();
                            queryStoriesForUser(targetUserId);
                        }
                    } else {
                        // Handle errors
                        Log.e("my_app_home", "Error getting documents: " + task.getException());
                    }

                });
    }

    private void queryStoriesForUser(String userId) {
        DocumentReference userRef = db.collection("Users").document(userId);
        db.collection("Stories")
                .whereEqualTo("user",userRef)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (!document.exists())
                                return;
                            Story story = document.toObject(Story.class);
                            storiesModelList.add(story);
                        }
                        storyAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("my_app_home", "Error getting documents: " + task.getException());
                    }
                });
    }

    private void init(View view) {
        db = FirebaseFirestore.getInstance();
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            activity = getActivity();
        }
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        storiesRecyclerView = view.findViewById(R.id.storiesRecyclerView);
        storiesRecyclerView.setHasFixedSize(false);
        storiesRecyclerView
                .setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        storiesModelList = new ArrayList<>();
        storiesModelList.add(new Story(null,"","",""));
        storyAdapter = new StoryAdapter(storiesModelList, getActivity());
        storiesRecyclerView.setAdapter(storyAdapter);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        list = new ArrayList<>();
        adapter = new HomeAdapter(list, getActivity());
        recyclerView.setAdapter(adapter);
    }

    private void loadDataFromFirestore() {
        if (user != null) {
            String currentUserId = user.getUid();

            queryPostsForCurrentUser(currentUserId);
            queryPostsForFollowedUsers(currentUserId);
        }
    }
    private void setUpSortPost(){
        if (!list.isEmpty()) {
            list.sort((post1, post2) -> {
                // Check for null timestamps
                if (post1.getTimestamp() == null || post2.getTimestamp() == null) {
                    return 0; // Return 0 to indicate equality if any timestamp is null
                }
                return post2.getTimestamp().compareTo(post1.getTimestamp());
            });
        }
    }
    private void loadRealtimePost(){
        db.collection("Posts").addSnapshotListener((value, error) -> {
            if (error!=null) {
                Log.e("err", "" + error.getMessage());
                return;
            }
            if (value.isEmpty()){
                Log.e("value", "" + "ko co thong bao");
                return;
            }
            loadDataFromFirestore();
        });

    }
    private void queryPostsForCurrentUser(String currentUserId) {
        DocumentReference userRef = db.collection("Users").document(currentUserId);
        db.collection("Posts").whereEqualTo("deleted",false)
                .whereEqualTo("userOwnerOfPost", userRef)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("my_app_Home", "so bai post: " + task.getResult().size());
                        list.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (!document.exists())
                                return; // or handle the case when the document doesn't exist
                            PostImageModel post = document.toObject(PostImageModel.class);
                            list.add(post);
                            setUpSortPost();
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        // Handle errors
                        Log.e("my_app_home", "Error getting documents: " + task.getException());
                    }
                });
    }

    private void queryPostsForFollowedUsers(String currentUserId) {
        DocumentReference currentUserRef = db.collection("Users").document(currentUserId);
        db.collection("Follows")
                .whereEqualTo("user", currentUserRef)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String targetUserId = document.getDocumentReference("target").getId();
                            queryPostsForUser(targetUserId);
                        }
                    } else {
                        // Handle errors
                        Log.e("my_app_home", "Error getting documents: " + task.getException());
                    }

                });
    }

    private void queryPostsForUser(String userId) { // lấy post của những người đã follow
        DocumentReference userRef = db.collection("Users").document(userId);
        db.collection("Posts").whereEqualTo("deleted",false)
                .whereEqualTo("userOwnerOfPost", userRef)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (!document.exists())
                                return; // or handle the case when the document doesn't exist
                            PostImageModel post = document.toObject(PostImageModel.class);
                            list.add(post);
                            setUpSortPost();
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        // Handle errors
                        Log.e("my_app_home", "Error getting documents: " + task.getException());
                    }
                });
    }


    public HomeAdapter.OnPressed createOnPressedListener() {
        return new HomeAdapter.OnPressed() {
            @Override
            public void onLiked(PostImageModel currentPost, boolean isNotChecked) {
                DocumentReference currentUserRef = db.collection("Users").document(user.getUid());
                Log.e("ping","call?");
                if (!isNotChecked) {
                    currentUserUnlikeCurrentPost(currentUserRef, currentPost.getPostReference());
                } else {
                    currentUserLikePost(currentUserRef, currentPost.getPostReference());
                    createLikeNotification(currentPost);

                }
            }


            private void currentUserUnlikeCurrentPost(DocumentReference currentUserRef, DocumentReference currentPostref) {
                db.collection("LikePosts")
                        .whereEqualTo("user", currentUserRef)
                        .whereEqualTo("post", currentPostref)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    // Assuming "document" is the document you want to delete
                                    document.getReference().delete()
                                            .addOnSuccessListener(aVoid -> {
                                                // Document successfully deleted
                                                Log.d("my_app_home", "DocumentSnapshot successfully deleted!");
                                            })
                                            .addOnFailureListener(e -> {
                                                // Handle any errors
                                                Log.w("my_app_home", "Error deleting document", e);
                                            });
                                }
                            } else {
                                Log.d("my_app_home", "Error getting documents: ", task.getException());
                            }
                        });
            }
                void createLikeNotification (PostImageModel currentPost){
                    if (user.getUid().equals(currentPost.getUserOwnerOfPost().getId())) return;
                    DocumentReference currentUserRef = db.collection("Users").document(user.getUid());
                    CollectionReference NotificationsRef = FirebaseFirestore.getInstance().collection("Notifications");
                    String newNotificationId = NotificationsRef.document().getId();
                    Map<String, Object> map = new HashMap<>();
                    map.put("target", currentPost.getUserOwnerOfPost());
                    map.put("user", currentUserRef);
                    map.put("notification", user.getDisplayName() + currentPost.makeShortLikeDescription());
                    map.put("time", FieldValue.serverTimestamp());
                    map.put("id", newNotificationId);
                    NotificationsRef.document(newNotificationId).set(map);
                }

                @Override
                public void setCommentCount (TextView textView){
                    commentCount.observe((LifecycleOwner) activity, integer -> {
                        assert commentCount.getValue() != null;
                        if (commentCount.getValue() == 0) {
                            textView.setVisibility(View.GONE);
                        } else
                            textView.setVisibility(View.VISIBLE);
                        StringBuilder builder = new StringBuilder();
                        builder.append("See all")
                                .append(commentCount.getValue())
                                .append("comment");
                        textView.setText(builder);
                    });
                }

                void currentUserLikePost (DocumentReference currentUserRef, DocumentReference
                currentPostref){
                    // User has not liked the post yet, so create a new LikePosts document
                    Map<String, Object> likeData = new HashMap<>();
                    likeData.put("user", currentUserRef);
                    likeData.put("post", currentPostref);

                    db.collection("LikePosts")
                            .add(likeData)
                            .addOnSuccessListener(documentReference -> Log.d("my_app_home", "New LikePosts document added with ID: " + documentReference.getId()))
                            .addOnFailureListener(e -> Log.e("my_app_home", "Error adding LikePosts document", e));
                }
        }

        ;
    }
}







