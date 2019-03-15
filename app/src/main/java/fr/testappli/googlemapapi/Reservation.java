package fr.testappli.googlemapapi;

import java.util.Date;

public class Reservation {
    private String address;
    private Date startDate;
    private Date endDate;
    private String description;

    Reservation(){

    }

    Reservation(String address, Date startDate, Date endDate, String description){
        setAddress(address);
        setStartDate(startDate);
        setEndDate(endDate);
        setDescription(description);
    }

    public String getAddress(){
        return this.address;
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

    private void setAddress(String newAddress){
        this.address = newAddress;
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
}
