package com.example.socialmedia.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.socialmedia.MainActivity;
import com.example.socialmedia.OtherProfileActivity;
import com.example.socialmedia.R;
import com.example.socialmedia.adapter.UserAdapter;
import com.example.socialmedia.model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Search extends Fragment {
    SearchView searchView;
    RecyclerView recyclerView;
    UserAdapter adapter;
    CollectionReference reference;
    List<Users> allUsers;
    private List<Users> list;
    private List<Users> listHistory;


    public Search() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);
        init(view);
        reference= FirebaseFirestore.getInstance().collection("Users");
       // loadUserData();
       // loadHistorySearch();
        searchUser();
        clickListener();
    }
    private void clickListener(){
        adapter.OnUserClicked(new UserAdapter.OnUserClicked() {
            @Override
            public void onClicked(String uid) {
                Intent intent= new Intent(getActivity(), OtherProfileActivity.class);
                intent.putExtra("User need to find",uid);
                startActivity(intent );
            }
        });
    }
    private void searchUser() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                reference.orderBy("name").startAt(query).endAt(query + "\uf8ff")
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (DocumentSnapshot snapshot : task.getResult()) {
                                        if (!snapshot.exists()) return;
                                        Users users = snapshot.toObject(Users.class);
                                        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        if (!users.getUid().equals(currentUserUid)) {
                                            if (list.stream().allMatch(u -> !u.getUid().equals(users.getUid()))) {
                                                list.add(0, users);
                                                continue;
                                            }
                                            if (list.get(0).equals(users)) {
                                                continue;
                                            }
                                            swapElementInList(users.getUid());
                                        }
                                    }
                                    adapter.notifyDataSetChanged();
                                    Log.d("LENGHT", "dai la: " + Integer.toString(list.size()));
                                }
                            }
                        });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void swapElementInList(String uid) {
        for (int i = 0; i < list.size(); ++i) {
            if (list.get(i).getUid().equals(uid)) {
                Collections.swap(list, i, 0);
                return;
            }
        }
    }

    private void loadUserData(){
        reference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error!=null)
                    return;
                if (value==null)
                    return;
                list.clear();
                for (QueryDocumentSnapshot snapshot : value){
                    Users users= snapshot.toObject(Users.class);
                    list.add(users);
                }
          //      adapter.notifyDataSetChanged();
            }
        });
    }
    private void loadUserDataSearch(List<String> listSeach){
        reference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error!=null)
                    return;
                if (value==null)
                    return;
                listHistory.clear();
                for (QueryDocumentSnapshot snapshot : value){
                    Users users= snapshot.toObject(Users.class);
                    if (listSeach.contains(users.getUid())&& !listHistory.contains(users)){
                        Log.d("listSL",Integer.toString(listSeach.size()));
                        listHistory.add(users);
                        Log.d("user",users.toString());
                    }
                }
                //      adapter.notifyDataSetChanged();
            }
        });
    }
//    private void loadHistorySearch() {
//        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        reference.orderBy("uid").startAt(user).endAt(user + "\uf8ff").limit(1)
//                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (DocumentSnapshot snapshot : task.getResult()) {
//                                if (!snapshot.exists()) return;
//                                Users users = snapshot.toObject(Users.class);
//                                List<String> listSearched = users.getSearched();
//                                Log.d("LENGTH", "search size=" + listSearched.size());
//                                for (Users u : getUsersThatInListByUid(listSearched)) {
//
//                                    list.add(u);
//                                    adapter.notifyDataSetChanged();
//                                    Log.d("LENGTH", "list 154 size=" + list.size());
//                                }
//
//                            }
//
//                        }
//                        Log.d("LENGTH", "list 197 size=" + list.size());
//                    }
//                });
//    }
    public List<Users> getUsersThatInListByUid(List<String> uidNeedToFind) {
        List<Users> users = new ArrayList<>();
        if(uidNeedToFind.isEmpty())
            return users;

        Task<QuerySnapshot> task = reference.whereIn("uid", uidNeedToFind).get();
        task.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot snapshot : task.getResult()) {
                        if (!snapshot.exists())
                            return; // or handle the case when the document doesn't exist
                        Users user = snapshot.toObject(Users.class);
                        if (!user.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                            users.add(user);
                    }
                    Log.d("LENGTH", "dai users la: " + users.size());
                } else {
                    Log.e("ERROR", "Error getting documents: ", task.getException());
                }
            }
        });

        return users;
    }
    private void init(View view){
        searchView= view.findViewById(R.id.searchView);
        recyclerView= view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        list= new ArrayList<>();
        listHistory= new ArrayList<>();
        adapter= new UserAdapter(list);
        recyclerView.setAdapter(adapter);
    }
}