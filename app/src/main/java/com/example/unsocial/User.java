package com.example.unsocial;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Users")
public class User {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "username")
    private String Username;
    @ColumnInfo(name = "password")
    private String Password;
    @ColumnInfo(name = "address")
    private String Address;
    private String url;

    public User(String username, String password) {
        Username = username;
        Password = password;
    }

    public User(String username, String password,String address) {
        Username = username;
        Password = password;
        Address = address;
    }

    public User(){

    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    @NonNull
    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "User{" +
                "Username='" + Username + '\'' +
                ", Password='" + Password + '\'' +
                ", Address='" + Address + '\'' +
                '}';
    }
}
