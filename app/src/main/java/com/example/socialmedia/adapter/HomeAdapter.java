package com.example.socialmedia.adapter;

import static androidx.core.content.ContextCompat.startActivity;
import static androidx.core.content.res.TypedArrayUtils.getText;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.socialmedia.OtherProfileActivity;
import com.example.socialmedia.R;
import com.example.socialmedia.ReplaceActivity;
import com.example.socialmedia.model.CommentModel;
import com.example.socialmedia.model.PostImageModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeHolder> {
    private final List<PostImageModel> list;
    Activity context;
    OnPressed onPressed;

    public HomeAdapter(List<PostImageModel> list, Activity context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public HomeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_items, parent, false);
        return new HomeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeHolder holder, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        PostImageModel currentPost = list.get(position);

        currentPost.getName(holder.userNameTv::setText);
        Date date = currentPost.getTimestamp(); // Current date
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("'Lúc 'HH'h'mm',' dd', tháng' MM', năm' yyyy", Locale.getDefault());
            String formattedDate = sdf.format(date);
            holder.timeTv.setText(formattedDate);
        } else {
            holder.timeTv.setText("Unknown"); // or any default value
        }
        currentPost.getLikesCount(likesCount -> {
            String text = likesCount + " likes";
            holder.likeCountTv.setText(text);
        });

        assert user != null;
        currentPost.CheckLikedPost((currentUserLikedPostBefore) -> {
            holder.likeCheckBox.setChecked(currentUserLikedPostBefore);
            holder.clickListener(currentPost);
        });

        holder.descriptionTv.setText(currentPost.getDescription());
        Random random = new Random();
        // tại sao lại lấy màu ?
        int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        currentPost.getProfileImage(profileImageUrl -> {
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_person)
                    .timeout(6500)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
            Glide.with(context.getApplicationContext())
                    .load(profileImageUrl != null ? profileImageUrl : R.drawable.ic_person)
                    .apply(requestOptions)
                    .into(holder.profileImage);
        });

        Glide.with(context.getApplicationContext())
                .load(currentPost.getPostImageUrl())
                .placeholder(new ColorDrawable(color))
                .timeout(7000)
                .into(holder.imageView);

        likePostListener(currentPost, holder.likeCountTv);
        setUpPost(holder,position);
        clickUserName(holder.userNameTv,list.get(position).getUserOwnerOfPost().getId());

    }
    public void clickUserName(TextView userNameTv,String otherUserId){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userNameTv.setOnClickListener(v -> {
            if (user.getUid().equals(otherUserId)){
                return;
            }
                Intent i= new Intent(context, OtherProfileActivity.class);
                i.putExtra("User need to find",otherUserId);
                context.startActivity(i);
        });
    }
    public void setUpPost(@NonNull HomeHolder holder, int position){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user.getUid().equals(list.get(position).getUserOwnerOfPost().getId())){
            holder.menuPostBtn.setVisibility(View.VISIBLE);

        }else {
            holder.menuPostBtn.setVisibility(View.GONE);
            holder.userNameTv.setTextColor(Color.argb(255,138,43,226));
        }
        holder.menuPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.edit) {
                            editPost(list.get(position).getPostId());
                            return true;
                        }
                        else if (menuItem.getItemId() == R.id.delete) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("deleted", true);
                            FirebaseFirestore.getInstance().collection("Posts")
                                    .document(list.get(position).getPostId()).update(hashMap);
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.inflate(R.menu.cmt_menu);
                popupMenu.show();
            }
        });

}
    private void editPost(final String postid){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("Edit Post");

        final EditText editText = new EditText(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        editText.setLayoutParams(lp);
        alertDialog.setView(editText);

        getText(postid, editText);

        alertDialog.setPositiveButton("Edit",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("description", editText.getText().toString());
                        FirebaseFirestore.getInstance().collection("Posts")
                                .document(postid).update(hashMap);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        alertDialog.show();
    }
    private void getText(String postid, final EditText editText){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts")
                .child(postid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                editText.setText(dataSnapshot.getValue(PostImageModel.class).getDescription());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    public void likePostListener(PostImageModel postImageModel,TextView textView){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("LikePosts").whereEqualTo("post",postImageModel.getPostReference()).addSnapshotListener(((value, error) -> {
                if (error != null) {
                    Log.d("Error: ", error.getMessage());
                }

                if (value == null)
                    return;
                postImageModel.getLikesCount(likesCount -> {
                String text = likesCount + " likes";
                textView.setText(text);
            });
        }));

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public void OnPressed(OnPressed onPressed) {
        this.onPressed = onPressed;
    }

    public interface OnPressed {
        void onLiked(PostImageModel post,boolean isChecked);

        void setCommentCount(TextView textView);
    }

    class HomeHolder extends RecyclerView.ViewHolder {
        private final CircleImageView profileImage;
        private final TextView userNameTv;
        private final TextView timeTv;
        private final TextView likeCountTv;
        private final TextView descriptionTv;
        private final ImageView imageView;
        private final CheckBox likeCheckBox;
        private final ImageButton commentBtn;
        private final ImageButton shareBtn;

        private  ImageButton menuPostBtn;
        public HomeHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            imageView = itemView.findViewById(R.id.imageView);
            userNameTv = itemView.findViewById(R.id.nameTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            likeCountTv = itemView.findViewById(R.id.likeCountTv);
            likeCheckBox = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            descriptionTv = itemView.findViewById(R.id.descTv);
            TextView commenTv = itemView.findViewById(R.id.commentTv);
            menuPostBtn= itemView.findViewById(R.id.menuPostBtn);
            onPressed.setCommentCount(commenTv);

        }

        public void clickListener(final PostImageModel currentPost) {
            commentBtn.setOnClickListener(v -> {
                Intent intent = new Intent(context, ReplaceActivity.class);
                intent.putExtra("currentPostId", currentPost.getPostId());
                intent.putExtra("isComment", true);
                context.startActivity(intent);
            });
            likeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> { {onPressed.onLiked(currentPost,isChecked);}} );
            shareBtn.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, currentPost.getPostImageUrl());
                intent.setType("text/*");
                context.startActivity(Intent.createChooser(intent, "Share link using..."));
            });
        }
    }
}
