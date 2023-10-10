package com.evileye2002.real_timechatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.evileye2002.real_timechatapp.adapters.AddFriendAdapter;
import com.evileye2002.real_timechatapp.databinding.ActivityAddFriendBinding;
import com.evileye2002.real_timechatapp.models.User;
import com.evileye2002.real_timechatapp.utilities._const;
import com.evileye2002.real_timechatapp.utilities.PreferenceManager;
import com.evileye2002.real_timechatapp.utilities._firestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddFriendActivity extends AppCompatActivity {
    ActivityAddFriendBinding binding;
    PreferenceManager manager;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddFriendBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListener();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    void init() {
        manager = new PreferenceManager(getApplicationContext());
        currentUserID = manager.getString(_const.ID);
        loading(false);
    }

    void setListener() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.inputSearch.addTextChangedListener(searchUsers);
    }

    final TextWatcher searchUsers = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().trim().isEmpty()) {
                List<User> userList = new ArrayList<>();
                AddFriendAdapter adapter = new AddFriendAdapter(userList, user -> {

                });
                binding.recyclerView.setAdapter(adapter);
                return;
            }
            getUsers(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    void loading(Boolean isLoading) {
        if (isLoading) {
            binding.recyclerView.setVisibility(View.INVISIBLE);
            binding.processBar.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.processBar.setVisibility(View.INVISIBLE);
        }
    }

    void getUsers(String s) {
        loading(true);
        _firestore.allUsers().get().addOnCompleteListener(task -> {
            loading(false);
            List<User> userList = new ArrayList<>();
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot snapshot : task.getResult()) {
                    if (snapshot.getId().equals(currentUserID))
                        continue;
                    User user = snapshot.toObject(User.class);
                    user.id = snapshot.getId();
                    if (user.name.contains(s) && user.friendList == null) {
                        userList.add(user);
                        continue;
                    }
                    String friendList = user.friendList != null ? user.friendList.toString() : "";
                    if (friendList.contains(currentUserID))
                        continue;
                    if (user.name.contains(s))
                        userList.add(user);
                }
                if (userList.size() > 0) {
                    AddFriendAdapter adapter = new AddFriendAdapter(userList, user -> {
                        List<String> userFriends = user.friendList != null ? user.friendList : new ArrayList<>();
                        userFriends.add(currentUserID);
                        _firestore.singleUser(user.id).update(_const.FRIEND_LIST, userFriends);

                        _firestore.singleUser(currentUserID).get().addOnCompleteListener(task1 -> {
                            User currentUser = task1.getResult().toObject(User.class);
                            List<String> currentUserFriends = currentUser.friendList != null ? currentUser.friendList : new ArrayList<>();
                            currentUserFriends.add(user.id);
                            _firestore.singleUser(currentUserID).update(_const.FRIEND_LIST, currentUserFriends);
                        });
                    });
                    binding.recyclerView.setAdapter(adapter);
                }
            }
        });
    }
}