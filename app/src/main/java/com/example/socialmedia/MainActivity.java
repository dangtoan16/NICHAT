package com.example.socialmedia;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.socialmedia.adapter.ViewPagerAdapter;
import com.example.socialmedia.fragments.Add;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


import java.net.URI;
import java.net.URISyntaxException;
import tech.gusavila92.websocketclient.WebSocketClient;

public class MainActivity extends ActiveActivity implements Add.OnDataPass {

    private WebSocketClient webSocketClient;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    ViewPagerAdapter pagerAdapter;
    FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
 //       createWebSocketClient();
        init();
        addTabs();
        setProfileIcon();
    }
    private void init(){
        viewPager= findViewById(R.id.viewPager);
        tabLayout= findViewById(R.id.tabLayout);

    }
    private void addTabs() {

        List<Integer> drawableResList= new ArrayList<>();
        drawableResList.add(R.drawable.ic_home);
        drawableResList.add(R.drawable.ic_search);
        drawableResList.add(R.drawable.ic_add);
        drawableResList.add(R.drawable.ic_notification);

        for(int i=0 ; i<4; i++){
            tabLayout.addTab(tabLayout.newTab().setIcon(drawableResList.get(i)));
        }
        tabLayout.addTab(tabLayout.newTab());

        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        Objects.requireNonNull(tabLayout.getTabAt(0)).setIcon(R.drawable.ic_home_fill);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (viewPager!=null && tab!=null){
                    int vitri= tab.getPosition();
                 //   viewPager.setCurrentItem(tab.getPosition());
                    switch (tab.getPosition()) {
                        case 0:
                            tab.setIcon(R.drawable.ic_home_fill);
                            break;
                        case 1:
                            tab.setIcon(R.drawable.ic_search);
                            break;
                        case 2:
                            tab.setIcon(R.drawable.ic_added);
                            break;
                        case 3:
                            tab.setIcon(R.drawable.ic_notification);
                            break;
                    }
                    viewPager.setCurrentItem(vitri);
                    //viewPager.postDelayed(() -> viewPager.setCurrentItem(vitri),1500);

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        tab.setIcon(R.drawable.ic_home);
                        break;
                    case 1:
                        tab.setIcon(R.drawable.ic_search);
                        break;
                    case 2:
                        tab.setIcon(R.drawable.ic_add);
                        break;
                    case 3:
                        tab.setIcon(R.drawable.ic_notification);
                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        tab.setIcon(R.drawable.ic_home_fill);
                        break;
                    case 1:
                        tab.setIcon(R.drawable.ic_search);
                        break;
                    case 2:
                        tab.setIcon(R.drawable.ic_add);
                        break;
                    case 3:
                        tab.setIcon(R.drawable.ic_notification);
                        break;
                }
            }
        });
    }

    public void setProfileIcon() {
        FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (!document.exists())
                    return; // or handle the case when the document doesn't exist
                setTabIcon(document);
            }
        });

        FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).addSnapshotListener((value, error) -> {
           if(!value.exists())
               return;
           setTabIcon(value);
        });
    }
    private void setTabIcon(DocumentSnapshot value){
        String imageUrl = value.getString("profileImageUrl");
        TabLayout.Tab profileTab = tabLayout.getTabAt(4);
        if (profileTab == null) {
            Log.d("my_app_MainActivity_setTabIcon", "Tab 4 null");
            return;
        }
        Glide.with(getApplicationContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        profileTab.setIcon( resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }


    @Override
    public void onChange(boolean returnToHome){
          if (returnToHome) {
            Toast.makeText(this, "Updated succesfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error!",Toast.LENGTH_SHORT ).show();
        }
        viewPager.setCurrentItem(0);
        pagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed(){
        if (viewPager.getCurrentItem()==4){
            viewPager.setCurrentItem(0);
        }else
            super.onBackPressed();
    }
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
        Map<String,Object> map= new HashMap<>();
        map.put("online",online);
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(user.getUid())
                .update(map);
    }
}