package com.example.play;

public class User {
    private String name;
    private String email;


    // Пустой конструктор (необходим для Firebase)
    public User() {
    }

    // Конструктор для создания объекта с данными
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Геттер для имени
    public String getName() {
        return name;
    }

    // Сеттер для имени
    public void setName(String name) {
        this.name = name;
    }

    // Геттер для email
    public String getEmail() {
        return email;
    }

    // Сеттер для email
    public void setEmail(String email) {
        this.email = email;
    }
}

