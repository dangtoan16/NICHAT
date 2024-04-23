package com.example.socialmedia.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmedia.MainActivity;
import com.example.socialmedia.MenuProfileActivity;
import com.example.socialmedia.OtherProfileActivity;
import com.example.socialmedia.R;
import com.example.socialmedia.ReplaceActivity;
import com.example.socialmedia.UserArray;
import com.example.socialmedia.adapter.HomeAdapter;
import com.example.socialmedia.adapter.UserAdapter;
import com.example.socialmedia.model.FollowModel;
import com.example.socialmedia.model.PostImageModel;
import com.example.socialmedia.model.Users;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class Profile extends Fragment  {

    private TextView nameTv, toolbarNameTv, statusTv, followingCountTv, followersCountTv, postCountTv;
    private CircleImageView profileImage;
    private RecyclerView recyclerView;

    private LinearLayout postLiner, folowingLiner, folowerLiner;
    private FirebaseUser user;
    private ImageButton editProfileBtn, menuProfileBtn;
    int count;
    FirebaseFirestore db;
    UserArray listFollowing;
    UserArray listFollower;
    UserAdapter userAdapter;
    FirestoreRecyclerAdapter<PostImageModel, PostImageHolder> adapter;

    public Profile() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        loadBasicData();
        realtimeData();
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(getContext(), 3));
        loadPostImages();
        recyclerView.setAdapter(adapter);
        selectTab();
        clickListenerWhenFollow();
        clickListener();
    }
    private void clickListenerWhenFollow(){
        userAdapter.OnUserClicked(new UserAdapter.OnUserClicked() {
            @Override
            public void onClicked(String uid) {
                Intent intent= new Intent(getActivity(), OtherProfileActivity.class);
                intent.putExtra("User need to find",uid);
                startActivity(intent );
            }
        });
    }
    private void realtimeData(){
        countFollowers(folowers -> followersCountTv.setText(String.valueOf(folowers)));
        countFollow(folowings -> followingCountTv.setText(String.valueOf(folowings)));
    }
    public void selectTab(){
        postLiner.setOnClickListener(v -> {
            recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(getContext(), 3));
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        });
        folowerLiner.setOnClickListener(v -> {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            userAdapter.setList(listFollower.getUserArray());
            Log.d("ng noi tieng",String.valueOf(listFollower.size()));
            recyclerView.setAdapter(userAdapter);
            userAdapter.notifyDataSetChanged();
        });
        folowingLiner.setOnClickListener(v -> {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            userAdapter.setList(listFollowing.getUserArray());
            Log.d("ng noi tieng1",String.valueOf(listFollowing.size()));
            recyclerView.setAdapter(userAdapter);
            userAdapter.notifyDataSetChanged();
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
                Log.e("TAG", "meet a IOOBE in RecyclerView");
            }
        }
    }


    private void clickListener() {

        editProfileBtn.setOnClickListener(v -> CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(getContext(), Profile.this));
        menuProfileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MenuProfileActivity.class);
            startActivity(intent);
        });
    }

    private void init(View view) {
        db = FirebaseFirestore.getInstance();
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        assert getActivity() != null;
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        nameTv = view.findViewById(R.id.nameTv);
        statusTv = view.findViewById(R.id.statusTV);
        toolbarNameTv = view.findViewById(R.id.toolbarNameTV);
        followersCountTv = view.findViewById(R.id.followersCountTv);
        followingCountTv = view.findViewById(R.id.followingCountTv);
        postCountTv = view.findViewById(R.id.postCountTv);
        profileImage = view.findViewById(R.id.profileImage);
        recyclerView = view.findViewById(R.id.recyclerView);
        editProfileBtn = view.findViewById(R.id.edit_profileImage);
        user = FirebaseAuth.getInstance().getCurrentUser();
        listFollowing = new UserArray();
        listFollower = new UserArray();
        userAdapter = new UserAdapter(listFollowing.getUserArray());
        menuProfileBtn= view.findViewById(R.id.menuBtn);
        postLiner= view.findViewById(R.id.postLiner);
        folowerLiner= view.findViewById(R.id.followerLiner);
        folowingLiner= view.findViewById(R.id.followingLiner);
    }

    private void countFollow(Profile.onCountFollowCompleteListener listener) {
        DocumentReference currentUserRef = db.collection("Users").document(user.getUid());
        db.collection("Follows")
                .whereEqualTo("user", currentUserRef)
                .addSnapshotListener((value, error) -> {
                    if (error!=null) {
                        Log.e("err", "" + error.getMessage());
                        return;
                    }
                    if (value.isEmpty()){
                        Log.e("value", "" + "ko co thong bao");
                        return;
                    }
                        listFollowing.clear();
                        for (DocumentSnapshot document : value.getDocuments()) {
                            if (!document.exists())
                                continue;
                            FollowModel following = document.toObject(FollowModel.class);
                            addTargetToFollowingList(following.getTarget());
                        }
                    if (listener != null) {
                        listener.onCountFollowComplete(value.getDocuments().size());
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
                listFollowing.add(document.toObject(Users.class));
            }
        });
    }

    private void countFollowers(Profile.onCountFollowCompleteListener listener) {
        DocumentReference currentUserRef = db.collection("Users").document(user.getUid());
        db.collection("Follows")
                .whereEqualTo("target", currentUserRef)
                .addSnapshotListener((value, error) -> {
                    if (error!=null) {
                        Log.e("err", "" + error.getMessage());
                        return;
                    }
                    if (value.isEmpty()){
                        Log.e("value", "" + "ko co thong bao");
                        return;
                    }
                    listFollower.clear();
                    for (DocumentSnapshot document : value.getDocuments()) {
                        if (!document.exists())
                            continue;
                        FollowModel following = document.toObject(FollowModel.class);
                        addUserToFollowerList(following.getUser());
                    }
                    if (listener != null) {

                        listener.onCountFollowComplete(value.getDocuments().size());

                    }
                });
    }

    private void addUserToFollowerList(DocumentReference userRef) {
        String userId = userRef.getId();
        db.collection("Users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (!document.exists())
                    return; // or handle the case when the document doesn't exist
                Users user = document.toObject(Users.class);
                Log.d("batloi", user.toString());
                listFollower.add(user);
            }
        });
    }

    public interface onCountFollowCompleteListener {
        void onCountFollowComplete(int folowings);
    }

    private void loadBasicData() {
        DocumentReference currentUserRef = FirebaseFirestore.getInstance().collection("Users").document(user.getUid());
        currentUserRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("my_app_profile_loadBasicData", error.getMessage());
                return;
            }

            if (value == null || !value.exists()) return;
            String name = value.getString("name");
            String status = value.getString("status");
            final String profileURL = value.getString("profileImageUrl");
            nameTv.setText(name);
            toolbarNameTv.setText(name);
            statusTv.setText(status);

            if (getActivity()==null) return;
            Glide.with(getActivity())
                    .load(profileURL)
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .timeout(4500)
                    .into(profileImage);

        });
    }

    private void loadPostImages() {
        DocumentReference currentUserRef = FirebaseFirestore.getInstance().collection("Users").document(user.getUid());
        Query query = FirebaseFirestore.getInstance().collection("Posts").whereEqualTo("userOwnerOfPost", currentUserRef);
        FirestoreRecyclerOptions<PostImageModel> options = new FirestoreRecyclerOptions.Builder<PostImageModel>()
                .setQuery(query, PostImageModel.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<PostImageModel, PostImageHolder>(options) {
            @NonNull
            @Override
            public PostImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_image_item, parent, false);
                return new PostImageHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull PostImageHolder holder, int position, @NonNull PostImageModel model) {
                Glide.with(holder.itemView.getContext().getApplicationContext())
                        .load(model.getPostImageUrl())
                        .timeout(4500)
                        .into(holder.imageView);
                count = getItemCount();
                postCountTv.setText(String.valueOf(count));
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

    public static class PostImageHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

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
            uploadImage(uri);

        }
    }

    private void uploadImage(Uri uri) {
        final StorageReference reference = FirebaseStorage.getInstance().getReference().child("Profile_Images").child("profileImage_" + user.getUid());
        reference.putFile(uri)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        reference.getDownloadUrl()
                                .addOnSuccessListener(uri1 -> {
                                    String imageURL = uri1.toString();
                                    UserProfileChangeRequest.Builder request = new UserProfileChangeRequest.Builder();
                                    request.setPhotoUri(uri1);

                                    user.updateProfile(request.build());

                                    Map<String, Object> map = new HashMap<>();
                                    map.put("profileImageUrl", imageURL);
                                    FirebaseFirestore.getInstance().collection("Users")
                                            .document(user.getUid())
                                            .update(map).addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    Toast.makeText(getContext(), "Updated Successful", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    if (task1.getException() != null)
                                                        Log.e("my_app_Profile_UploadImage", "Error: " + task1.getException().getMessage());

                                                }
                                            });

                                });
                    } else {
                        if (task.getException() != null)
                            Log.e("my_app_Profile_UploadImage", "Error: " + task.getException().getMessage());

                    }
                });

    }
}