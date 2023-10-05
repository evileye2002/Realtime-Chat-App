package com.evileye2002.real_timechatapp.models;

import java.io.Serializable;
import java.util.List;

public class Conversation implements Serializable {
    public String id, lastSenderID, lastSenderName, lastMessage, lastTimestamp;
    public boolean isGroup;
    //Group
    public String senderID, senderName, senderImage,receiverID, receiverName, receiverImage;

    //Group
    public String name, image, members;

    public List<Members> memberList;
}
