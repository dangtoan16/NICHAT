package com.example.socialmedia.adapter;

import static com.example.socialmedia.ViewStoryActivity.FILE_TYPE;
import static com.example.socialmedia.ViewStoryActivity.VIDEO_URL_KEY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmedia.R;
import com.example.socialmedia.StoryAddActivity;
import com.example.socialmedia.ViewStoryActivity;
import com.example.socialmedia.model.Story;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoriesHolder> {

    List<Story> list;
    Activity activity;
    FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();

    public StoryAdapter(List<Story> list, Activity activity) {
        this.list = list;

        this.activity = activity;
    }

    @NonNull
    @Override
    public StoriesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.story_layout, parent, false);
        return new StoriesHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryAdapter.StoriesHolder holder, @SuppressLint("RecyclerView") int position) {
        if (position ==0) {

            Glide.with(activity)
                    .load(activity.getResources().getDrawable(R.drawable.ic_add_story))
                    .into(holder.imageView);
            holder.imageView.setScaleX(0.5f);
            holder.imageView.setScaleY(0.5f);
            holder.textView.setVisibility(View.GONE);
            holder.cancelBtn.setVisibility(View.GONE);
            holder.imageView.setOnClickListener(v ->
                    activity.startActivity(new Intent(activity, StoryAddActivity.class)));

        }else {

            Glide.with(activity)
                    .load(list.get(position).getUrl())
                    .timeout(6500)
                    .into(holder.imageView);

            holder.imageView.setOnClickListener(v -> {

                if (holder.getAbsoluteAdapterPosition() == 0) {
                    //new story

                    Dexter.withContext(activity)
                            .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .withListener(new MultiplePermissionsListener() {
                                @Override
                                public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                                    if (multiplePermissionsReport.areAllPermissionsGranted()) {

                                        activity.startActivity(new Intent(activity, StoryAddActivity.class));

                                    } else {
                                        Toast.makeText(activity, "Please allow permission from settings.", Toast.LENGTH_SHORT).show();
                                    }

                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                                    permissionToken.continuePermissionRequest();
                                }
                            }).check();

                } else {
                    //open story
                    Intent intent = new Intent(activity, ViewStoryActivity.class);
                    intent.putExtra(VIDEO_URL_KEY, list.get(position).getUrl());
                    intent.putExtra(FILE_TYPE, list.get(position).getType());
                    activity.startActivity(intent);

                }

            });

        }
        if (list.get(position).getUser() != null && user.getUid().equals(list.get(position).getUser().getId())) {
            holder.cancelBtn.setVisibility(View.VISIBLE);
        } else {
            holder.cancelBtn.setVisibility(View.GONE);
        }

        holder.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Xác nhận xóa");
                builder.setMessage("Bạn có muốn xóa tin này không?");

                builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteStory(position);
                    }
                });

                builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });





    }
    private void deleteStory(int position) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference storyRef = db.collection("Stories").document(list.get(position).getId());

        storyRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(activity, "Story deleted", Toast.LENGTH_SHORT).show();
                        if (position >= 1 && position < list.size()) {
                            list.remove(position);
                        } else {
                            Log.e("StoryAdapter", "Invalid position: " + position);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(activity, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class StoriesHolder extends RecyclerView.ViewHolder {

        private CircleImageView imageView;
        private ImageButton cancelBtn;
        private TextView textView;

        public StoriesHolder(@NonNull View itemView) {
            super(itemView);


            imageView = (CircleImageView) itemView.findViewById(R.id.imageView);
            textView= itemView.findViewById(R.id.nameTV);
            cancelBtn= itemView.findViewById(R.id.cancelBtn);

        }
    }

}
