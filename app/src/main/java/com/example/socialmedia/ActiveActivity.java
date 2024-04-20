package com.example.socialmedia;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public abstract class ActiveActivity extends AppCompatActivity {

        @Override
        protected void onResume(){
            super.onResume();
            updateStatus(true);
        }
        @Override
        protected void onPause(){
            updateStatus(false);
            super.onPause();
        }
        void updateStatus(boolean online){
            FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
            Map<String,Object> map= new HashMap<>();
            map.put("online",online);
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(user.getUid())
                    .update(map);
        }
}
