package com.evileye2002.real_timechatapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.evileye2002.real_timechatapp.databinding.ItemDateBinding;
import com.evileye2002.real_timechatapp.databinding.ItemReceivedMessageBinding;
import com.evileye2002.real_timechatapp.databinding.ItemSendMessageBinding;
import com.evileye2002.real_timechatapp.models.ChatMessage;
import com.evileye2002.real_timechatapp.models.Conversation;
import com.evileye2002.real_timechatapp.models.Members;
import com.evileye2002.real_timechatapp.models.User;
import com.evileye2002.real_timechatapp.utilities.Const;
import com.evileye2002.real_timechatapp.utilities.Funct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    final List<ChatMessage> chatMessageList;
    List<String> listDateFirst;
    List<String> listTimeLast;
    List<Members> memberList;
    final String currentUserID;
    final int VIEW_TYPE_SENT = 1;
    final int VIEW_TYPE_RECEIVED = 2;
    final int VIEW_TYPE_DATE = 3;

    public ChatAdapter(List<ChatMessage> chatMessageList, String currentUserID,List<Members> memberList) {
        this.chatMessageList = chatMessageList;
        this.currentUserID = currentUserID;
        this.memberList = memberList;

        listDateFirst = new ArrayList<>();
        listTimeLast = new ArrayList<>();

        for (ChatMessage chat : chatMessageList) {
            String dates = listDateFirst.toString();
            if (dates.contains(chat.timestamp.split("-")[0])) {
                continue;
            }
            listDateFirst.add(chat.timestamp);
        }

        Collections.reverse(chatMessageList);
        for (ChatMessage chat : chatMessageList) {
            String times = listTimeLast.toString();
            if (times.contains(chat.timestamp.split("-")[0])) {
                continue;
            }
            listTimeLast.add(chat.timestamp);
        }
        Collections.reverse(chatMessageList);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT)
            return new SendMessageViewHolder(
                    ItemSendMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );
        else
            return new ReceivedMessageViewHolder(
                    ItemReceivedMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT)
            ((SendMessageViewHolder) holder).setData(chatMessageList.get(position));
        else
            ((ReceivedMessageViewHolder) holder).setData(chatMessageList.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessageList.get(position).senderID.equals(currentUserID))
            return VIEW_TYPE_SENT;
        else
            return VIEW_TYPE_RECEIVED;
    }

    //ViewHolder
    class DateViewHolder extends RecyclerView.ViewHolder {
        ItemDateBinding binding;

        public DateViewHolder(ItemDateBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(ChatMessage chat) {
            /*String currentDate = Funct.dateToString(new Date(),"dd/MM/yyyy-HH:mm:ss");
            String date = currentDate.split("-")[0];
            String[] dates = date.split("/");
            String time = currentDate.split("-")[1];

            String finalTimestamp = "";
            if(!currentDate.contains(dates[2])){
                finalTimestamp = Funct.dateToString(Funct.stringToDate(chat.timestamp,"dd/MM/yyyy-HH:mm:ss"),"dd THG.MM, yyyy");
            }
            if(!currentDate.contains(dates[1] + "/" + dates[2])){
                finalTimestamp = Funct.dateToString(Funct.stringToDate(chat.timestamp,"dd/MM/yyyy-HH:mm:ss"),"dd, TH.MM");
            }
            if(currentDate.contains())


            binding.textDate.setText(finalTimestamp);*/
        }
    }

    class SendMessageViewHolder extends RecyclerView.ViewHolder {
        ItemSendMessageBinding binding;

        public SendMessageViewHolder(ItemSendMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(ChatMessage chat) {
            String dateF = setDateFilter(chat.timestamp);
            if (dateF != null) {
                binding.textDate.setText(dateF);
                binding.layoutDate.setVisibility(View.VISIBLE);
            }

            String timeF = setTimeFilter(chat.timestamp);
            if (timeF != null) {
                binding.textTime.setText(timeF);
                binding.textTime.setVisibility(View.VISIBLE);
            }
            binding.textMessage.setText(chat.message);
        }
    }

    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        ItemReceivedMessageBinding binding;

        public ReceivedMessageViewHolder(ItemReceivedMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(ChatMessage chat) {
            String dateF = setDateFilter(chat.timestamp);
            if (dateF != null) {
                binding.textDate.setText(dateF);
                binding.layoutDate.setVisibility(View.VISIBLE);
            }

            String timeF = setTimeFilter(chat.timestamp);
            if (timeF != null) {
                binding.textTime.setText(timeF);
                binding.textTime.setVisibility(View.VISIBLE);
            }
            binding.textMessage.setText(chat.message);

            for (Members member : memberList){
                if(member.id.equals(currentUserID))
                    continue;
                if(member.id.equals(chat.senderID)){
                    binding.imageProfile.setImageBitmap(Funct.stringToBitmap(member.image));
                    break;
                }
            }

            /*Const.userDoc(chat.senderID).get().addOnCompleteListener(task -> {
                boolean isValid = task.isSuccessful() && task.getResult() != null;
                if (isValid)
                    binding.imageProfile.setImageBitmap(Funct.stringToBitmap(task.getResult().getString(Const.IMAGE)));
            });*/

        }
    }

    String setDateFilter(String chatDateTime) {
        for (String date : listDateFirst) {
            if (chatDateTime.equals(date)) {
                String dateF = Funct.dateToString(Funct.stringToDate(date, "dd/MM/yyyy"), "dd/MM, yyyy");
                listDateFirst.remove(date);
                return dateF;
            }
        }
        return null;
    }

    String setTimeFilter(String chatDateTime) {
        for (String time : listTimeLast) {
            if (chatDateTime.equals(time)) {
                String timeF = Funct.dateToString(Funct.stringToDate(time, "dd/MM/yyyy-HH:mm:ss"), "HH:mm");
                listTimeLast.remove(time);
                return timeF;
            }
        }
        return null;
    }
}
