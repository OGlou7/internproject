package fr.testappli.googlemapapi.api;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Objects;

import fr.testappli.googlemapapi.models.Garage;
import fr.testappli.googlemapapi.models.NonAvailableTime;

public class GarageHelper {

    private static final String COLLECTION_NAME = "garages";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getGaragesCollection(String user_uid){
        return UserHelper.getUsersCollection().document(user_uid).collection(COLLECTION_NAME);
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

    public static void updateListDateNonDispo(String userID, String garageID, ArrayList<NonAvailableTime> nonAvailableTimeList) {
        GarageHelper.getGaragesCollection(userID)
                .document(garageID).get().addOnSuccessListener(documentSnapshot -> {
                    Garage garage = documentSnapshot.toObject(Garage.class);
                    Objects.requireNonNull(garage).getListDateNonDispo().addAll(nonAvailableTimeList);
                    GarageHelper.getGaragesCollection(userID)
                            .document(garageID)
                            .set(garage);
                });
    }

    // --- DELETE ---

    public static Task<Void> deleteGarage(String userID, String garageID) {
        return GarageHelper.getGaragesCollection(userID)
                .document(garageID)
                .delete();
    }
}
