package com.evileye2002.real_timechatapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.evileye2002.real_timechatapp.databinding.ItemAddFriendBinding;
import com.evileye2002.real_timechatapp.databinding.ItemFriendBinding;
import com.evileye2002.real_timechatapp.listeners.UserListener;
import com.evileye2002.real_timechatapp.models.User;
import com.evileye2002.real_timechatapp.utilities._funct;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder>{
    List<User> userList;
    final UserListener userListener;

    public FriendAdapter(List<User> userList, UserListener userListener) {
        this.userList = userList;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFriendBinding binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FriendViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        holder.setData(userList.get(position));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //ViewHolder
    class FriendViewHolder extends RecyclerView.ViewHolder{
        ItemFriendBinding binding;

        public FriendViewHolder(ItemFriendBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(User user){
            binding.imageProfile.setImageBitmap(_funct.stringToBitmap(user.image));
            binding.textName.setText(user.name);
            binding.getRoot().setOnClickListener(v -> userListener.onItemClick(user));
        }
    }
}
