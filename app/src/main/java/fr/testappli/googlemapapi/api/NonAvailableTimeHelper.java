package fr.testappli.googlemapapi.api;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;

import fr.testappli.googlemapapi.models.NonAvailableTime;
import fr.testappli.googlemapapi.week.WeekViewEvent;

public class NonAvailableTimeHelper {
    private static final String COLLECTION_NAME = "nonAvailableTime";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getNonAvailableTimeCollection(String user_uid, String garage_uid){
        return GarageHelper.getGaragesCollection(user_uid).document(garage_uid).collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createNonAvailableTimeForGarage(String user_uid, String garage_uid, String nonAvailableTime_uid, WeekViewEvent weekViewEvent){
        NonAvailableTime nonAvailableTimeToCreate = new NonAvailableTime(weekViewEvent);
        return NonAvailableTimeHelper.getNonAvailableTimeCollection(user_uid, garage_uid)
                .document(nonAvailableTime_uid)
                .set(nonAvailableTimeToCreate);
    }

    // --- UPDATE ---
    public static Task<Void> updateNonAvailableTime(String user_uid, String garage_uid, String nonAvailableTime_uid, WeekViewEvent weekViewEvent) {
        NonAvailableTime nonAvailableTimeToCreate = new NonAvailableTime(weekViewEvent);
        return NonAvailableTimeHelper.getNonAvailableTimeCollection(user_uid, garage_uid)
                .document(nonAvailableTime_uid)
                .set(nonAvailableTimeToCreate);
    }

    public static Task<Void> updateStartTime(String user_uid, String garage_uid, String nonAvailableTime_uid, NonAvailableTime startTime) {
        return NonAvailableTimeHelper.getNonAvailableTimeCollection(user_uid, garage_uid)
                .document(nonAvailableTime_uid)
                .update("startTime", startTime);
    }

    public static Task<Void> updateEndTime(String user_uid, String garage_uid, String nonAvailableTime_uid, NonAvailableTime endTime) {
        return NonAvailableTimeHelper.getNonAvailableTimeCollection(user_uid, garage_uid)
                .document(nonAvailableTime_uid)
                .update("endTime", endTime);
    }

    public static Task<Void> updateName(String user_uid, String garage_uid, String nonAvailableTime_uid, String name) {
        return NonAvailableTimeHelper.getNonAvailableTimeCollection(user_uid, garage_uid)
                .document(nonAvailableTime_uid)
                .update("name", name);
    }

    // --- DELETE ---

    public static Task<Void> deleteDateNonDispo(String user_uid, String garage_uid, String nonAvailableTime_uid) {
        return NonAvailableTimeHelper.getNonAvailableTimeCollection(user_uid, garage_uid)
                .document(nonAvailableTime_uid)
                .delete();
    }
}
