package com.example.socialmedia.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmedia.ActiveActivity;
import com.example.socialmedia.R;
import com.example.socialmedia.adapter.HistoryChatAdapter;
import com.example.socialmedia.model.HistoryChatModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistoryChatActivity extends ActiveActivity {

    HistoryChatAdapter adapter;
    List<HistoryChatModel> list;
    FirebaseUser user;
    String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_user);

        init();

        fetchUserData();

        clickListener();

    }

    void init() {

        RecyclerView recyclerView = findViewById(R.id.recyclerView);


        list = new ArrayList<>();
        adapter = new HistoryChatAdapter(this, list);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(adapter);

        user = FirebaseAuth.getInstance().getCurrentUser();


    }


    void fetchUserData() {

        CollectionReference reference = FirebaseFirestore.getInstance().collection("Messages");
        reference.whereArrayContains("uid", user.getUid())
                .addSnapshotListener((value, error) -> {

                    if (error != null)
                        return;

                    if (value == null)
                        return;



                    if (value.isEmpty())
                        return;


                    list.clear();
                    for (QueryDocumentSnapshot snapshot : value) {

                        if (snapshot.exists()) {
                            HistoryChatModel model = snapshot.toObject(HistoryChatModel.class);
                            list.add(model);
                        }

                    }

                    adapter.notifyDataSetChanged();

                });


    }

    void clickListener() {

        adapter.OnStartChat((position, uids, chatID) -> {

            String oppositeUID;
            if (!uids.get(0).equalsIgnoreCase(user.getUid())) {
                oppositeUID = uids.get(0);
            } else {
                oppositeUID = uids.get(1);
            }

            Intent intent = new Intent(this, MessageActivity.class);
            intent.putExtra("uid", oppositeUID);
            intent.putExtra("uid0",uids.get(0));
            intent.putExtra("uid1",uids.get(1));
            intent.putExtra("id", chatID);
            startActivity(intent);


        });

    }
}

