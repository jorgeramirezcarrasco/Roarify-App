package com.polimi.jaj.roarify.model;

/**
 * Created by Alberto on 30/12/16.
 */

public class DisplayedMessage {
    private String author;
    private String message;
    private String timeSent;
    private String distance;

    public DisplayedMessage(String author, String message, String timeSent, String distance) {
        this.author = author;
        this.message = message;
        this.timeSent = timeSent;
        this.distance = distance;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(String timeSent) {
        this.timeSent = timeSent;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
