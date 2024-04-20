package com.example.socialmedia.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialmedia.R;
import com.example.socialmedia.adapter.CommentAdapter;
import com.example.socialmedia.model.CommentModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.N;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Comment extends Fragment {

    EditText commentEt;
    ImageButton sendBtn;
    RecyclerView recyclerView;
    CommentAdapter commentAdapter;
    List<CommentModel> list;
    FirebaseUser user;
    String id,uid;
    CollectionReference reference;
    private FirebaseFirestore db;
    String postId;
    public Comment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_comment, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @NonNull Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);
        init(view);
        reference= FirebaseFirestore.getInstance().collection("Comments");
        loadCommentData();
        clickListener();
    }
    public Comment(String postId){
        this.postId= postId;
    }
    private void clickListener() {
        sendBtn.setOnClickListener(v -> {
            String comment= commentEt.getText().toString();
            if (comment.isEmpty()){
                Toast.makeText(getContext(),"Enter comment",Toast.LENGTH_SHORT).show();
                return;
            }
            DocumentReference currentUserRef = db.collection("Users").document(user.getUid());
            DocumentReference postRef = db.collection("Posts").document(postId);
            String commentID= reference.document().getId();
            Map<String,Object> map= new HashMap<>();
            map.put("comment",comment);
            map.put("commentID", commentID);
            map.put("timestamp", FieldValue.serverTimestamp());
            map.put("updateAt",FieldValue.serverTimestamp());
            map.put("user",currentUserRef);
            map.put("post", postRef);
            map.put("deleted",false);
            reference.document(commentID)
                    .set(map)
                    .addOnCompleteListener(task -> {
                       if (task.isSuccessful()){
                           commentEt.setText("");
                       }else {
                           Toast.makeText(getContext(),"Failed to cmt"+task.getException().getMessage(),
                                   Toast.LENGTH_SHORT).show();
                       }
                    });
        });
    }

    private void loadCommentData() {
        DocumentReference currentPost= db.collection("Posts").document(postId);

        reference.whereEqualTo("post",currentPost).whereEqualTo("deleted",false).addSnapshotListener((value, error) -> {
            if (error!=null) return;
            if (value==null){
                Toast.makeText(getContext(),"No Cmt", Toast.LENGTH_SHORT).show();
                return;
            }
            list.clear();
            for (DocumentSnapshot snapshot: value){
                CommentModel model= snapshot.toObject(CommentModel.class);
                list.add(model);
            }
            // Sort only if the list is not empty and timestamps are not null
            if (!list.isEmpty()) {
                list.sort((comment1, comment2) -> {
                    // Check for null timestamps
                    if (comment1.getTimestamp() == null || comment2.getTimestamp() == null) {
                        return 0; // Return 0 to indicate equality if any timestamp is null
                    }
                    return comment1.getTimestamp().compareTo(comment2.getTimestamp());
                });
            }
            commentAdapter.notifyDataSetChanged();
        });
    }

    private void init(View view){
        commentEt= view.findViewById(R.id.commentET);
        sendBtn= view.findViewById(R.id.sendBtn);
        recyclerView= view.findViewById(R.id.commentRecycleView);
        user= FirebaseAuth.getInstance().getCurrentUser();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(null);
        list = new ArrayList<>();
        commentAdapter= new CommentAdapter(getContext(),list);
        recyclerView.setAdapter(commentAdapter);
        db= FirebaseFirestore.getInstance();
    }
}