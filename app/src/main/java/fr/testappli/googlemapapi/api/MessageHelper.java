package fr.testappli.googlemapapi.api;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import fr.testappli.googlemapapi.models.Message;
import fr.testappli.googlemapapi.models.Message2;
import fr.testappli.googlemapapi.models.User;

public class MessageHelper {
    private static final String COLLECTION_NAME = "messages";

    // --- GET ---

    public static Query getLastMessageForChat(String chat){
        return ChatHelper.getChatCollection()
                .document(chat)
                .collection(COLLECTION_NAME)
                .orderBy("dateCreated")
                .limit(50);
    }

    public static Task<QuerySnapshot> getAllMessageForChat(String chat){
        return ChatHelper.getChatCollection()
                .document(chat)
                .collection(COLLECTION_NAME)
                .orderBy("dateCreated")
                .get();
    }

    public static CollectionReference getMessageCollectionForChat(String chat){
        return ChatHelper.getChatCollection()
                .document(chat)
                .collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<DocumentReference> createMessageForChat(String textMessage, String chat, String userSender, String userReceiver){
        // Create the Message object
        Message2 message = new Message2(textMessage, userReceiver, userSender, false);

        // Store Message to Firestore
        return ChatHelper.getChatCollection()
                .document(chat)
                .collection(COLLECTION_NAME)
                .add(message);
    }

    public static Task<DocumentReference> createMessageWithImageForChat(String urlImage, String textMessage, String chat, User userSender){

        // Creating Message with the URL image
        Message message = new Message(textMessage, urlImage, userSender);

        // Storing Message on Firestore
        return ChatHelper.getChatCollection()
                .document(chat)
                .collection(COLLECTION_NAME)
                .add(message);
    }

    // --- UPDATE ---

    public static Task<Void> updateIsSeen(String chat, String messageID, boolean isseen) {
        return ChatHelper.getChatCollection()
                .document(chat)
                .collection(COLLECTION_NAME)
                .document(messageID)
                .update("isseen", isseen);
    }

}
