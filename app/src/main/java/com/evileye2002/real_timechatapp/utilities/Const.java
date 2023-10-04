package com.evileye2002.real_timechatapp.utilities;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class Const {
    public static final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    public static final FirebaseMessaging firestoreMessaging = FirebaseMessaging.getInstance();
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final DocumentReference currentUser(String id){
        return firestore.collection(Const.KEY_COLLECTION_USERS).document(id);
    }

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
    public static final String KEY_CONVERSATION_NAME = "name";
    public static final String KEY_CONVERSATION_MEMBERS = "members";
    public static final String KEY_CONVERSATION_IMAGE = "image";
    public static final String KEY_CONVERSATION_LAST_SENDER_ID = "lastSenderID";
    public static final String KEY_CONVERSATION_LAST_MESSAGE = "lastMessage";
    public static final String KEY_CONVERSATION_LAST_TIMESTAMP = "lastTimestamp";

}
