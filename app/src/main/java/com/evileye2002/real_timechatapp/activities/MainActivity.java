package com.evileye2002.real_timechatapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.evileye2002.real_timechatapp.R;
import com.evileye2002.real_timechatapp.databinding.ActivityMainBinding;
import com.evileye2002.real_timechatapp.utilities.Const;
import com.evileye2002.real_timechatapp.utilities.Funct;
import com.evileye2002.real_timechatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    PreferenceManager preferenceManager;
    DocumentReference currentUserDoc;
    View navViewHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListener();
        getCurrentUserData();
        getToken();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        binding.navView.bringToFront();
        navViewHeader = binding.navView.getHeaderView(0);
        currentUserDoc = Const.database.collection(Const.KEY_COLLECTION_USERS).document(preferenceManager.getString(Const.KEY_USER_ID));
    }

    void setListener() {
        binding.imageMenu.setOnClickListener(v -> {
            if (!binding.drawerLayout.isDrawerOpen(GravityCompat.START))
                binding.drawerLayout.openDrawer(GravityCompat.START);
        });

        binding.navView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navRooms) {

            }
            if (item.getItemId() == R.id.navFriend) {
                /*Intent intent = new Intent(getApplicationContext(), FriendActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);*/
            }
            if (item.getItemId() == R.id.navSignOut) {
                signOut();
            }
            return true;
        });

        ImageView btnSettings = navViewHeader.findViewById(R.id.imageSettings);
        btnSettings.setOnClickListener(v -> {
            Funct.showToast(getApplicationContext(), "click setting");
        });
    }

    void signOut() {
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Const.KEY_USER_TOKEN, FieldValue.delete());
        currentUserDoc.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                });
    }

    void getCurrentUserData() {
        TextView textName = navViewHeader.findViewById(R.id.textName);
        ImageView imageProfile = navViewHeader.findViewById(R.id.imageProfile);
        textName.setText(preferenceManager.getString(Const.KEY_USER_NAME));
        imageProfile.setImageBitmap(Funct.stringToBitmap(preferenceManager.getString(Const.KEY_USER_IMAGE)));
    }

    void getToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(this::updateToken)
                .addOnFailureListener(e -> {

                });
    }

    void updateToken(String token) {
        currentUserDoc.update(Const.KEY_USER_TOKEN, token)
                .addOnFailureListener(e -> {

                });
    }

}