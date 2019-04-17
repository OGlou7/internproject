package fr.testappli.googlemapapi.api;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ChatHelper {
    private static final String COLLECTION_NAME = "chats";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getChatCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createChat(String chat) {
        Map<String, Object> test = new HashMap<>();
        test.put("id", chat);
        return ChatHelper.getChatCollection().document(chat).set(test);
    }
}
