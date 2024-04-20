package com.example.socialmedia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.example.socialmedia.fragments.Comment;
import com.example.socialmedia.fragments.CreateAccountFragment;
import com.example.socialmedia.fragments.LoginFragment;

public class ReplaceActivity extends AppCompatActivity {

    private FrameLayout frameLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replace);

        frameLayout= findViewById(R.id.frameLayout);
        String postId= getIntent().getStringExtra("currentPostId");
        boolean isComment= getIntent().getBooleanExtra("isComment",false);
        if (isComment)
            setFragment(new Comment(postId));
        else
            setFragment(new LoginFragment());
    }
    public void setFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction= getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right);

        if(fragment instanceof CreateAccountFragment){
            fragmentTransaction.addToBackStack(null);
        }
        if (fragment instanceof Comment){
            String id= getIntent().getStringExtra("id");
            String uid= getIntent().getStringExtra("uid");
            Bundle bundle= new Bundle();
            bundle.putString("id",id);
            bundle.putString("uid",uid);
            fragment.setArguments(bundle);
        }
        fragmentTransaction.replace(frameLayout.getId(),fragment);
        fragmentTransaction.commit();
    }
}