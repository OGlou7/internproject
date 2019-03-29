package fr.testappli.googlemapapi.api;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;

import fr.testappli.googlemapapi.models.Garage;

public class NonAvailableTimeHelper {
    private static final String COLLECTION_NAME = "nonAvailableTime";
    // --- COLLECTION REFERENCE ---

    public static CollectionReference getNonAvailableTimeCollection(String user_uid, String garage_uid){
        return GarageHelper.getGaragesCollection(user_uid).document(garage_uid).collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createGarageForUser(String user_uid, String garage_uid, String address, String description, double price){
        Garage garageToCreate = new Garage(garage_uid, address, description, price);
        return GarageHelper.getGaragesCollection(user_uid)
                .document(garage_uid)
                .set(garageToCreate);
    }

    // --- GET ---

    public static Query getAllGarageForUser(String userID){
        return GarageHelper.getGaragesCollection(userID)
                .orderBy("address")
                .limit(50);
    }
    public static Task<DocumentSnapshot> getGarageForUser(String userID, String garageID){
        return GarageHelper.getGaragesCollection(userID)
                .document(garageID)
                .get();
    }

    // --- UPDATE ---

    public static Task<Void> updateIsReserved(String userID, String garageID, Boolean isReserved) {
        return GarageHelper.getGaragesCollection(userID)
                .document(garageID)
                .update("isReserved", isReserved);
    }

    public static Task<Void> updateListDateNonDispo(String userID, String garageID, ArrayList<Date> listDateNonDispo) {
        return GarageHelper.getGaragesCollection(userID)
                .document(garageID)
                .update("listDateNonDispo", listDateNonDispo);
    }

    // --- DELETE ---

    public static Task<Void> deleteGarage(String userID, String garageID) {
        return GarageHelper.getGaragesCollection(userID)
                .document(garageID)
                .delete();
    }
}
