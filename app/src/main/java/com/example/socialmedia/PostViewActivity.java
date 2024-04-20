package com.example.socialmedia;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.checkerframework.checker.guieffect.qual.UI;

public class PostViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_view);
        Intent intent= getIntent();
        String action= intent.getAction();
        Uri uri= intent.getData();
        String scheme= uri.getScheme();
        String host = uri.getHost();
        String path= uri.getPath();
        String query= uri.getQuery();

        FirebaseStorage.getInstance().getReference().child(uri.getLastPathSegment())
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        ImageView imageView= findViewById(R.id.imageView);
                        Glide.with(PostViewActivity.this)
                                .load(uri.toString())
                                .timeout(4500)
                                .into(imageView);
                    }
                });

    }
    public void onBackPressed(){
        super.onBackPressed();
        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            startActivity(new Intent(PostViewActivity.this,MainActivity.class));
        }else
            startActivity(new Intent(PostViewActivity.this,ReplaceActivity.class));
    }
}