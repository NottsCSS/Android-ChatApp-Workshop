package com.app.chatapp.models;

public class User {

    private String phoneNo;

    public User() {
    }

    public User(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }
}
