package fr.testappli.googlemapapi.models;

public class Chat {

    private String sender;
    private String receiver;
    private String message;
    //private boolean isseen;

    public Chat(String sender, String receiver, String message) { this.sender = sender; this.receiver = receiver; this.message = message; }

    public Chat() {
    }

    public String getSender() { return sender; }
    public String getReceiver() { return receiver ; }
    public String getMessage() { return message ; }

    public void setSender(String sender) { this.sender = sender; }
    public void setReceiver(String receiver) { this.receiver = receiver ; }
    public void setMessage(String message) { this.message = message ; }
}