package com.polimi.jaj.roarify.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Message implements Serializable{

    private static final long serialVersionUID = 1L;

    private String messageId;
    private String userId;
    private String userName;
    private String text;
    private String time;
    private Double latitude;
    private Double longitude;


    /* CONSTRUCTOR */
    public Message(String messageId, String userId, String userName, String text, String time, Double latitude, Double longitude) {
        this.messageId = messageId;
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
    }



    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
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