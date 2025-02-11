package fr.testappli.googlemapapi.models;

import android.support.annotation.Nullable;

public class Garage implements java.io.Serializable{
    private String uid;
    private String address;
    @Nullable private String description;
    private double price;
    private boolean isAvailable;
    private String rentalTime;
    private String ownerID;


    public Garage() { }

    public void setGarage(Garage garage){
        this.uid = garage.getUid();
        this.address = garage.getAddress();
        this.description = garage.getDescription();
        this.price = garage.getPrice();
        this.isAvailable = garage.getisAvailable();
        this.rentalTime = garage.getRentalTime();
        this.ownerID = garage.getOwnerID();
    }

    public Garage(String uid, String address, @Nullable String description, double price, String rentalTime, String ownerID) {
        this.uid = uid;
        this.address = address;
        this.description = description;
        this.price = price;
        this.isAvailable = false;
        this.rentalTime = rentalTime;
        this.ownerID = ownerID;
    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getAddress() { return address; }
    public @Nullable String getDescription() { return description; }
    public double getPrice() { return price; }
    public boolean getisAvailable() { return isAvailable; }
    public String getRentalTime() { return rentalTime; }
    public String getOwnerID() { return ownerID; }


    // --- SETTERS ---
    public void setUid(String uid) { this.uid = uid; }
    public void setAddress(String address) { this.address = address; }
    public void setDescription(@Nullable String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setReserved(boolean isAvailable) { this.isAvailable = isAvailable; }
    public void setRentalTime(String rentalTime) { this.rentalTime = rentalTime; }
    public void setOwnerID(String ownerID) { this.ownerID = ownerID; }
}