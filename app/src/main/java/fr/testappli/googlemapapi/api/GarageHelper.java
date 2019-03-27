package fr.testappli.googlemapapi.api;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;

import fr.testappli.googlemapapi.models.Garage;

public class GarageHelper {

    private static final String COLLECTION_NAME = "garages";

    // --- COLLECTION REFERENCE ---

    private static CollectionReference getGaragesCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createGarage(String uid, String address, String description, double price) {
        Garage garageToCreate = new Garage(uid, address, description, price);
        return GarageHelper.getGaragesCollection().document(uid).set(garageToCreate);
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getGarage(String uid){
        return GarageHelper.getGaragesCollection().document(uid).get();
    }

    // --- UPDATE ---

    public static Task<Void> updateIsReserved(String uid, Boolean isReserved) {
        return GarageHelper.getGaragesCollection().document(uid).update("isReserved", isReserved);
    }

    public static Task<Void> updateListDateNonDispo(String uid, ArrayList<Date> listDateNonDispo) {
        return GarageHelper.getGaragesCollection().document(uid).update("listDateNonDispo", listDateNonDispo);
    }

    // --- DELETE ---

    public static Task<Void> deleteGarage(String uid) {
        return GarageHelper.getGaragesCollection().document(uid).delete();
    }
}
