package com.evileye2002.real_timechatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.evileye2002.real_timechatapp.utilities.Timestamp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

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
    Conversation currentCon;
    String currentConID;
    List<ChatMessage> mainMessageList;
    List<ChatMessage> pendingList;
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
        currentCon = (Conversation) getIntent().getSerializableExtra(Const.CONVERSATION);
        currentConID = currentCon != null ? currentCon.id : "";
        mainMessageList = new ArrayList<>();
        pendingList = new ArrayList<>();
        memberList = new ArrayList<>();
    }

    void setListener() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
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

    void sendMessage() {
        /*String msg = binding.inputMessage.getText().toString();
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
        }*/
        if (binding.inputMessage.getText().toString().isEmpty())
            return;
        /*String lastTimestamp1 = listPending.get(listPending.size()).timestamp;
        String lastTimestamp2 = listPending.get(listPending.size() - 1).timestamp;
        String lastTimestamp3 = listPending.get(listPending.size() - 2).timestamp;*/

        String pendingID = Integer.toString(mainMessageList.size() + 1);
        String msg = binding.inputMessage.getText().toString();
        pendingSend(pendingID);

        Timestamp getTimestamp = new Timestamp(result -> {
            String timestamp = result;
            
            if(timestamp.equals("")){
                if(binding.internetState.getVisibility() != View.VISIBLE)
                    binding.internetState.setVisibility(View.VISIBLE);
                return;
            }

            if(binding.internetState.getVisibility() != View.GONE)
                binding.internetState.setVisibility(View.GONE);
            HashMap<String, Object> message = new HashMap<>();
            message.put(Const.SENDER_ID, currentUserID);
            message.put(Const.MESSAGE, msg);
            message.put(Const.TIMESTAMP, timestamp);
            message.put(Const.PENDING_ID, pendingID);

            if (currentConID.equals("")) {
                addConversation(msg, timestamp, message);
            } else {
                Const.chat_collection(currentConID).add(message).addOnCompleteListener(task -> {
                    boolean isValid = task.isSuccessful() && task.getResult() != null;
                    if (isValid)
                        updateConversation(msg, timestamp);
                });
            }
        });
        getTimestamp.execute();
    }

    void pendingSend(String pendingID) {
        ChatMessage pendingChat = new ChatMessage();
        pendingChat.pendingID = pendingID;
        pendingChat.timestamp = Funct.dateToString(new Date(), Const.dateFormat);
        pendingChat.message = binding.inputMessage.getText().toString();
        pendingChat.senderID = currentUserID;
        pendingChat.status = "pending";

        pendingList.add(pendingChat);
        mainMessageList.add(pendingChat);
        updateChat();
        binding.inputMessage.setText(null);
    }

    void addConversation(String msg, String timestamp, HashMap<String, Object> message) {
        Members currentUser = new Members(currentUserID, manager.getString(Const.NAME), manager.getString(Const.IMAGE));
        Members member = new Members(receiverUser.id, receiverUser.name, receiverUser.image);
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
            if (isValid) {
                String id = task.getResult().getId();
                currentConID = id == null ? "" : id;
                if (!currentConID.equals(""))
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

        Const.conDoc(currentConID).update(update);
    }

    void checkConversation() {
        if (currentConID.equals("")) {
            getDetails(false);
            getConversation();
            return;
        }
        getDetails(currentCon.isGroup);
        memberList = currentCon.memberList;
        listenMessage();
    }

    void getDetails(boolean isGroup) {
        if (isGroup) {
            binding.imageConversation.setImageBitmap(Funct.stringToBitmap(currentCon.image));
            binding.textName.setText(currentCon.name);
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
        loading(false);
        boolean isValid = task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0;
        if (isValid) {
            String id = task.getResult().getDocuments().get(0).getId();
            currentConID = id == null ? "" : id;
            if (!currentConID.equals("")) {
                currentCon = task.getResult().getDocuments().get(0).toObject(Conversation.class);
                memberList = currentCon.memberList;
                listenMessage();
            }
        }
    };

    void listenMessage() {
        loading(true);
        Const.chat_collection(currentConID).orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener(messageListener);
    }

    final EventListener<QuerySnapshot> messageListener = (value, error) -> {
        if (error != null)
            return;
        if (value != null) {
            loading(false);
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                ChatMessage newChat = documentChange.getDocument().toObject(ChatMessage.class);
                /*if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chat = documentChange.getDocument().toObject(ChatMessage.class);
                    chatMessageList.add(chat);
                }*/
                if(documentChange.getType() == DocumentChange.Type.REMOVED){
                    mainMessageList.removeIf(chatMain -> chatMain.pendingID.equals(newChat.pendingID));
                    updateChat();
                    return;
                }
                if(documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for (ChatMessage chatMain : mainMessageList){
                        if(chatMain.pendingID.equals(newChat.pendingID)){
                            mainMessageList.set(mainMessageList.indexOf(chatMain),newChat);
                            break;
                        }
                    }
                    updateChat();
                    return;
                }
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    /*if(!newChat.senderID.equals(currentUserID) && mainMessageList.size() > 0){
                        mainMessageList.add(newChat);
                        mainMessageList.sort(Comparator.comparing(o -> o.timestamp));
                        adapter = new ChatAdapter(mainMessageList, currentUserID, memberList);
                        binding.recyclerView.setAdapter(adapter);
                        binding.recyclerView.smoothScrollToPosition(mainMessageList.size() - 1);
                        return;
                    }*/

                    if (pendingList.size() > 0) {
                        mainMessageList.forEach(chatMain -> {
                            if(chatMain.status != null){
                                if (chatMain.pendingID.equals(newChat.pendingID) && !chatMain.status.equals("complete")) {
                                    pendingList.removeIf(pendingChat -> chatMain.pendingID.equals(pendingChat.pendingID));
                                    chatMain.timestamp = newChat.timestamp;
                                    chatMain.status = "complete";
                                    chatMain.pendingID = "";
                                    updateChat();
                                }
                            }
                        });
                        /*for (ChatMessage pendingChat : mainMessageList) {
                            if (pendingChat.pendingID.equals(""))
                                continue;
                            if (pendingChat.status == null)
                                continue;
                            if (pendingChat.pendingID.equals(newChat.pendingID) && !pendingChat.status.equals("1")) {
                                pendingList.removeIf(chat2 -> pendingChat.pendingID.equals(chat2.pendingID));

                                pendingChat.timestamp = newChat.timestamp;
                                pendingChat.status = "1";
                                pendingChat.pendingID = "";

                                adapter = new ChatAdapter(mainMessageList, currentUserID, memberList);
                                binding.recyclerView.setAdapter(adapter);
                                binding.recyclerView.smoothScrollToPosition(mainMessageList.size() - 1);
                            }
                        }*/
                        return;
                    }
                    mainMessageList.add(newChat);
                }
            }
            if (mainMessageList.size() > 0) {
                updateChat();
            }
        }
    };

    void updateChat(){
        adapter = new ChatAdapter(mainMessageList, currentUserID, memberList);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.smoothScrollToPosition(mainMessageList.size() - 1);
    }
}