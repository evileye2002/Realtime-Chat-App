package com.evileye2002.real_timechatapp.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.evileye2002.real_timechatapp.databinding.ItemConversationBinding;
import com.evileye2002.real_timechatapp.listeners.ConversationListener;
import com.evileye2002.real_timechatapp.models.Conversation;

import java.util.ArrayList;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>{
    final String currentUserID;
    List<Conversation> listConversation;
    final ConversationListener conversationListener;

    public ConversationAdapter(String currentUserID, List<Conversation> listConversation, ConversationListener conversationListener) {
        this.currentUserID = currentUserID;
        this.listConversation = listConversation;
        this.conversationListener = conversationListener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    //ViewHolder
    class ConversationViewHolder extends RecyclerView.ViewHolder{
        final ItemConversationBinding binding;

        public ConversationViewHolder(ItemConversationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
