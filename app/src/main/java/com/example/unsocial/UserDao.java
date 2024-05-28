package com.example.unsocial;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(User user);

    @Query("DELETE FROM Users")
    void deleteAllUsers();

    @Query("select * from Users ORDER BY username DESC LIMIT 1")
    public User getLastUser();
}
