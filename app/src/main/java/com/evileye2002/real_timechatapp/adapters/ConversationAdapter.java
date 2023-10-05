package com.evileye2002.real_timechatapp.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.evileye2002.real_timechatapp.databinding.ItemConversationBinding;
import com.evileye2002.real_timechatapp.listeners.ConversationListener;
import com.evileye2002.real_timechatapp.models.Conversation;
import com.evileye2002.real_timechatapp.models.User;
import com.evileye2002.real_timechatapp.utilities.Const;
import com.evileye2002.real_timechatapp.utilities.Funct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {
    String currentUserID;
    List<Conversation> conversationList;
    final ConversationListener conversationListener;

    public ConversationAdapter(String currentUserID, List<Conversation> conversationList, ConversationListener conversationListener) {
        this.currentUserID = currentUserID;
        this.conversationList = conversationList;
        this.conversationListener = conversationListener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemConversationBinding binding = ItemConversationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ConversationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.setData(conversationList.get(position));
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    //ViewHolder
    class ConversationViewHolder extends RecyclerView.ViewHolder {
        ItemConversationBinding binding;

        public ConversationViewHolder(ItemConversationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(Conversation conversation) {
            if (conversation.isGroup) {
                binding.imageConversation.setImageBitmap(Funct.stringToBitmap(conversation.image));
                binding.textName.setText(conversation.name);
                binding.getRoot().setOnClickListener(v -> conversationListener.onItemClick(conversation, null));
            } else {
                List<String> member = new ArrayList<>(Arrays.asList(conversation.members.split(",")));
                member.remove(currentUserID);
                Const.userDoc(member.get(0)).get().addOnCompleteListener(task -> {
                    User user = task.getResult().toObject(User.class);
                    user.id = task.getResult().getId();
                    binding.imageConversation.setImageBitmap(Funct.stringToBitmap(user.image));
                    binding.textName.setText(user.name);
                    binding.getRoot().setOnClickListener(v -> conversationListener.onItemClick(null, user));
                });
            }
            if (conversation.lastMessage != null) {
                String lastSenderName = conversation.lastSenderID.equals(currentUserID) ? "Bạn" : conversation.lastSenderName;
                //String lastTimestamp = Funct.dateToString(Funct.stringToDate(conversation.lastTimestamp, "dd/MM/yyyy-HH:mm:ss"), "HH:mm");
                binding.textLastMessage.setText(lastSenderName + ": " + conversation.lastMessage);
            }
        }
    }
}
