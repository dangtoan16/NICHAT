package com.example.socialmedia.model;



public class Users {
    private String email, name, profileImageUrl, uid, status;
    boolean isAdmin,online;

    public Users() {
    }

    public Users(String email, String name, String profileImageUrl, String uid, String status, boolean isAdmin,boolean online) {
        this.email = email;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.uid = uid;
        this.status = status;
        this.isAdmin = isAdmin;
        this.online = online;
    }

    @Override
    public String toString() {
        return "Users{" +
                "email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", uid='" + uid + '\'' +
                ", status='" + status + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }


}
