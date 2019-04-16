package fr.testappli.googlemapapi.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Message2 {
    private String message;
    private String receiver;
    private String sender;
    private Date dateCreated;
    private boolean isseen;

    public Message2( String message, String receiver, String sender, boolean isseen) {
        this.message = message;
        this.receiver = receiver;
        this.sender = sender;
        this.isseen = isseen;
    }

    public Message2() {
    }

    public String getMessage() { return message ; }
    public String getReceiver() { return receiver ; }
    public String getSender() { return sender; }
    @ServerTimestamp
    public Date getDateCreated() { return dateCreated; }
    public boolean isIsseen() { return isseen; }

    public void setMessage(String message) { this.message = message ; }
    public void setReceiver(String receiver) { this.receiver = receiver ; }
    public void setSender(String sender) { this.sender = sender; }
    public void setDateCreated(Date dateCreated) { this.dateCreated = dateCreated; }
    public void setIsseen(boolean isseen) { this.isseen = isseen; }
}
