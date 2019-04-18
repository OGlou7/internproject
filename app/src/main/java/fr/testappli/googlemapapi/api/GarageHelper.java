package fr.testappli.googlemapapi.api;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;

import fr.testappli.googlemapapi.models.Garage;

public class GarageHelper {

    private static final String COLLECTION_NAME = "garages";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getGaragesCollection(String user_uid){
        return UserHelper.getUsersCollection().document(user_uid).collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createGarageForUser(String user_uid, String garage_uid, String address, String description, double price, String rentalTime){
        Garage garageToCreate = new Garage(garage_uid, address, description, price, rentalTime, user_uid);
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

    // --- UPDATE ---

    public static Task<Void> updateisAvailable(String userID, String garageID, Boolean isAvailable) {
        return GarageHelper.getGaragesCollection(userID)
                .document(garageID)
                .update("isAvailable", isAvailable);
    }

    public static Task<Void> updateAddress(String userID, String garageID, String address) {
        return GarageHelper.getGaragesCollection(userID)
                .document(garageID)
                .update("address", address);
    }

    public static Task<Void> updateDescription(String userID, String garageID, String description) {
        return GarageHelper.getGaragesCollection(userID)
                .document(garageID)
                .update("description", description);
    }

    public static Task<Void> updatePrice(String userID, String garageID, double price) {
        return GarageHelper.getGaragesCollection(userID)
                .document(garageID)
                .update("price", price);
    }

    public static Task<Void> updateRentalTime(String userID, String garageID, String rentalTime) {
        return GarageHelper.getGaragesCollection(userID)
                .document(garageID)
                .update("rentalTime", rentalTime);
    }

    // --- DELETE ---

    public static Task<Void> deleteGarage(String userID, String garageID) {
        return GarageHelper.getGaragesCollection(userID)
                .document(garageID)
                .delete();
    }
}
