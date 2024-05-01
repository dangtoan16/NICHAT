package com.example.socialmedia;

import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class UpdateProfileActivity extends ActiveActivity {
    EditText nameEt, statusEt;
    TextInputEditText newPass, confirmPass;
    AppCompatButton updateBtn;
    ProgressBar progressBar;
    ImageButton back;
    FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_profile);
        init();
        upload();

        click();
    }
    private void init(){
        nameEt= findViewById(R.id.nameET);
        statusEt= findViewById(R.id.statusET);
        newPass= findViewById(R.id.PassET);
        confirmPass= findViewById(R.id.confirmPassET);
        updateBtn= findViewById(R.id.updateBtn);
        progressBar = findViewById(R.id.progressBar);
        back= findViewById(R.id.backBtn);
    }
    private void upload(){
        DocumentReference currentUserRef = FirebaseFirestore.getInstance().collection("Users").document(user.getUid());
        currentUserRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("my_app_profile_loadProfileData", error.getMessage());
                return;
            }

            if (value == null || !value.exists()) return;
            String name = value.getString("name");
            String status = value.getString("status");
            final String profileURL = value.getString("profileImageUrl");
            nameEt.setText(name);
            statusEt.setText(status);
        });
    }
    private void click(){
        updateBtn.setOnClickListener(v -> {
            String name= nameEt.getText().toString();
            String status= statusEt.getText().toString();
            String newpassword= newPass.getText().toString();
            String confirmPassword= confirmPass.getText().toString();
            HashMap<String, Object> hashMap = new HashMap<>();
            if (!name.trim().isEmpty()) {
                hashMap.put("name", name);
            }
            if (!status.trim().isEmpty())
            {
                hashMap.put("status", status);
            }
            runOnUiThread(this::runProgressBar);
            up(hashMap,newpassword);
            if(!newpassword.equals(confirmPassword)){
                confirmPass.setError("Password not match");
                return;
            }

        });
        back.setOnClickListener(v -> {
            finish();
        });
    }
    private void up(HashMap<String, Object> hashMap,String newpassword){
        FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                .update(hashMap);
        if (!newpassword.trim().isEmpty()) {
            user.updatePassword(newpassword);
        }
        Toast.makeText(UpdateProfileActivity.this, "Update profile successfully", Toast.LENGTH_SHORT).show();
    }
    private void  runProgressBar(){
        progressBar.setVisibility(VISIBLE);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        progressBar.setVisibility(View.GONE);
    }
}