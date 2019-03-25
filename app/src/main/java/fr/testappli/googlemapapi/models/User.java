package fr.testappli.googlemapapi.models;

import android.support.annotation.Nullable;

public class User {

    private String uid;
    private String username;
    private Boolean isVendor;
    @Nullable
    private String urlPicture;

    public User() { }

    public User(String uid, String username, @Nullable String urlPicture) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
        this.isVendor = false;
    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getUsername() { return username; }
    public String getUrlPicture() { return urlPicture; }
    public Boolean getIsVendor() { return isVendor; }

    // --- SETTERS ---
    public void setUsername(String username) { this.username = username; }
    public void setUid(String uid) { this.uid = uid; }
    public void setUrlPicture(String urlPicture) { this.urlPicture = urlPicture; }
    public void setIsVendor(Boolean vendor) { isVendor = vendor; }
}

