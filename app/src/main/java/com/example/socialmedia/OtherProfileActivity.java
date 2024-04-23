package com.example.socialmedia;

import static android.app.PendingIntent.getActivity;
import static java.security.AccessController.getContext;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.example.socialmedia.chat.MessageActivity;
import com.example.socialmedia.fragments.Profile;
import com.example.socialmedia.model.FollowModel;
import com.example.socialmedia.model.PostImageModel;
import com.example.socialmedia.model.Users;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.marsad.stylishdialogs.StylishAlertDialog;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class OtherProfileActivity extends ActiveActivity {
    private TextView nameTv, toolbarNameTv, statusTv, followingCountTv, followersCountTv, postCountTv;
    private CircleImageView profileImage;
    private Button followBtn, startChatBtn;
    private ImageButton backBtn;
    private RecyclerView recyclerView;
    private FirebaseUser user;
    boolean isFollwed;
    DocumentReference userBeingViewedRef, currentUserRef;
    int count;
    FirestoreRecyclerAdapter<PostImageModel, Profile.PostImageHolder> adapter;
    FirebaseFirestore db;
    UserArray listFollowing;
    UserArray listFollower;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_other_profile);
        init();
        loadBasicData();
        realtimeData();
        listenToDataBase();
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this, 3));
        loadPostImages();
        recyclerView.setAdapter(adapter);
        clickListener();
    }

    private void listenToDataBase() {
        db.collection("Follows").addSnapshotListener((value, e) -> {
            if (e != null) {
                Log.e("my_app_otherprofile_listener", "Listen failed.", e);
                return;
            }

            for (DocumentChange dc : value.getDocumentChanges()) {
                switch (dc.getType()) {
                    case ADDED:
                    case REMOVED:
                        countFollowers(folowers -> followersCountTv.setText(String.valueOf(folowers)));
                        countFollow(folowings -> followingCountTv.setText(String.valueOf(folowings)));
                        break;
                }
            }
        });
    }

    public class WrapContentLinearLayoutManager extends GridLayoutManager {

        public WrapContentLinearLayoutManager(Context context, int spanCount) {
            super(context, spanCount);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                Log.e("my_app_otherprofile_WrapCont", "meet a IOOBE in RecyclerView");
            }
        }
    }

    private void clickListener() {
        followBtn.setOnClickListener(v -> {
            if (isFollwed)
                setToUnfollow();
            else
                setToFollow();
        });
        backBtn.setOnClickListener(v -> finish());


        startChatBtn.setOnClickListener(v -> {queryChat();});
    }

    private void setToFollow() {
        Map<String, Object> likeData = new HashMap<>();
        likeData.put("user", currentUserRef);
        likeData.put("target", userBeingViewedRef);

        db.collection("Follows")
                .add(likeData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("my_app_other_setFollow", "New Follows document added with ID: " + documentReference.getId());
                    Toast.makeText(this, "Followed", Toast.LENGTH_SHORT).show();
                    createFolowNotification();

                })
                .addOnFailureListener(e -> Log.e("my_app_other_setFollow", "Error adding Follows document", e));
    }

    private void setToUnfollow() {
        FirebaseFirestore.getInstance().collection("Follows")
                .whereEqualTo("target", userBeingViewedRef)
                .whereEqualTo("user", currentUserRef).limit(1).get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("my_app_otherProfileSetUnfollow", "Error getting documents: " + task.getException());
                        return;
                    }
                    QuerySnapshot querySnapshot = task.getResult();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        document.getReference().delete()
                                .addOnSuccessListener(aVoid ->
                                {
                                    Log.d("my_app_otherProfileSetUnfollow", "Follow document successfully deleted!");
                                    updateFollowingStatus(null,false)
                                ;})
                                .addOnFailureListener(e -> Log.e("my_app_otherProfileSetUnfollow", "Error deleting LikePosts document", e));
                    }

                });
    }
    void queryChat(){
        assert getContext() != null;
        StylishAlertDialog alertDialog= new StylishAlertDialog(this,StylishAlertDialog.PROGRESS);
        alertDialog.setTitleText("Starting chat...");
        alertDialog.setCancelable(false);
        alertDialog.show();

        CollectionReference reference = FirebaseFirestore.getInstance().collection("Messages");
        DocumentReference userRef = db.collection("Users").document(user.getUid());
        reference.whereArrayContains("user", userRef)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        QuerySnapshot snapshot= task.getResult();
                        if(snapshot.isEmpty()){
                            startChat(alertDialog);
                        }else{
                            alertDialog.dismissWithAnimation();
                            for(DocumentSnapshot snapshotChat: snapshot){
                                Intent intent=new Intent(getApplicationContext(), MessageActivity.class);
                                intent.putExtra(("uid"),userBeingViewedRef.getId());
                                intent.putExtra("id",snapshotChat.getId());
                                startActivity(intent);
                            }
                        }
                    }else
                        alertDialog.dismissWithAnimation();
                });
    }
    void startChat(StylishAlertDialog alertDialog){
        List<String> list= new ArrayList<>();
        list.add(user.getUid());
        list.add(userBeingViewedRef.getId());
        Collections.sort(list);
        FirebaseFirestore.getInstance().collection("Messages")
                .whereEqualTo("uid",list).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                       createNewChat(list,alertDialog);
                    } else {
                        Log.e("my_app_chat", "Error getting documents: " + task.getException());
                    }

                });

    }

    private void createNewChat(List<String> list,StylishAlertDialog alertDialog) {
        CollectionReference reference= FirebaseFirestore.getInstance().collection("Messages");
        String pushID= list.get(0)+list.get(1);
        Map<String,Object> map= new HashMap<>();
        map.put("id",pushID);
        map.put("lastMessage","Hi");
        map.put("time", FieldValue.serverTimestamp());
        map.put("uid",list);
        reference.document(pushID).update(map).addOnCompleteListener(task -> {
            if (!task.isSuccessful()){
                reference.document(pushID).set(map);
            }
        });
        CollectionReference messageRef= FirebaseFirestore.getInstance().collection("Messages")
                .document(pushID).collection("Messages");
        String messageID= messageRef.document().getId();
        Map<String,Object> messageMap= new HashMap<>();
        messageMap.put("id", messageID);
        messageMap.put("message","Hi");
        messageMap.put("senderID", user.getUid());
        messageMap.put("time",FieldValue.serverTimestamp());
        messageRef.document(messageID).set(messageMap);
        new Handler().postDelayed(()->{
            alertDialog.dismissWithAnimation();
            Intent intent = new Intent(this, MessageActivity.class);
            intent.putExtra("uid",userBeingViewedRef.getId());
            intent.putExtra("id", pushID);
            startActivity(intent);
        },2500);
    }

    private void updateChat(List<String> list,StylishAlertDialog alertDialog) {
        CollectionReference reference= FirebaseFirestore.getInstance().collection("Messages");
        String pushID= reference.document().getId();
        Map<String,Object> map= new HashMap<>();
        map.put("id",pushID);
        map.put("lastMessage","Hi");
        map.put("time", FieldValue.serverTimestamp());
        map.put("uid",list);
        reference.document(pushID).update(map).addOnCompleteListener(task -> {
            if (!task.isSuccessful()){
                reference.document(pushID).set(map);
            }
        });
        CollectionReference messageRef= FirebaseFirestore.getInstance().collection("Messages")
                .document(pushID).collection("Messages");
        String messageID= messageRef.document().getId();
        Map<String,Object> messageMap= new HashMap<>();
        messageMap.put("id", messageID);
        messageMap.put("message","Hi");
        messageMap.put("senderID", user.getUid());
        messageMap.put("time",FieldValue.serverTimestamp());
        messageRef.document(messageID).set(messageMap);
        new Handler().postDelayed(()->{
            alertDialog.dismissWithAnimation();
            Intent intent = new Intent(this, MessageActivity.class);
            intent.putExtra("uid",userBeingViewedRef.getId());
            intent.putExtra("id", pushID);
            startActivity(intent);
        },2500);
    }


    private void init() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarNameTv = findViewById(R.id.toolbarNameTV);
        nameTv = findViewById(R.id.nameTv);
        statusTv = findViewById(R.id.statusTV);
        followersCountTv = findViewById(R.id.followersCountTv);
        followingCountTv = findViewById(R.id.followingCountTv);
        postCountTv = findViewById(R.id.postCountTv);
        profileImage = findViewById(R.id.profileImage);
        followBtn = findViewById(R.id.followBtn);
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayout countLayout = findViewById(R.id.countLayout);
        startChatBtn = findViewById(R.id.startChatBtn);
        backBtn = findViewById(R.id.backBtn);
        user = FirebaseAuth.getInstance().getCurrentUser();
        listFollowing = new UserArray();
        listFollower = new UserArray();
        Intent intent = getIntent();
        String currentViewUserId = intent.getStringExtra("User need to find");
        db = FirebaseFirestore.getInstance();
        if (currentViewUserId != null) {

            userBeingViewedRef = FirebaseFirestore.getInstance().collection("Users").document(currentViewUserId);
        }else {
            Log.e("my_app_otherprofile_init", "không có intent");
        }
        currentUserRef = FirebaseFirestore.getInstance().collection("Users").document(user.getUid());
    }

    private void countFollow(Profile.onCountFollowCompleteListener listener) {

        db.collection("Follows")
                .whereEqualTo("user", userBeingViewedRef)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listFollowing.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (!document.exists())
                                continue; // or handle the case when the document doesn't exist
                            FollowModel following = document.toObject(FollowModel.class);
                            addTargetToFollowingList(following.getTarget());
                        }
                    } else {
                        // Handle errors
                        Log.e("my_app_otherprofile_following", "Error getting documents: " + task.getException());
                    }
                    if (listener != null) {
                        listener.onCountFollowComplete(task.getResult().size());
                    }
                });
    }

    private void addTargetToFollowingList(DocumentReference targetRef) {
        String targetId = targetRef.getId();

        db.collection("Users").document(targetId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (!document.exists())
                    return; // or handle the case when the document doesn't exist
                listFollowing.add( document.toObject(Users.class) );
            }
        });
    }

    private void countFollowers(Profile.onCountFollowCompleteListener listener) {
        db.collection("Follows")
                .whereEqualTo("target", userBeingViewedRef)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listFollower.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (!document.exists())
                                continue; // or handle the case when the document doesn't exist
                            FollowModel following = document.toObject(FollowModel.class);
                            addUserToFollowerList(following.getUser());
                        }
                    } else {
                        // Handle errors
                        Log.e("my_app_otherprofile_following", "Error getting documents: " + task.getException());
                    }
                    if (listener != null) {

                        listener.onCountFollowComplete(task.getResult().size());

                    }
                });
    }

    private void addUserToFollowerList(DocumentReference userRef) {
        String userId = userRef.getId();

        db.collection("Users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (!document.exists())
                    return;
                Users user = document.toObject(Users.class);
                Log.d("my_app_Other_add",user.toString());
                listFollower.add(user);
                updateFollowingStatus(listFollower,true);
            }
        });
    }
    private void realtimeData(){
        countFollowers(folowers -> followersCountTv.setText(String.valueOf(folowers)));
        countFollow(folowings -> followingCountTv.setText(String.valueOf(folowings)));
    }
    private void loadBasicData() {
        userBeingViewedRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("my_app_others_profile_loadBasicData", error.getMessage());
                return;
            }

            if (value == null || !value.exists()) return;
            String name = value.getString("name");
            String status = value.getString("status");
            final String profileURL = value.getString("profileImageUrl");


            nameTv.setText(name);
            toolbarNameTv.setText(name);
            statusTv.setText(status);

            try {
                Glide.with(getApplicationContext())
                        .load(profileURL)
                        .placeholder(R.drawable.ic_person)
                        .circleCrop()
                        .timeout(4500)
                        .into(profileImage);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    private void updateFollowingStatus(UserArray list,boolean update) {
        isFollwed = update && list.contains(user);
        if (isFollwed) {
            followBtn.setText("UnFollow");
            startChatBtn.setVisibility(View.VISIBLE);
        } else {
            followBtn.setText("Follow");
            startChatBtn.setVisibility(View.GONE);
        }
    }


    private void loadPostImages() {
        Query query = FirebaseFirestore.getInstance().collection("Posts").whereEqualTo("userOwnerOfPost", userBeingViewedRef);
        FirestoreRecyclerOptions<PostImageModel> options = new FirestoreRecyclerOptions.Builder<PostImageModel>()
                .setQuery(query, PostImageModel.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<PostImageModel, Profile.PostImageHolder>(options) {
            @NonNull
            @Override
            public Profile.PostImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_image_item, parent, false);
                return new Profile.PostImageHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull Profile.PostImageHolder holder, int position, @NonNull PostImageModel model) {
                Glide.with(holder.itemView.getContext().getApplicationContext())
                        .load(model.getPostImageUrl())
                        .timeout(4500)
                        .into(holder.imageView);
                count = getItemCount();
                postCountTv.setText(String.valueOf( count));
            }

            @Override
            public int getItemCount() {
                return super.getItemCount();
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    void createFolowNotification() {
        CollectionReference NotificationsRef = FirebaseFirestore.getInstance().collection("Notifications");
        String newNotificationId = NotificationsRef.document().getId();
        Map<String, Object> map = new HashMap<>();
        map.put("target", userBeingViewedRef);
        map.put("user", currentUserRef);
        map.put("notification", user.getDisplayName() + " followed you");
        map.put("time", FieldValue.serverTimestamp());
        map.put("id", newNotificationId);
        NotificationsRef.document(newNotificationId).set(map);
    }

    private static class PostImageHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public PostImageHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (result == null) return;
            Uri uri = result.getUri();
        }
    }

}