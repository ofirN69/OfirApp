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
    String venue;

    String address;
    String city;

    String dresscode;

    String status;




   String menutype;

    String ownerId;

    ArrayList<User> users;

    public Event(String id, String type, String date,  String venue, String address, String city, String dresscode, String status, String menutype, String ownerId, ArrayList<User> users) {
        this.id = id;
        this.type = type;
        this.date = date;
        this.venue = venue;
        this.address = address;
        this.city = city;
        this.dresscode = dresscode;
        this.status = status;
        this.menutype = menutype;
        this.ownerId = ownerId;
        this.users = users;
    }

    public Event() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDresscode() {
        return dresscode;
    }

    public void setDresscode(String dresscode) {
        this.dresscode = dresscode;
    }

    public String getMenutype() {
        return menutype;
    }

    public void setMenutype(String menutype) {
        this.menutype = menutype;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", date='" + date + '\'' +
                ", venue='" + venue + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", dresscode='" + dresscode + '\'' +
                ", status='" + status + '\'' +
                ", menutype='" + menutype + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", users=" + users +
                '}';
    }
}

