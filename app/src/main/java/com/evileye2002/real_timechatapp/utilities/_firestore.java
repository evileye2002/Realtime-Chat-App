package com.evileye2002.real_timechatapp.utilities;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class _firestore {
     final static FirebaseFirestore firestore = FirebaseFirestore.getInstance();
     public final static FirebaseMessaging firestoreMessaging = FirebaseMessaging.getInstance();
    public static CollectionReference allUsers(){
        return firestore.collection(_const.COLLECTION_USERS);
    }

    public static DocumentReference singleUser(String ID){
        return allUsers().document(ID);
    }

    public static CollectionReference allCons(){
        return firestore.collection(_const.COLLECTION_CON);
    }

    public static DocumentReference singleCon(String ID){
        return allCons().document(ID);
    }

    public static CollectionReference allChats(String ID){
        return singleCon(ID).collection(_const.COLLECTION_CHATS);
    }

    public static DocumentReference singleChat(String ConID, String ID){
        return allChats(ConID).document(ID);
    }
}
