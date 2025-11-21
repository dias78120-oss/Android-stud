package com.example.play;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users") // Это делает класс таблицей Room
public class User {
    @PrimaryKey(autoGenerate = true)
    private int id; // обязательно для Room

    private String name;
    private String email;

    // Пустой конструктор (Firebase требует)
    public User() {}

    // Конструктор с данными (для Room и Firebase)
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Геттер и сеттер для id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Геттер и сеттер для name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Геттер и сеттер для email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
