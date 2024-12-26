package com.ofir.ofirapp.models;

import java.util.ArrayList;

public class Event {
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    String id;
    String type;
    String date;
    String hour;
    String venue;

    String address;

    String status;

    ArrayList<String> Menu;

    ArrayList<User> Users;
}
