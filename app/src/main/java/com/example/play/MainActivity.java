package com.example.play;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private long backPressedTime;
    private Toast backToast;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences settingsPrefs = getSharedPreferences("Settings", MODE_PRIVATE);
        boolean isDarkMode = settingsPrefs.getBoolean("isDarkMode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        super.onCreate(savedInstanceState);
        // Устанавливаем полноэкранный режим до setContentView для плавной отрисовки
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // --- Инициализация элементов нового макета ---
        ImageView quizLogo = findViewById(R.id.quiz_logo);
        TextView gameTitle = findViewById(R.id.game_title);
        MaterialButton buttonStart = findViewById(R.id.buttonStart);
        ImageButton buttonSettings = findViewById(R.id.buttonSettings);
        ImageButton buttonProfile = findViewById(R.id.buttonProfile);

        // --- Установка обработчиков нажатий ---
        buttonStart.setOnClickListener(view -> {
            try {
                Intent intent = new Intent(MainActivity.this, GameLevels.class);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        buttonSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        buttonProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // --- Запускаем анимацию появления элементов ---
        // Создаем разные анимации для разных элементов для лучшего эффекта
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in); // Убедитесь, что этот файл тоже есть

        quizLogo.startAnimation(fadeIn);
        gameTitle.startAnimation(fadeIn);
        buttonStart.startAnimation(slideUp);

        // Загрузка локальных пользователей (без изменений)
        loadLocalUsers();
    }

    /**
     * Метод для вывода всех локальных пользователей в Logcat.
     * Работает в фоновом потоке, чтобы не блокировать UI.
     */
    private void loadLocalUsers() {
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                List<User> users = db.userDao().getAllUsers();
                for (User u : users) {
                    Log.d("LocalUser", "Name: " + u.getName() + ", Email: " + u.getEmail());
                }
            } catch (Exception e) {
                Log.e("DB_Error", "Failed to load local users", e);
            }
        }).start();
    }

    /**
     * Обработка кнопки "Назад" для выхода из приложения по двойному нажатию.
     */
    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            if (backToast != null) backToast.cancel();
            super.onBackPressed();
            return; // Выход из приложения
        } else {
            backToast = Toast.makeText(this, "Нажмите еще раз, чтобы выйти", Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }
}