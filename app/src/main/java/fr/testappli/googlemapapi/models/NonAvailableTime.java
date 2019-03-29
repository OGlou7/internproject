package fr.testappli.googlemapapi.models;

import java.util.Date;

import fr.testappli.googlemapapi.week.WeekViewEvent;

public class NonAvailableTime implements java.io.Serializable{
    private Date mStartTime;
    private Date mEndTime;
    private String mName;
    private String mLocation;
    private int mColor;

    public NonAvailableTime(){
    }

    public NonAvailableTime(WeekViewEvent weekViewEvent){
        this.mName = weekViewEvent.getName();
        this.mStartTime = weekViewEvent.getStartTime().getTime();
        this.mEndTime = weekViewEvent.getEndTime().getTime();
        this.mLocation = weekViewEvent.getLocation();
        this.mColor = weekViewEvent.getColor();
    }

    public NonAvailableTime(String name, Date startTime, Date endTime, String location, int color) {
        this.mName = name;
        this.mStartTime = startTime;
        this.mEndTime = endTime;
        this.mLocation = location;
        this.mColor = color;
    }

    public Date getStartTime() {
        return mStartTime;
    }
    public Date getEndTime() {
        return mEndTime;
    }
    public String getName() {
        return mName;
    }
    public String getLocation() {
        return mLocation;
    }
    public int getColor(){ return mColor; }

    public void setStartTime(Date startTime) {
        this.mStartTime = startTime;
    }
    public void setEndTime(Date endTime) {
        this.mEndTime = endTime;
    }
    public void setName(String name) {
        this.mName = name;
    }
    public void setLocation(String location) {
        this.mLocation = location;
    }
    public void setColor(int color) {
        this.mColor = color;
    }
}
