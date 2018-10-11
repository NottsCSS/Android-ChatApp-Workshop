package com.app.chatapp.models;

public class Contact {
    private String phoneNo;
    private String uid;
    private String chatID;

    public Contact() {
    }

    public Contact(String phoneNo, String chatID) {
        this.phoneNo = phoneNo;
        this.chatID = chatID;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }
}