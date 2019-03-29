package fr.testappli.googlemapapi.models;

import java.util.Date;

import fr.testappli.googlemapapi.week.WeekViewEvent;

public class NonAvailableTime {
    private String uid;
    private Date mStartTime;
    private Date mEndTime;
    private String mName;

    public NonAvailableTime(){
    }

    public NonAvailableTime(WeekViewEvent weekViewEvent){
        this.mName = weekViewEvent.getName();
        this.mStartTime = weekViewEvent.getStartTime().getTime();
        this.mEndTime = weekViewEvent.getEndTime().getTime();
    }

    public NonAvailableTime(String name, Date startTime, Date endTime) {
        this.mName = name;
        this.mStartTime = startTime;
        this.mEndTime = endTime;
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

    public void setStartTime(Date startTime) {
        this.mStartTime = startTime;
    }
    public void setEndTime(Date endTime) {
        this.mEndTime = endTime;
    }
    public void setName(String name) {
        this.mName = name;
    }
}
