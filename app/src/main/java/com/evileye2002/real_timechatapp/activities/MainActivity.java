package com.evileye2002.real_timechatapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.evileye2002.real_timechatapp.R;
import com.evileye2002.real_timechatapp.adapters.ConversationAdapter;
import com.evileye2002.real_timechatapp.databinding.ActivityMainBinding;
import com.evileye2002.real_timechatapp.models.Conversation;
import com.evileye2002.real_timechatapp.utilities.PreferenceManager;
import com.evileye2002.real_timechatapp.utilities.Timestamp;
import com.evileye2002.real_timechatapp.utilities._const;
import com.evileye2002.real_timechatapp.utilities._firestore;
import com.evileye2002.real_timechatapp.utilities._funct;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    PreferenceManager manager;
    String currentUserID;
    List<Conversation> mainConList;
    ConversationAdapter conAdapter;
    View navViewHeader;
    String timestamp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //test();
        init();
        setListener();
        getCurrentUserData();
        getToken();
        requestPermission();
        listenConversation();
    }

    void test() {
        //String raw = "Thứ Ba, 10 Tháng Mười, 2023, tuần 41";
        String raw = "Thứ Ba, 10 Tháng Mười, 2023, tuần 41";
        String[] pre = raw.toLowerCase().split(",");
        String mm = pre[1]
                .replace(" một", " 1")
                .replace(" hai", " 2")
                .replace(" ba", " 3")
                .replace(" tư", " 4")
                .replace(" năm", " 5")
                .replace(" sáu", " 6")
                .replace(" bảy", " 7")
                .replace(" tám", " 8")
                .replace(" chín", " 9")
                .replace(" mười", " 10")
                .replace(" mười một", " 11")
                .replace(" mười hai", " 12");
        String pre2 = "01:02:03, " + pre[0] + "," + mm + "," + pre[2];

        String patternRaw = "HH:mm:ss, EEEE, dd MMMM, y";
        String patternTarget = "HH:mm:ss,EEE,dd,MMM,y";
        Date dateRaw = _funct.stringToDate(pre2, patternRaw);
        String date = _funct.dateToString(dateRaw, patternTarget).toUpperCase();
        String a = date;
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
        mainConList = new ArrayList<>();
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
        _firestore.singleUser(currentUserID).update(updates)
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
        _firestore.firestoreMessaging.getToken()
                .addOnSuccessListener(this::updateToken);
    }

    void updateToken(String token) {
        _firestore.singleUser(currentUserID).update(_const.TOKEN, token);
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

    void listenConversation() {
        loading(true);
        _firestore.allCons()
                .orderBy(_const.LAST_TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener(conversationListener);
    }

    final EventListener<QuerySnapshot> conversationListener = (value, error) -> {
        if (error != null)
            return;
        if (value != null) {
            loading(false);
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                Conversation newCon = documentChange.getDocument().toObject(Conversation.class);
                newCon.id = documentChange.getDocument().getId();
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    if (newCon.memberList.toString().contains(currentUserID))
                        mainConList.add(newCon);
                }
                if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (Conversation conMain : mainConList) {
                        if (conMain.id.equals(newCon.id)) {
                            conMain.lastMessage = newCon.lastMessage;
                            conMain.lastSenderID = newCon.lastSenderID;
                            conMain.lastTimestamp = newCon.lastTimestamp;
                            //conAdapter.notifyItemChanged(mainConList.indexOf(conMain));
                            mainConList.sort((o1, o2) -> o2.lastTimestamp.compareTo(o1.lastTimestamp));
                            conAdapter = new ConversationAdapter(currentUserID, mainConList, conversation -> {
                                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                                intent.putExtra(_const.CONVERSATION, conversation);
                                startActivity(intent);
                                finish();
                            });
                            binding.recyclerView.setAdapter(conAdapter);
                            return;
                        }
                    }
                }
            }
            updateCon();
        }
    };

    void updateCon() {
        if(conAdapter == null){
            conAdapter = new ConversationAdapter(currentUserID, mainConList, conversation -> {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra(_const.CONVERSATION, conversation);
                startActivity(intent);
                finish();
            });
            binding.recyclerView.setAdapter(conAdapter);
        }
        conAdapter.notifyItemRangeInserted(mainConList.size(), mainConList.size());
    }
}