package fr.testappli.googlemapapi.models;

import android.support.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import fr.testappli.googlemapapi.week.WeekViewEvent;

import static fr.testappli.googlemapapi.week.WeekActivity.dateToCalendar;

public class Garage implements java.io.Serializable{
    private String uid;
    private String address;
    @Nullable
    private String description;
    private double price;
    private boolean isAvailable;

    public Garage() { }

    public void setGarage(Garage garage){
        this.uid = garage.getUid();
        this.address = garage.getAddress();
        this.description = garage.getDescription();
        this.price = garage.getPrice();
        this.isAvailable = garage.getisAvailable();
    }

    public Garage(String uid, String address, @Nullable String description, double price) {
        this.uid = uid;
        this.address = address;
        this.description = description;
        this.price = price;
        this.isAvailable = false;
    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getAddress() { return address; }
    public @Nullable String getDescription() { return description; }
    public double getPrice() { return price; }
    public boolean getisAvailable() { return isAvailable; }

    // --- SETTERS ---
    public void setUid(String uid) { this.uid = uid; }
    public void setAddress(String address) { this.address = address; }
    public void setDescription(@Nullable String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setReserved(boolean isAvailable) { this.isAvailable = isAvailable; }
}