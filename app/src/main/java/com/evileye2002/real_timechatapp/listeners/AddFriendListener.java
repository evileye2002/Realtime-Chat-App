package com.evileye2002.real_timechatapp.listeners;

import com.evileye2002.real_timechatapp.models.User;

public interface AddFriendListener {
    void onAddClick(User user);
    void onCancelClick(User user);
}
