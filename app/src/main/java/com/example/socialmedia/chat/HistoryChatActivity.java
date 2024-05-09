package com.example.socialmedia.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmedia.ActiveActivity;
import com.example.socialmedia.R;
import com.example.socialmedia.adapter.HistoryChatAdapter;
import com.example.socialmedia.model.HistoryChatModel;
import com.example.socialmedia.model.NameIdModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoryChatActivity extends ActiveActivity {

    HistoryChatAdapter adapter;
    EditText searchET;
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
        searchET= findViewById(R.id.searchET);

        list = new ArrayList<>();
        adapter = new HistoryChatAdapter(this, list);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(adapter);

        user = FirebaseAuth.getInstance().getCurrentUser();


    }


    void fetchUserData() {

        CollectionReference reference = FirebaseFirestore.getInstance().collection("Messages");
        reference.whereArrayContains("uid", user.getUid()).orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null){
                        Log.d("sapxepChat",error.getMessage());
                        return;}

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
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    adapter.setList(list);
                }else
                    searchChat(s.toString());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.notifyDataSetChanged();
            }
        });

    }
    public void searchChat(String searchText) {
        searchText = searchText.replaceAll("\\s+", "");
        List<HistoryChatModel> filteredList = new ArrayList<>();

        adapter.setList(filteredList);
        for (int i=0 ; i<list.size(); i++) {
            String name = findNameByID(list.get(i));
            // So sánh tên với từ khóa tìm kiếm (không phân biệt hoa thường)
            if (name != null && name.toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(list.get(i));
            }
        }
        adapter.notifyDataSetChanged();
    }
    public String findNameByID(HistoryChatModel historyChatModel){
        for (NameIdModel nameID: adapter.listNameId){
            if (historyChatModel.getUid().contains(nameID.id) ){
                return nameID.name;
            }
        }
        return "Not found !!!";
    }

}

