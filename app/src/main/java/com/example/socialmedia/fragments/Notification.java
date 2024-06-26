package com.example.socialmedia.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.socialmedia.R;
import com.example.socialmedia.adapter.NotificationAdapter;
import com.example.socialmedia.model.NotificationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Notification extends Fragment {
    RecyclerView recyclerView;
    NotificationAdapter adapter;
    List<NotificationModel> list;
    FirebaseUser user;
    public Notification() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        loadNotification();
    }

    void loadNotification() {
        CollectionReference reference= FirebaseFirestore.getInstance().collection("Notifications");
        DocumentReference userRef= FirebaseFirestore.getInstance().collection("Users").document(user.getUid()) ;
        reference.whereEqualTo("target",userRef)
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                   if (error!=null) {
                       Log.e("err", "" + error.getMessage());
                       return;
                   }
                   if (value.isEmpty()){
                       Log.e("value", "" + "ko co thong bao");
                       return;
                   }

                   list.clear();
                   for (QueryDocumentSnapshot snapshot : value){
                       NotificationModel model= snapshot.toObject(NotificationModel.class);
                       list.add(model);
                   }
                   Log.d("list",""+list.size());
                   adapter.notifyDataSetChanged();
                });
    }

    void init(View view) {
        recyclerView= view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        list =new ArrayList<>();
        adapter= new NotificationAdapter(getContext(),list);
        recyclerView.setAdapter(adapter);
        user= FirebaseAuth.getInstance().getCurrentUser();
    }
}