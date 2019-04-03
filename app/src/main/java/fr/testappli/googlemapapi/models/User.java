package fr.testappli.googlemapapi.models;

import android.support.annotation.Nullable;

import java.util.ArrayList;

public class User {

    private String uid;
    private String username;
    private Boolean isVendor;
    @Nullable
    private String urlPicture;
    private String immatriculation;

    public User() { }

    public User(String uid, String username, @Nullable String urlPicture) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
        this.isVendor = false;
        this.immatriculation = "";
    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getUsername() { return username; }
    public @Nullable String getUrlPicture() { return urlPicture; }
    public Boolean getIsVendor() { return isVendor; }
    public String getImmatriculation() { return immatriculation; }

    // --- SETTERS ---
    public void setUsername(String username) { this.username = username; }
    public void setUid(String uid) { this.uid = uid; }
    public void setUrlPicture(@Nullable String urlPicture) { this.urlPicture = urlPicture; }
    public void setIsVendor(Boolean vendor) { isVendor = vendor; }
    public void setImmatriculation(String immatriculation) { this.immatriculation = immatriculation; }
}

