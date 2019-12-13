package com.example.travelo.Database;

import java.util.List;

public interface DataStatus {
    void UserIsLoaded(User user, String key);

    void DataIsLoaded(List<User> users, List<String> keys);

    void DataIsInserted();

    void DataIsUpdated();

    void DataIsDeleted();
}
