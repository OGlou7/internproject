package fr.testappli.googlemapapi;

import java.util.Date;

public class Reservation {
    private String completeAddress;
    private Date startDate;
    private Date endDate;
    private String description;
    private double price;

    Reservation(){

    }

    Reservation(String address, Date startDate, Date endDate, String description, double price){
        setCompleteAddress(address);
        setStartDate(startDate);
        setEndDate(endDate);
        setDescription(description);
        setPrice(price);
    }

    public String getCompleteAddress(){
        return this.completeAddress;
    }
    public Date getStartDate(){
        return this.startDate;
    }
    public Date getEndDate(){
        return this.endDate;
    }
    public String getDescription(){
        return this.description;
    }
    public double getPrice(){
        return this.price;
    }
    public String getAddress(){ return this.completeAddress.split(", ")[0]; }
    public String getCity(){ return this.completeAddress.split(", ")[1]; }
    public String getCountry(){ return this.completeAddress.split(", ")[2]; }

    private void setCompleteAddress(String newAddress){
        this.completeAddress = newAddress;
    }
    private void setStartDate(Date newStartDate){
        this.startDate = newStartDate;
    }
    private void setEndDate(Date newEndDate){
        this.endDate = newEndDate;
    }
    private void setDescription(String newDescription){
        this.description = newDescription;
    }
    private void setPrice(double newPrice){
        this.price = newPrice;
    }
}
