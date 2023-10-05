package com.evileye2002.real_timechatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.evileye2002.real_timechatapp.adapters.AddFriendAdapter;
import com.evileye2002.real_timechatapp.databinding.ActivityAddFriendBinding;
import com.evileye2002.real_timechatapp.models.User;
import com.evileye2002.real_timechatapp.utilities.Const;
import com.evileye2002.real_timechatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddFriendActivity extends AppCompatActivity {
    ActivityAddFriendBinding binding;
    PreferenceManager manager;
    DocumentReference currentUser;
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
        currentUserID = manager.getString(Const.ID);
        currentUser = Const.userDoc(currentUserID);
        loading(false);
    }

    void setListener() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.inputSearch.addTextChangedListener(new TextWatcher() {
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

    void getUsers(String s) {
        loading(true);
        Const.user_collection.get().addOnCompleteListener(task -> {
            loading(false);
            List<User> userList = new ArrayList<>();
            boolean isExist = task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0;
            if (isExist) {
                for (QueryDocumentSnapshot snapshot : task.getResult()) {
                    if (snapshot.getId().equals(currentUserID)) {
                        continue;
                    }

                    if (snapshot.getString(Const.FRIENDS) != null) {
                        if (snapshot.getString(Const.FRIENDS).contains(currentUserID))
                            continue;
                    }

                    if (snapshot.getString(Const.NAME).contains(s)) {
                        User user = snapshot.toObject(User.class);
                        user.id = snapshot.getId();
                        userList.add(user);
                    }
                }
                if (userList.size() > 0) {
                    AddFriendAdapter adapter = new AddFriendAdapter(userList, user -> {
                        //Request add friend
                        //Delete user from list

                        String userFriends = user.friends != null ? user.friends : "";
                        Const.user_collection.document(user.id).update(Const.FRIENDS, userFriends + currentUserID + ",");

                        currentUser.get().addOnCompleteListener(task1 -> {
                            String currentUserFriends = task1.getResult().getString(Const.FRIENDS) != null ? task1.getResult().getString(Const.FRIENDS) : "";
                            currentUser.update(Const.FRIENDS, currentUserFriends + user.id + ",");
                        });
                    });
                    binding.recyclerView.setAdapter(adapter);
                }
            }
        });
    }
}