package com.example.travelo.database;

public class User {
    String id;
    String name;
    String surname;
    String email;
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

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public User() {
    }

    public User(String id, String name, String surname, String login, String email, boolean admin) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.admin = admin;
    }
}
