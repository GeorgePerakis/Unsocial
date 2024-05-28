package com.example.unsocial;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.unsocial.User;
import com.example.unsocial.UserDao;

@Database(entities = {User.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
}