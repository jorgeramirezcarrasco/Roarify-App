package com.polimi.jaj.roarify.model;

import java.io.Serializable;

/**
 * Created by Alberto on 30/12/16.
 */

public class Message implements Serializable{

    private static final long serialVersionUID = 1L;

    private String messageId;
    private Integer userId;
    private String userName;
    private String message;
    private Integer time; // Timestamp to be formatted as desired
    private Double latitude;
    private Double longitude;
    private String timeSent; // To be removed in the future
    private String distance; // To be removed in the future
    private String title; // To be removed in the future

    /* PROVISIONAL CONSTRUCTOR, SO THAT THE APP STILL WORKS
     * To be removed in the future */
    public Message(String userName, String message, String timeSent, String distance) {
        this.userName = userName;
        this.message = message;
        this.timeSent = timeSent;
        this.distance = distance;
    }

    /* CONSTRUCTOR TO BE USED */
    public Message(String messageId, Integer userId, String userName, String message, Integer time, Double latitude, Double longitude) {
        this.messageId = messageId;
        this.userId = userId;
        this.userName = userName;
        this.message = message;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
