package com.example.play;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private long backPressedTime;
    private Toast backToast;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // Настройка Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Кнопка "Начать игру"
        Button buttonStart = findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(view -> {
            try {
                Intent intent = new Intent(MainActivity.this, GameLevels.class);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Кнопка "Профиль"
        ImageView imageView8 = findViewById(R.id.imageView8);
        imageView8.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Кнопка "Настройки"
        ImageView imageView5 = findViewById(R.id.imageView5);
        imageView5.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Настройка полноэкранного режима
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Загрузка всех локальных пользователей из Room
        loadLocalUsers();
    }

    // Метод для вывода всех локальных пользователей в Logcat
    private void loadLocalUsers() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            List<User> users = db.userDao().getAllUsers(); // Предполагаем, что есть метод getAllUsers()
            for (User u : users) {
                Log.d("LocalUser", "Name: " + u.getName() + ", Email: " + u.getEmail());
            }
        }).start();
    }

    // Обработка кнопки "Назад" для двойного подтверждения выхода
    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            if (backToast != null) backToast.cancel();
            super.onBackPressed();
        } else {
            backToast = Toast.makeText(this, "Нажмите еще раз, чтобы выйти", Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }
}
