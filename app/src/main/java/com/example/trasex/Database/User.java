package com.example.trasex.Database;

public class User {
    String id;
    String name;
    String surname;
    String email;
    String city;
    boolean admin;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public User() {
    }

    public User(String id, String name, String surname, String login, String email, String city, String street, boolean admin) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.city = city;
        this.admin = admin;
    }
}
