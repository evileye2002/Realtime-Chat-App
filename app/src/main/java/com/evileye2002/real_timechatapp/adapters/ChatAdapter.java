package com.evileye2002.real_timechatapp.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.evileye2002.real_timechatapp.R;
import com.evileye2002.real_timechatapp.databinding.ItemChatBinding;
import com.evileye2002.real_timechatapp.databinding.ItemReceivedMessageBinding;
import com.evileye2002.real_timechatapp.databinding.ItemSendMessageBinding;
import com.evileye2002.real_timechatapp.listeners.ChatListener;
import com.evileye2002.real_timechatapp.models.ChatMessage;
import com.evileye2002.real_timechatapp.models.Conversation;
import com.evileye2002.real_timechatapp.utilities._funct;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    final List<ChatMessage> chatMessageList;
    List<ChatMessage> chatMessageListOld;
    List<Date> listDayFirst;
    List<Date> listTimeLast;
    List<Conversation.Members> memberList;
    ChatListener listener;
    final String currentUserID;
    Drawable bg_complete;
    Drawable bg_unComplete;

    public ChatAdapter(List<ChatMessage> chatMessageList, String currentUserID, List<Conversation.Members> memberList, ChatListener listener) {
        this.chatMessageList = chatMessageList;
        this.chatMessageListOld = chatMessageList;
        this.currentUserID = currentUserID;
        this.memberList = memberList;
        this.listener = listener;

        sortDate();

        /*for (ChatMessage chat : chatMessageList) {
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
        Collections.reverse(chatMessageList);*/
    }

    void sortDate(){
        listDayFirst = new ArrayList<>();
        listTimeLast = new ArrayList<>();
        Date preDate = null;
        Date preTime = null;
        for (ChatMessage chat : chatMessageList) {
            if (preDate == null || !isSameDay(preDate, chat.timestamp)) {
                listDayFirst.add(chat.timestamp);
            }
            preDate = chat.timestamp;
        }
        Collections.reverse(chatMessageList);
        for (ChatMessage chat : chatMessageList) {
            if (preTime == null || !isSameDay(preTime, chat.timestamp)) {
                listTimeLast.add(chat.timestamp);
            }
            preTime = chat.timestamp;
        }
        Collections.reverse(chatMessageList);
    }

    Boolean isSameDay(Date date1, Date date2) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        int day1 = cal.get(Calendar.DAY_OF_MONTH);
        int month1 = cal.get(Calendar.MONTH);
        int year1 = cal.get(Calendar.YEAR);

        cal.setTime(date2);
        int day2 = cal.get(Calendar.DAY_OF_MONTH);
        int month2 = cal.get(Calendar.MONTH);
        int year2 = cal.get(Calendar.YEAR);

        return (day1 == day2) && (month1 == month2) && (year1 == year2);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        /*if (viewType == VIEW_TYPE_SENT)
            return new SendMessageViewHolder(
                    ItemSendMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );
        else
            return new ReceivedMessageViewHolder(
                    ItemReceivedMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );*/
        return new ChatViewHolder(ItemChatBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        /*if (getItemViewType(position) == VIEW_TYPE_SENT)
            ((SendMessageViewHolder) holder).setData(chatMessageList.get(position));
        else
            ((ReceivedMessageViewHolder) holder).setData(chatMessageList.get(position));*/
        if(chatMessageListOld.size() < chatMessageList.size())
            sortDate();
        ((ChatViewHolder) holder).setData(chatMessageList.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
       /* if (chatMessageList.get(position).senderID.equals(currentUserID))
            return VIEW_TYPE_SENT;
        else
            return VIEW_TYPE_RECEIVED;*/
        return 0;
    }

    //ViewHolder
    class ChatViewHolder extends RecyclerView.ViewHolder {
        ItemChatBinding binding;

        public ChatViewHolder(ItemChatBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            /*bg_complete = binding.getRoot().getResources().getDrawable(R.drawable.bg_complete);
            bg_unComplete = binding.getRoot().getResources().getDrawable(R.drawable.bg_un_complete);*/
        }

        void setData(ChatMessage chat) {
            binding.getRoot().setOnLongClickListener(v -> {
                listener.onLongClick(chat);
                return true;
            });
            if (chat.senderID.equals(currentUserID)) {
                binding.textSender.setText(chat.message);
                return;
            }

            //Receiver
            binding.layoutReceiver.setVisibility(View.VISIBLE);
            binding.layoutSender.setVisibility(View.GONE);
            binding.textReceiver.setText(chat.message);

            for (Conversation.Members member : memberList) {
                if (member.id.equals(currentUserID))
                    continue;
                if (member.id.equals(chat.senderID)) {
                    binding.imageProfile.setImageBitmap(_funct.stringToBitmap(member.image));
                    break;
                }
            }
        }
    }

    class SendMessageViewHolder extends RecyclerView.ViewHolder {
        ItemSendMessageBinding binding;

        public SendMessageViewHolder(ItemSendMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            bg_complete = binding.getRoot().getResources().getDrawable(R.drawable.bg_complete);
            bg_unComplete = binding.getRoot().getResources().getDrawable(R.drawable.bg_un_complete);
        }

        void setData(ChatMessage chat) {
            /*String dateF = setDateFilter(chat.timestamp);
            if (dateF != null) {
                binding.textDate.setText(dateF);
                binding.layoutDate.setVisibility(View.VISIBLE);
            }

            String timeF = setTimeFilter(chat.timestamp);
            if (timeF != null) {
                binding.textTime.setText(timeF);
                binding.textTime.setVisibility(View.VISIBLE);
            }*/
            if (chat.status != null) {
                binding.status.setVisibility(View.VISIBLE);
                if (chat.status.equals("pending"))
                    binding.status.setBackgroundDrawable(bg_unComplete);
                if (chat.status.equals("complete")) {
                    binding.status.setBackgroundDrawable(bg_complete);
                    binding.status.setImageResource(R.drawable.ic_check);
                    chat.status = "0";
                } else if (chat.status.equals("0"))
                    binding.status.setVisibility(View.GONE);
            }
            binding.textMessage.setText(chat.message);
            binding.getRoot().setOnLongClickListener(v -> {
                listener.onLongClick(chat);
                return true;
            });
        }
    }

    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        ItemReceivedMessageBinding binding;

        public ReceivedMessageViewHolder(ItemReceivedMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(ChatMessage chat) {
            /*String dateF = setDateFilter(chat.timestamp);
            if (dateF != null) {
                binding.textDate.setText(dateF);
                binding.layoutDate.setVisibility(View.VISIBLE);
            }

            String timeF = setTimeFilter(chat.timestamp);
            if (timeF != null) {
                binding.textTime.setText(timeF);
                binding.textTime.setVisibility(View.VISIBLE);
            }*/
            binding.textMessage.setText(chat.message);
            binding.getRoot().setOnLongClickListener(v -> {
                listener.onLongClick(chat);
                return true;
            });

            for (Conversation.Members member : memberList) {
                if (member.id.equals(currentUserID))
                    continue;
                if (member.id.equals(chat.senderID)) {
                    binding.imageProfile.setImageBitmap(_funct.stringToBitmap(member.image));
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
        /*for (String date : listDateFirst) {
            if (chatDateTime.equals(date)) {
                String dateF = _funct.dateToString(_funct.stringToDate(date, "dd/MM/yyyy"), "dd/MM, yyyy");
                listDateFirst.remove(date);
                return dateF;
            }
        }*/
        return null;
    }

    String setTimeFilter(String chatDateTime) {
        /*for (String time : listTimeLast) {
            if (chatDateTime.equals(time)) {
                String timeF = _funct.dateToString(_funct.stringToDate(time, "dd/MM/yyyy-HH:mm:ss"), "HH:mm");
                listTimeLast.remove(time);
                return timeF;
            }
        }*/
        return null;
    }
}
