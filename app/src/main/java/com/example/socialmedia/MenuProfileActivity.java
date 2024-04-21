package com.example.socialmedia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MenuProfileActivity extends ActiveActivity {
    TextView moonTv, logoutTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_profile);
        init();
        logoutTv.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReplaceActivity.class);
            intent.putExtra("isComment", false);
            this.startActivity(intent);
        });
    }

    private void init(){
        moonTv= findViewById(R.id.darkTv);
        logoutTv= findViewById(R.id.logOutTv);
    }
}