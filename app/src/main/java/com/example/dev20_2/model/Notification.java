package com.example.dev20_2.model;

public class Notification {
    private String email;
    private String lat;
    private String lng;
    private int type;
    private int upvote;
    private int downvote;
    private String description;

    public Notification() {
    }

    public Notification(String email, String lat, String lng, int type, int upvote, int downvote) {
        this.email = email;
        this.lat = lat;
        this.lng = lng;
        this.type = type;
        this.upvote = upvote;
        this.downvote = downvote;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getUpvote() {
        return upvote;
    }

    public void setUpvote(int upvote) {
        this.upvote = upvote;
    }

    public int getDownvote() {
        return downvote;
    }

    public void setDownvote(int downvote) {
        this.downvote = downvote;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
