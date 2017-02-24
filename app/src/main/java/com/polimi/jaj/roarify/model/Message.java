package com.polimi.jaj.roarify.model;

import java.io.Serializable;

public class Message implements Serializable{

    private static final long serialVersionUID = 1L;

    private String messageId;
    private String userId;
    private String userName;
    private String text;
    private String time;
    private Double lat;
    private Double lon;
    private String isParent;
    private String parentId;
    private String distance;


    public Message(){}

    /* CONSTRUCTOR */
    public Message(String messageId, String userId, String userName, String text, String time, Double lat, Double lon, String isParent, String parentId, String distance) {
        this.messageId = messageId;
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.time = time;
        this.lat = lat;
        this.lon = lon;
        this.isParent = isParent;
        this.parentId = parentId;
        this.distance = distance;
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
        return lat;
    }

    public void setLatitude(Double latitude) {
        this.lat = latitude;
    }

    public Double getLongitude() {
        return lon;
    }

    public void setLongitude(Double lon) {
        this.lon = lon;
    }

    public void setParentId (String parentId){this.parentId = parentId;}

    public void setIsParent (String isParent){this.isParent=isParent;}

    public  String getIsParent (){return isParent;}

    public String getParentId(){return parentId;}

    public void setDistance (String distance){this.distance = distance;}

    public String getDistance(){return distance;}


}