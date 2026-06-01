package com.example.demo.entity;

public class Teacher {
    private Integer id;
    private String name;
    private String title;
    private String phone;

    public Teacher() {
    }

    public Teacher(Integer id, String name, String title, String phone) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.phone = phone;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
