package com.evileye2002.real_timechatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.evileye2002.real_timechatapp.adapters.FriendAdapter;
import com.evileye2002.real_timechatapp.databinding.ActivityFriendBinding;
import com.evileye2002.real_timechatapp.models.User;
import com.evileye2002.real_timechatapp.utilities.Const;
import com.evileye2002.real_timechatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FriendActivity extends AppCompatActivity {
    ActivityFriendBinding binding;
    PreferenceManager manager;
    DocumentReference currentUser;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListener();
        getFriends();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFriends();
    }

    void init() {
        manager = new PreferenceManager(getApplicationContext());
        currentUserID = manager.getString(Const.ID);
        currentUser = Const.userDoc(currentUserID);
    }

    void setListener() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.imageAddFriend.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AddFriendActivity.class);
            startActivity(intent);
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

    void getFriends() {
        Const.user_collection.get().addOnCompleteListener(task -> {
            loading(false);
            List<User> userList = new ArrayList<>();
            boolean isExist = task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0;
            if (isExist) {
                for (QueryDocumentSnapshot snapshot : task.getResult()) {
                    if (snapshot.getId().equals(currentUserID))
                        continue;
                    if(snapshot.getString(Const.FRIENDS) != null)
                        if (snapshot.getString(Const.FRIENDS).contains(currentUserID)) {
                            User user = snapshot.toObject(User.class);
                            user.id = snapshot.getId();
                            userList.add(user);
                        }
                }
                if (userList.size() > 0) {
                    FriendAdapter adapter = new FriendAdapter(userList, user -> {
                        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                        intent.putExtra(Const.USER, user);
                        startActivity(intent);
                        finish();
                    });
                    binding.recyclerView.setAdapter(adapter);
                }
            }
        });
    }

}