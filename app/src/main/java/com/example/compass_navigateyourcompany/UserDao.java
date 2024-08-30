package com.example.compass_navigateyourcompany;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Query("SELECT * FROM User WHERE login_name = :loginName AND password = :password AND auth_token = :authToken")
    User getUser(String loginName, String password, String authToken);

    @Query("SELECT * FROM User WHERE id = :id")
    User findUserById(int id);

    @Query("SELECT * FROM User WHERE auth_token = :authToken")
    User findByAuthToken(String authToken);

}