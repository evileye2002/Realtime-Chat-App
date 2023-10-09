package com.evileye2002.real_timechatapp.models;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {
    public String id, lastName, name, email, password, image, token;
    public List<String> friendList;

    public User() {

    }

}
