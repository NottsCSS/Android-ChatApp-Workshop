package com.app.chatapp.models;


public class Chat {
    private String uid;
    private String message;
    private long timestampUTC;

    public Chat() {
    }

    public Chat(String uid, String message, long timestampUTC) {
        this.uid = uid;
        this.message = message;
        this.timestampUTC = timestampUTC;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestampUTC() {
        return timestampUTC;
    }

    public void setTimestampUTC(long timestampUTC) {
        this.timestampUTC = timestampUTC;
    }
}
