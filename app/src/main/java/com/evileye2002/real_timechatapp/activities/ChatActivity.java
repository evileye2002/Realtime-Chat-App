package com.evileye2002.real_timechatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.evileye2002.real_timechatapp.adapters.ChatAdapter;
import com.evileye2002.real_timechatapp.databinding.ActivityChatBinding;
import com.evileye2002.real_timechatapp.models.ChatMessage;
import com.evileye2002.real_timechatapp.models.Conversation;
import com.evileye2002.real_timechatapp.models.Members;
import com.evileye2002.real_timechatapp.models.User;
import com.evileye2002.real_timechatapp.utilities.Const;
import com.evileye2002.real_timechatapp.utilities.Funct;
import com.evileye2002.real_timechatapp.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    ActivityChatBinding binding;
    PreferenceManager manager;
    String currentUserID;
    User receiverUser;
    Conversation conversation;
    String conversationID;
    List<ChatMessage> chatMessageList;
    List<Members> memberList;
    ChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListener();
        checkConversation();
        //listenMessage();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    void init() {
        manager = new PreferenceManager(getApplicationContext());
        currentUserID = manager.getString(Const.ID);
        receiverUser = (User) getIntent().getSerializableExtra(Const.USER);
        conversation = (Conversation) getIntent().getSerializableExtra(Const.CONVERSATION);
        conversationID = conversation != null ? conversation.id : "";
        chatMessageList = new ArrayList<>();
        memberList = new ArrayList<>();
    }

    void setListener() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
        //binding.recyclerView.setAdapter(adapter);
    }

    void sendMessage() {
        String msg = binding.inputMessage.getText().toString();
        String timestamp = Funct.dateToString(new Date(), "dd/MM/yyyy-HH:mm:ss");

        HashMap<String, Object> message = new HashMap<>();
        message.put(Const.SENDER_ID, currentUserID);
        message.put(Const.MESSAGE, msg);
        message.put(Const.TIMESTAMP, timestamp);

        if (conversationID.equals("")) {
            addConversation(msg, timestamp, message);
        } else {
            Const.chat_collection(conversationID).add(message).addOnCompleteListener(task -> {
                boolean isValid = task.isSuccessful() && task.getResult() != null;
                if (isValid)
                    updateConversation(msg, timestamp);
                binding.inputMessage.setText(null);
            });
        }
    }

    void addConversation(String msg, String timestamp, HashMap<String, Object> message) {
        Members currentUser = new Members(currentUserID,manager.getString(Const.NAME),manager.getString(Const.IMAGE));
        Members member = new Members(receiverUser.id,receiverUser.name,receiverUser.image);
        memberList.add(currentUser);
        memberList.add(member);

        HashMap<String, Object> conversation = new HashMap<>();
        conversation.put(Const.IS_GROUP, false);
        conversation.put(Const.COLLECTION_MEMBERS, memberList);

        conversation.put(Const.SENDER_ID, currentUserID);
        conversation.put(Const.RECEIVER_ID, receiverUser.id);

        conversation.put(Const.LAST_SENDER_ID, currentUserID);
        conversation.put(Const.LAST_MESSAGE, msg);
        conversation.put(Const.LAST_TIMESTAMP, timestamp);

        Const.con_collection.add(conversation).addOnCompleteListener(task -> {
            boolean isValid = task.isSuccessful() && task.getResult() != null;
            if (isValid){
                String id = task.getResult().getId();
                conversationID = id == null ? "" : id;
                if (!conversationID.equals(""))
                    listenMessage();
                Const.chat_collection(task.getResult().getId()).add(message);
            }
            binding.inputMessage.setText(null);
        });
    }

    void updateConversation(String msg, String timestamp) {
        HashMap<String, Object> update = new HashMap<>();
        update.put(Const.LAST_SENDER_ID, currentUserID);
        update.put(Const.LAST_MESSAGE, msg);
        update.put(Const.LAST_TIMESTAMP, timestamp);

        Const.conDoc(conversationID).update(update);
    }

    void checkConversation() {
        binding.processBar.setVisibility(View.GONE);
        if (conversationID.equals("")) {
            getDetails(false);
            getConversation();
            binding.recyclerView.setVisibility(View.VISIBLE);
            return;
        }
        getDetails(conversation.isGroup);
        memberList = conversation.memberList;
        listenMessage();
    }

    void getDetails(boolean isGroup) {
        if (isGroup) {
            binding.imageConversation.setImageBitmap(Funct.stringToBitmap(conversation.image));
            binding.textName.setText(conversation.name);
            return;
        }
        binding.imageConversation.setImageBitmap(Funct.stringToBitmap(receiverUser.image));
        binding.textName.setText(receiverUser.name);
    }

    void getConversation() {
        Const.con_collection
                .whereEqualTo(Const.SENDER_ID, currentUserID)
                .whereEqualTo(Const.RECEIVER_ID, receiverUser.id)
                .get()
                .addOnCompleteListener(getConversationID);
        Const.con_collection
                .whereEqualTo(Const.SENDER_ID, receiverUser.id)
                .whereEqualTo(Const.RECEIVER_ID, currentUserID)
                .get()
                .addOnCompleteListener(getConversationID);
    }

    final OnCompleteListener<QuerySnapshot> getConversationID = task -> {
        boolean isValid = task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0;
        if (isValid) {
            String id = task.getResult().getDocuments().get(0).getId();
            conversationID = id == null ? "" : id;
            conversation = task.getResult().getDocuments().get(0).toObject(Conversation.class);
            memberList = conversation.memberList;
            if (!conversationID.equals(""))
                listenMessage();
        }
    };

    void listenMessage() {
        Const.chat_collection(conversationID).addSnapshotListener(messageListener);
    }

    final EventListener<QuerySnapshot> messageListener = (value, error) -> {
        if (error != null)
            return;
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                /*if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chat = documentChange.getDocument().toObject(ChatMessage.class);
                    chatMessageList.add(chat);
                }*/
                ChatMessage chat = documentChange.getDocument().toObject(ChatMessage.class);
                chatMessageList.add(chat);
            }
            if(chatMessageList.size() > 0){
                Collections.sort(chatMessageList, Comparator.comparing(o -> o.timestamp));
                adapter = new ChatAdapter(chatMessageList,currentUserID,memberList);
                binding.recyclerView.setAdapter(adapter);
                binding.recyclerView.smoothScrollToPosition(chatMessageList.size() - 1);
            }

            /*if (size == 0)
                adapter.notifyDataSetChanged();
            else {
                adapter.notifyItemRangeInserted(chatMessageList.size(), chatMessageList.size());
                binding.recyclerView.smoothScrollToPosition(chatMessageList.size() - 1);
            }*/
            binding.recyclerView.setVisibility(View.VISIBLE);
        }
    };
}