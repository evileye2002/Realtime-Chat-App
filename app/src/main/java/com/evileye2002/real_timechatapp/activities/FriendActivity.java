package com.evileye2002.real_timechatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.evileye2002.real_timechatapp.adapters.ConversationAdapter;
import com.evileye2002.real_timechatapp.databinding.ActivityFriendBinding;
import com.evileye2002.real_timechatapp.models.Conversation;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListener();
        getConversations();
    }

    void init() {
        manager = new PreferenceManager(getApplicationContext());
        currentUser = Const.currentUser(manager.getString(Const.KEY_USER_ID));
    }

    void setListener() {

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

    void getConversations() {
        currentUser.get()
                .addOnCompleteListener(task -> {
                    boolean isExist = task.isSuccessful() && task.getResult() != null && task.getResult().getString(Const.KEY_COLLECTION_CONVERSATIONS) != null;
                    if (isExist) {
                        String currentUSerConversations = task.getResult().getString(Const.KEY_COLLECTION_CONVERSATIONS);
                        Const.firestore.collection(Const.KEY_COLLECTION_USERS)
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    List<Conversation> conversationList = new ArrayList<>();
                                    boolean isExist1 = task1.isSuccessful() && task1.getResult() != null;
                                    if (isExist1) {
                                        for (QueryDocumentSnapshot snapshot : task1.getResult()) {
                                            if (currentUSerConversations.contains(snapshot.getId())) {
                                                Conversation con = snapshot.toObject(Conversation.class);
                                                conversationList.add(con);

                                                ConversationAdapter adapter = new ConversationAdapter(manager.getString(Const.KEY_USER_ID), conversationList, conversation -> {

                                                });
                                                binding.recyclerView.setAdapter(adapter);
                                            }
                                        }
                                    }
                                });
                    }
                });
    }
}