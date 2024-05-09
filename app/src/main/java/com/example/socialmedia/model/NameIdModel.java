package com.example.socialmedia.model;


import androidx.annotation.NonNull;

public class NameIdModel {
    public String name,id;

    public NameIdModel(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public NameIdModel() {
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}

