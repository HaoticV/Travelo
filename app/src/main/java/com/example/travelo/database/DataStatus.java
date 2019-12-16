package com.example.travelo.database;

import java.util.List;

public interface DataStatus {
    void UserIsLoaded(User user, String key);

    void DataIsLoaded(List<User> users, List<String> keys);

    void DataIsInserted();

    void DataIsUpdated();

    void DataIsDeleted();
}