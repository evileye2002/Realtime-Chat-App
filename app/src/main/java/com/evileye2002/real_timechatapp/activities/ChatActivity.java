package com.evileye2002.real_timechatapp.activities;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.evileye2002.real_timechatapp.R;
import com.evileye2002.real_timechatapp.adapters.ChatAdapter;
import com.evileye2002.real_timechatapp.databinding.ActivityChatBinding;
import com.evileye2002.real_timechatapp.models.ChatMessage;
import com.evileye2002.real_timechatapp.models.Conversation;
import com.evileye2002.real_timechatapp.models.User;
import com.evileye2002.real_timechatapp.utilities._const;
import com.evileye2002.real_timechatapp.utilities._firestore;
import com.evileye2002.real_timechatapp.utilities._funct;
import com.evileye2002.real_timechatapp.utilities.PreferenceManager;
import com.evileye2002.real_timechatapp.utilities.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    ActivityChatBinding binding;
    PreferenceManager manager;
    String currentUserID, currentConID;
    User receiverUser;
    Conversation currentCon;
    List<ChatMessage> mainMessageList;
    List<ChatMessage> pendingList;
    List<Conversation.Members> membersDetails;
    int countPending = 0;
    ChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListener();
        checkConversation();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    void init() {
        manager = new PreferenceManager(getApplicationContext());
        currentUserID = manager.getString(_const.ID);
        receiverUser = (User) getIntent().getSerializableExtra(_const.USER);
        currentCon = (Conversation) getIntent().getSerializableExtra(_const.CONVERSATION);
        currentConID = currentCon != null ? currentCon.id : "";
        mainMessageList = new ArrayList<>();
        pendingList = new ArrayList<>();
        membersDetails = new ArrayList<>();
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

    void resendMessage(ChatMessage chat) {
        if (chat.message.isEmpty())
            return;

        //String pendingID = Integer.toString(countPending + 1);
        String msg = chat.message;
        pendingList.remove(chat);
        mainMessageList.remove(chat);
        //pendingSend(pendingID, msg);

        Timestamp getTimestamp = new Timestamp(result -> {
            if (result.equals("")) {
                if (binding.internetState.getVisibility() != View.VISIBLE)
                    binding.internetState.setVisibility(View.VISIBLE);
                return;
            }
            if (binding.internetState.getVisibility() != View.GONE)
                binding.internetState.setVisibility(View.GONE);
            HashMap<String, Object> message = new HashMap<>();
            message.put(_const.SENDER_ID, currentUserID);
            message.put(_const.MESSAGE, msg);
            message.put(_const.TIMESTAMP, result);
            //message.put(_const.PENDING_ID, pendingID);

            _firestore.allChats(currentConID).add(message).addOnCompleteListener(task -> {
                boolean isValid = task.isSuccessful() && task.getResult() != null;
                if (isValid)
                    updateConversation(msg, result);
            });
        });
        getTimestamp.execute();
    }

    void sendMessage() {
        if (binding.inputMessage.getText().toString().isEmpty())
            return;

        //String pendingID = Integer.toString(countPending + 1);
        String msg = binding.inputMessage.getText().toString();
        binding.inputMessage.setText(null);
        //pendingSend(pendingID, msg);

        Timestamp getTimestamp = new Timestamp(result -> {
            if (result.equals("")) {
                if (binding.internetState.getVisibility() != View.VISIBLE)
                    binding.internetState.setVisibility(View.VISIBLE);
                return;
            }

            if (binding.internetState.getVisibility() != View.GONE)
                binding.internetState.setVisibility(View.GONE);

            HashMap<String, Object> message = new HashMap<>();
            message.put(_const.SENDER_ID, currentUserID);
            message.put(_const.MESSAGE, msg);
            message.put(_const.TIMESTAMP, result);
            //message.put(_const.PENDING_ID, pendingID);

            _firestore.allChats(currentConID).add(message).addOnCompleteListener(task -> {
                if (task.isSuccessful())
                    updateConversation(msg, result);
            });
        });
        getTimestamp.execute();
    }

    void pendingSend(String pendingID, String msg) {
        ChatMessage pendingChat = new ChatMessage();
        pendingChat.pendingID = pendingID;
        //pendingChat.timestamp = _funct.dateToString(new Date(), _const.dateFormat);
        pendingChat.message = msg;
        pendingChat.senderID = currentUserID;
        pendingChat.status = "pending";

        countPending++;
        pendingList.add(pendingChat);
        mainMessageList.add(pendingChat);
        binding.inputMessage.setText(null);
        updateChat();
    }

    void updateConversation(String msg, Date timestamp) {
        HashMap<String, Object> update = new HashMap<>();
        update.put(_const.LAST_SENDER_ID, currentUserID);
        update.put(_const.LAST_MESSAGE, msg);
        update.put(_const.LAST_TIMESTAMP, timestamp);

        _firestore.singleCon(currentConID).update(update);
    }

    void checkConversation() {
        if (currentCon == null) {
            getDetails(false);
            getConversation();
            return;
        }
        getDetails(currentCon.isGroup);
        membersDetails = currentCon.membersDetails;
        listenMessage();
    }

    void getDetails(boolean isGroup) {
        if (isGroup) {
            binding.imageConversation.setImageBitmap(_funct.stringToBitmap(currentCon.image));
            binding.textName.setText(currentCon.name);
            return;
        }
        if(receiverUser != null){
            binding.imageConversation.setImageBitmap(_funct.stringToBitmap(receiverUser.image));
            binding.textName.setText(receiverUser.name);
            return;
        }
        for (Conversation.Members member : currentCon.membersDetails){
            if(member.id.equals(currentUserID))
                continue;
            binding.imageConversation.setImageBitmap(_funct.stringToBitmap(member.image));
            binding.textName.setText(member.name);
        }
    }

    String createConID(String ID1, String ID2) {
        if (ID1.hashCode() < ID2.hashCode())
            return ID1 + "_" + ID2;
        else
            return ID2 + "_" + ID1;
    }

    void getConversation() {
        currentConID = createConID(currentUserID, receiverUser.id);
        _firestore.singleCon(currentConID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                currentCon = task.getResult().toObject(Conversation.class);
                if (currentCon == null) {
                    createCon();
                    return;
                }
                membersDetails = currentCon.membersDetails != null ? currentCon.membersDetails : new ArrayList<>();
                listenMessage();
            }
        });
    }

    void createCon() {
        Conversation.Members currentUser = new Conversation.Members(currentUserID, manager.getString(_const.NAME), manager.getString(_const.IMAGE));
        Conversation.Members member = new Conversation.Members(receiverUser.id, receiverUser.name, receiverUser.image);
        membersDetails.add(currentUser);
        membersDetails.add(member);

        HashMap<String, Object> conversation = new HashMap<>();
        conversation.put(_const.IS_GROUP, false);
        conversation.put(_const.MEMBER_LIST, Arrays.asList(currentUserID, receiverUser.id));
        conversation.put(_const.MEMBERS_DETAILS, membersDetails);

        _firestore.singleCon(currentConID).set(conversation).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                listenMessage();
            }
        });
    }

    void listenMessage() {
        loading(true);
        _firestore.allChats(currentConID)
                .orderBy(_const.TIMESTAMP, Query.Direction.ASCENDING)
                .addSnapshotListener(messageListener);
    }

    final EventListener<QuerySnapshot> messageListener = (value, error) -> {
        if (error != null)
            return;
        if (value != null) {
            loading(false);
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                ChatMessage newChat = documentChange.getDocument().toObject(ChatMessage.class);
                newChat.id = documentChange.getDocument().getId();
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    /*if (pendingList.size() > 0) {
                        for (ChatMessage chatMain : mainMessageList) {
                            if (chatMain.pendingID.equals(newChat.pendingID) && chatMain.senderID.equals(currentUserID)) {
                                pendingList.removeIf(pendingChat -> chatMain.pendingID.equals(pendingChat.pendingID));
                                chatMain.id = newChat.id;
                                chatMain.timestamp = newChat.timestamp;
                                chatMain.status = "complete";
                                adapter = new ChatAdapter(mainMessageList, currentUserID, membersDetails, this::showDialog);
                                binding.recyclerView.setAdapter(adapter);
                                return;
                            }
                        }
                    }
                    countPending++;*/

                    mainMessageList.add(newChat);
                }

                if (documentChange.getType() == DocumentChange.Type.REMOVED) {
                    for (ChatMessage chatMain : mainMessageList) {
                        if (chatMain.id.equals(newChat.id)) {
                            int i = mainMessageList.indexOf(chatMain);
                            mainMessageList.remove(chatMain);
                            adapter.notifyItemRemoved(i);
                            return;
                        }
                    }
                }
            }
            updateChat();
        }
    };

    void updateChat() {
        if(adapter == null){
            adapter = new ChatAdapter(mainMessageList, currentUserID, membersDetails, this::showDialog);
            binding.recyclerView.setAdapter(adapter);
        }
        adapter.notifyItemRangeInserted(mainMessageList.size(), mainMessageList.size());
        if(mainMessageList.size() == 0){
            binding.recyclerView.smoothScrollToPosition(mainMessageList.size());
            return;
        }
        binding.recyclerView.smoothScrollToPosition(mainMessageList.size() - 1);
    }

    void showDialog(ChatMessage chat) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_dialog);

        LinearLayout copy = dialog.findViewById(R.id.layoutCopy);
        LinearLayout resend = dialog.findViewById(R.id.layoutResend);
        LinearLayout delete = dialog.findViewById(R.id.layoutDelete);

        if (chat.status != null)
            if (chat.status.equals("pending"))
                resend.setVisibility(View.VISIBLE);
        if (!chat.senderID.equals(currentUserID))
            delete.setVisibility(View.GONE);

        copy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", chat.message);
            clipboard.setPrimaryClip(clip);
            dialog.dismiss();
        });
        resend.setOnClickListener(v -> {
            resendMessage(chat);
            dialog.dismiss();
        });
        delete.setOnClickListener(v -> {
            if (chat.id != null) {
                _firestore.singleChat(currentConID, chat.id).delete();
            }
            if (chat.status != null)
                if (chat.status.equals("pending")) {
                    int i = mainMessageList.indexOf(chat);
                    mainMessageList.remove(chat);
                    adapter.notifyItemRemoved(i);
                }
            dialog.dismiss();
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }
}