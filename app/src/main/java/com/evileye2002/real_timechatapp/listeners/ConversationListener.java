package com.evileye2002.real_timechatapp.listeners;

import com.evileye2002.real_timechatapp.models.Conversation;
import com.evileye2002.real_timechatapp.models.User;

public interface ConversationListener {
    void onItemClick(Conversation conversation, User user);
}
