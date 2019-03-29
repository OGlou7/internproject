package fr.testappli.googlemapapi.models;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;

public class Garage {
    private String uid;
    private String address;
    @Nullable
    private String description;
    private double price;
    private boolean isReserved;
    private ArrayList<Date> listDateNonDispo;

    public Garage() { }

    public Garage(String uid, String address, @Nullable String description, double price) {
        this.uid = uid;
        this.address = address;
        this.description = description;
        this.price = price;
        this.isReserved = false;
        this.listDateNonDispo = new ArrayList<>();
    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getAddress() { return address; }
    public @Nullable String getDescription() { return description; }
    public double getPrice() { return price; }
    public boolean getIsReserved() { return isReserved; }
    public ArrayList<Date> getListDateNonDispo() { return listDateNonDispo; }

    // --- SETTERS ---
    public void setUid(String uid) { this.uid = uid; }
    public void setAddress(String address) { this.address = address; }
    public void setDescription(@Nullable String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setReserved(boolean isReserved) { this.isReserved = isReserved; }
    public void setListDateNonDispo(ArrayList<Date> listDateNonDispo) { this.listDateNonDispo = listDateNonDispo; }
}