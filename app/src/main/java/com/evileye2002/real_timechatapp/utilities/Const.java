package com.evileye2002.real_timechatapp.utilities;

import com.google.firebase.firestore.FirebaseFirestore;

public class Const {
    public static final FirebaseFirestore database = FirebaseFirestore.getInstance();
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";

    //Collections
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_COLLECTION_CHATS = "chats";
    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";

    //Document User
    public static final String KEY_USER_ID = "id";
    public static final String KEY_USER_NAME = "name";
    public static final String KEY_USER_EMAIL = "email";
    public static final String KEY_USER_PASSWORD = "password";
    public static final String KEY_USER_IMAGE = "image";
    public static final String KEY_USER_FRIENDS = "friend";
    public static final String KEY_USER_TOKEN = "token";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";

    //Document Chats


    //Document Conversations
}
