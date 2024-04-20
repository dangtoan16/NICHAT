package com.example.socialmedia.adapter;

import static androidx.core.content.res.TypedArrayUtils.getText;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmedia.R;
import com.example.socialmedia.model.CommentModel;
import com.example.socialmedia.model.PostImageModel;
import com.example.socialmedia.model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder> {
    Context context;
    List<CommentModel> list;
    FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
    public CommentAdapter(Context context, List<CommentModel> list){
        this.context= context;
        this.list= list;
    }
    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.comment_items,parent,false);
        return new CommentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentHolder holder, int position) {
        final CommentModel cmt = list.get(position);
        FirebaseFirestore refenrence= FirebaseFirestore.getInstance();
        refenrence.collection("Users")
                .document(list.get(position).getUser().getId())
                        .get()
                        .addOnCompleteListener(task ->
                        {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document= task.getResult();
                                    if (!document.exists())
                                        return; // or handle the case when the document doesn't exist
                                    Users user = document.toObject(Users.class);
                                    setData(user,holder,position);
                            } else {
                                // Handle errors
                                Log.e("my_app_home", "Error getting documents: " + task.getException());
                            }

                        });
        if (user.getUid().equals(list.get(position).getUser().getId())){
            holder.menuCmtBtn.setVisibility(View.VISIBLE);

        }else {
            holder.menuCmtBtn.setVisibility(View.GONE);
        }
        holder.menuCmtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.edit) {
                            editCmt(cmt.getCommentID());
                            return true;
                        }
                        else if (menuItem.getItemId() == R.id.delete) {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("deleted", true);
                            FirebaseFirestore.getInstance().collection("Comments")
                                    .document(cmt.getCommentID()).update(hashMap);

                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.inflate(R.menu.cmt_menu);
//                if (!cmt.getPublisher().equals(user.getUid())){
//                    popupMenu.getMenu().findItem(R.id.edit).setVisible(false);
//                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
//                }
                popupMenu.show();
            }
        });
    }
    private void editCmt(final String cmtid){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("Edit CMT");

        final EditText editText = new EditText(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        editText.setLayoutParams(lp);
        alertDialog.setView(editText);

        getText(cmtid, editText);

        alertDialog.setPositiveButton("Edit",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("comment", editText.getText().toString());
                        FirebaseFirestore.getInstance().collection("Comments")
                                .document(cmtid).update(hashMap);
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
    private void getText(String cmtid, final EditText editText){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comemts")
                .child(cmtid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                editText.setText(dataSnapshot.getValue(CommentModel.class).getComment());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setData(Users user, CommentHolder holder, int position) {
        Glide.with(context)
                .load(user.getProfileImageUrl())
                .into(holder.profileImage);
        holder.nameTv.setText(user.getName());
        holder.commentTv.setText(list.get(position).getComment());
        Date date = list.get(position).getTimestamp(); // Current date
        Log.d("avatar",user.getProfileImageUrl());
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("'Lúc 'HH'h'mm',' dd', tháng' MM', năm' yyyy");
            String formattedDate = sdf.format(date);
            holder.timeTv.setText(formattedDate);
        } else {
            holder.timeTv.setText("Unknown"); // or any default value
        }
    }


    @Override
    public int getItemCount() {
        return list.size();
    }
    static class CommentHolder extends RecyclerView.ViewHolder{
        CircleImageView profileImage;
        TextView nameTv, commentTv, timeTv;
        ImageButton menuCmtBtn;
        public CommentHolder(@NonNull View itemView){
            super(itemView);
            profileImage= itemView.findViewById(R.id.profileImage);
            nameTv= itemView.findViewById(R.id.nameTV);
            commentTv= itemView.findViewById(R.id.commentTV);
            timeTv= itemView.findViewById(R.id.timeTV);
            menuCmtBtn= itemView.findViewById(R.id.menuCmtBtn);
        }
    }

}
