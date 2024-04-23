package com.example.socialmedia;

import com.example.socialmedia.model.Users;
import com.firebase.ui.auth.data.model.User;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class UserArray {

    private ArrayList<Users> userArray;

    public UserArray() {
        userArray = new ArrayList<>();
    }
    public ArrayList<Users> getUserArray(){
        return userArray;
    }
    // Method to add a user to the array if not already present
    public synchronized void add(Users user) {
        if (!contains(user)) {
            userArray.add(user);
        }
    }

    // Method to get the size of the array
    public int size() {
        return userArray.size();
    }

    // Method to check if the array contains a specific user
    public boolean contains(Users user) {
        for (Users u : userArray) {
            if (u.getUid().equals(user.getUid())) {
                return true;
            }
        }
        return false;
    }
    public boolean contains(FirebaseUser user) {
        for (Users u : userArray) {
            if (u.getUid().equals(user.getUid())) {
                return true;
            }
        }
        return false;
    }
    // Method to clear the array
    public void clear() {
        userArray.clear();
    }
}