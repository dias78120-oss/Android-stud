package com.example.play;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {

    // Вставка пользователя
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    // Обновление пользователя
    @Update
    void update(User user);

    // Удаление пользователя
    @Delete
    void deleteUser(User user);

    // Получение всех пользователей
    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    // Получение пользователя по email
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);
}
