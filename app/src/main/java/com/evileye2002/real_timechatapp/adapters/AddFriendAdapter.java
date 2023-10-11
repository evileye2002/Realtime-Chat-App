package com.evileye2002.real_timechatapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.evileye2002.real_timechatapp.databinding.ItemAddFriendBinding;
import com.evileye2002.real_timechatapp.listeners.UserListener;
import com.evileye2002.real_timechatapp.models.User;
import com.evileye2002.real_timechatapp.utilities._funct;

import java.util.List;

public class AddFriendAdapter extends RecyclerView.Adapter<AddFriendAdapter.AddFriendViewHolder>{
    List<User> userList;
    final UserListener userListener;

    public AddFriendAdapter(List<User> userList, UserListener userListener) {
        this.userList = userList;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public AddFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAddFriendBinding binding = ItemAddFriendBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AddFriendViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AddFriendViewHolder holder, int position) {
        holder.setData(userList.get(position));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //ViewHolder
    class AddFriendViewHolder extends RecyclerView.ViewHolder{
        ItemAddFriendBinding binding;
        String currentUserID;

        public AddFriendViewHolder(ItemAddFriendBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(User user){
            String friendRequestReceived = user.friendRequestReceived != null ? user.friendRequestReceived.toString() : "";
            if(friendRequestReceived.contains(currentUserID)){
                binding.imageAdd.setVisibility(View.GONE);
                binding.imageCancel.setVisibility(View.VISIBLE);
                binding.imageCancel.setOnClickListener(v -> {
                    userListener.onItemClick(user);
                });
            }

            binding.imageProfile.setImageBitmap(_funct.stringToBitmap(user.image));
            binding.textName.setText(user.name);
            binding.imageAdd.setOnClickListener(v -> {
                userListener.onItemClick(user);
                userList.remove(user);
                notifyDataSetChanged();
            });
        }
    }
}
