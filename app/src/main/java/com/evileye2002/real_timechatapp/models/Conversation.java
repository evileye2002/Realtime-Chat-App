package com.evileye2002.real_timechatapp.models;

import java.io.Serializable;
import java.util.List;

public class Conversation implements Serializable {
    public String id, lastSenderID, lastSenderName, lastMessage, lastTimestamp;
    public boolean isGroup;

    public String name, image;
    public List<String> memberList;

    public List<Members> membersDetails;
}
