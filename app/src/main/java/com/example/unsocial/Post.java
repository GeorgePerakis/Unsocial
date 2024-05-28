package com.example.unsocial;

public class Post {
    private String Description;
    private String Address;
    private String Name;
    private String Title;

    public Post(String description, String address, String name, String title) {
        Description = description;
        Address = address;
        Name = name;
        Title = title;
    }

    public Post()
    {

    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }
}
