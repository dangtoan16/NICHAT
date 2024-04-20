package com.example.socialmedia.fragments;


import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmedia.MainActivity;
import com.example.socialmedia.R;
import com.example.socialmedia.ReplaceActivity;
import com.example.socialmedia.adapter.GalleryAdapter;
import com.example.socialmedia.model.GalleryImages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Add extends Fragment {

    private EditText descET;
    Dialog dialog;
    private ImageView imageView;
    private RecyclerView recyclerView;
    private ImageButton nextBtn;
    private List<GalleryImages> list;
    private GalleryAdapter adapter;
    Uri imageUri;
    FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
    OnDataPass onDataPass;

    public Add() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setHasFixedSize(false);

        list = new ArrayList<>();
        adapter = new GalleryAdapter(list);
        recyclerView.setAdapter(adapter);
        clickListener();
    }

    private void clickListener() {
        adapter.SendImage(picUri ->

                CropImage.activity(picUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(6, 4)
                        .start(getContext(), Add.this));

        nextBtn.setOnClickListener(v -> {

            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageReference = storage.getReference().child("Post_Images/" + System.currentTimeMillis());
            dialog.show();
            storageReference.putFile(imageUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> uploadData(uri.toString()));

                } else {
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Upload post failed"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                onDataPass.onChange(true);
            });
        });

    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        onDataPass = (OnDataPass) context;

    }
    public interface OnDataPass {
        void onChange( boolean returnToHome);

    }

    private void uploadData(String imageURL) {
        CollectionReference reference = FirebaseFirestore.getInstance().collection("Posts");
        DocumentReference userRef= FirebaseFirestore.getInstance().collection("Users").document(user.getUid());
        String id = reference.document().getId();
        String description = descET.getText().toString();
        Map<String, Object> map = new HashMap<>();
        map.put("postId",id);
        map.put("userOwnerOfPost",userRef);
        map.put("description", description);
        map.put("deleted",false);
        map.put("timestamp", FieldValue.serverTimestamp());
        map.put("postImageUrl", imageURL);

        reference.document(id).set(map)
                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Toast.makeText(getContext(), "Updated succesfully", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(getContext(), "Error:" + task.getException().getMessage()
//                                , Toast.LENGTH_SHORT).show();
//                    }
                    dialog.dismiss();
                });

    }

    private void init(View view) {
        descET = view.findViewById(R.id.descriptionET);
        imageView = view.findViewById(R.id.imageView);
        recyclerView = view.findViewById(R.id.recyclerView);
        ImageButton backBtn = view.findViewById(R.id.backBtn);
        nextBtn = view.findViewById(R.id.nextBtn);
        user = FirebaseAuth.getInstance().getCurrentUser();
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.loading_dialog);
        dialog.getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.dialog_bg, null));
        dialog.setCancelable(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().runOnUiThread(() -> Dexter.withContext(getContext())
                .withPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            File file = new File(Environment.getExternalStorageDirectory().toString() + "/Download");
                            if (file.exists()) {
                                File[] files = file.listFiles();
                                assert files != null;
                                for (File file1 : files) {
                                    if (file1.getAbsolutePath().endsWith(".jpg") || file1.getAbsolutePath().endsWith(".png")) {
                                        list.add(new GalleryImages(Uri.fromFile(file1)));
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                    }
                }).check());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                Glide.with(getContext())
                        .load(imageUri)
                        .into(imageView);
                imageView.setVisibility(View.VISIBLE);
                nextBtn.setVisibility(View.VISIBLE);
            }
        }
    }
}