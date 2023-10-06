package com.evileye2002.real_timechatapp.utilities;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class Const {
    public static final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    public static final FirebaseMessaging firestoreMessaging = FirebaseMessaging.getInstance();
    public static final String PREFERENCE_NAME = "chatAppPreference";
    public static final String dateFormat = "dd/MM/yyyy-HH:mm:ss";

    //Collections Key
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_CHATS = "chats";
    public static final String COLLECTION_CON = "conversations";
    public static final String COLLECTION_MEMBERS = "memberList";

    //Collection Reference
    public static final CollectionReference user_collection = firestore.collection(COLLECTION_USERS);
    public static final CollectionReference con_collection = firestore.collection(COLLECTION_CON);
    public static final CollectionReference chat_collection(String id){
        return conDoc(id).collection(COLLECTION_CHATS);
    }

    public static final CollectionReference member_collection(String conID, String chatID){
        return chatDoc(conID,chatID).collection(COLLECTION_MEMBERS);
    }

    //Document Reference
    public static final DocumentReference userDoc(String id){
        return user_collection.document(id);
    }
    public static final DocumentReference conDoc(String id){
        return con_collection.document(id);
    }

    public static final DocumentReference chatDoc(String conID, String chatID){
        return chat_collection(conID).document(chatID);
    }

    //Document
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String IMAGE = "image";
    public static final String SENDER_ID = "senderID";


    //Document User
    public static final String USER = "user";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String FRIENDS = "friends";
    public static final String TOKEN = "token";
    public static final String IS_SIGNED_IN = "isSignedIn";

    //Document Chats
    public static final String CHAT = "chat";
    public static final String MESSAGE = "message";
    public static final String TIMESTAMP = "timestamp";
    public static final String PENDING_ID = "pendingID";

    //Document Conversations
    public static final String CONVERSATION = "conversation";
    public static final String IS_GROUP = "isGroup";
    public static final String LAST_SENDER_ID = "lastSenderID";
    public static final String LAST_SENDER_NAME = "lastSenderName";
    public static final String LAST_MESSAGE = "lastMessage";
    public static final String LAST_TIMESTAMP = "lastTimestamp";

    public static final String SENDER_NAME = "senderName";
    public static final String SENDER_IMAGE = "senderImage";
    public static final String RECEIVER_ID = "receiverID";
    public static final String RECEIVER_NAME = "receiverName";
    public static final String RECEIVER_IMAGE = "receiverImage";

}
