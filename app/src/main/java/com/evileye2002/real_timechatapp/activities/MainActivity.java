package com.evileye2002.real_timechatapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.evileye2002.real_timechatapp.R;
import com.evileye2002.real_timechatapp.databinding.ActivityMainBinding;
import com.evileye2002.real_timechatapp.utilities.PreferenceManager;
import com.evileye2002.real_timechatapp.utilities.Timestamp;
import com.evileye2002.real_timechatapp.utilities._const;
import com.evileye2002.real_timechatapp.utilities._firestore;
import com.evileye2002.real_timechatapp.utilities._funct;
import com.google.firebase.firestore.FieldValue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    PreferenceManager manager;
    String currentUserID;
    View navViewHeader;
    String timestamp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        test();
        init();
        setListener();
        getCurrentUserData();
        getToken();
        requestPermission();
    }

    void test() {
        Timestamp getTimestamp = new Timestamp(result -> {
            timestamp = result;
            String a = timestamp;
        });
        getTimestamp.execute();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    void requestPermission() {
        boolean isHasPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (isHasPermission) {

        } else
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else
                _funct.showToast(getApplicationContext(), "Yêu cầu quyền truy cập vào bộ nhớ để tiếp tục");
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    void init() {
        manager = new PreferenceManager(getApplicationContext());
        binding.navView.bringToFront();
        navViewHeader = binding.navView.getHeaderView(0);
        currentUserID = manager.getString(_const.ID);
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
                Intent intent = new Intent(getApplicationContext(), FriendActivity.class);
                startActivity(intent);
                finish();
            }
            if (item.getItemId() == R.id.navSignOut) {
                signOut();
            }
            return true;
        });
        ImageView btnSettings = navViewHeader.findViewById(R.id.imageSettings);
        btnSettings.setOnClickListener(v -> {
            /*Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);*/
        });
    }

    void signOut() {
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(_const.TOKEN, FieldValue.delete());
        _firestore.singleCon(currentUserID).update(updates)
                .addOnSuccessListener(unused -> {
                    manager.clear();
                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                });
    }

    void getCurrentUserData() {
        TextView textName = navViewHeader.findViewById(R.id.textName);
        ImageView imageProfile = navViewHeader.findViewById(R.id.imageProfile);
        textName.setText(manager.getString(_const.NAME));
        imageProfile.setImageBitmap(_funct.stringToBitmap(manager.getString(_const.IMAGE)));
    }

    void getToken() {
        _const.firestoreMessaging.getToken()
                .addOnSuccessListener(this::updateToken)
                .addOnFailureListener(e -> {

                });
    }

    void updateToken(String token) {
        _firestore.singleUser(currentUserID).update(_const.TOKEN, token)
                .addOnFailureListener(e -> {

                });
    }

    void loading(Boolean isLoading) {
        if (isLoading) {
            binding.recyclerView.setVisibility(View.INVISIBLE);
            binding.processBar.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.processBar.setVisibility(View.INVISIBLE);
        }
    }

}