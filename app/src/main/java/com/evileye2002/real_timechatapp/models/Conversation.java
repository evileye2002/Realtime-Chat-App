package com.evileye2002.real_timechatapp.models;

import java.io.Serializable;
import java.util.List;

public class Conversation implements Serializable {
    public String id, lastSenderID, lastSenderName, lastMessage, lastTimestamp;
    public boolean isGroup;

    public String name, image;
    public List<String> memberList;

    public List<Members> membersDetails;

    public static class Members implements Serializable {
        public String id,name,image;

        public Members(String id, String name, String image) {
            this.id = id;
            this.name = name;
            this.image = image;
        }

        public Members() {
        }
    }
}
