package com.example.play;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

        private ImageButton backButton;
        private Button logoutButton;
        private Switch switchSound, switchTheme; // Добавили switchTheme
        private FirebaseAuth mAuth;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_settings);

                mAuth = FirebaseAuth.getInstance();

                backButton = findViewById(R.id.buttonBack);
                logoutButton = findViewById(R.id.buttonLogout);
                switchSound = findViewById(R.id.switchSound);
                switchTheme = findViewById(R.id.switchTheme);

                SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);

                // --- Музыка ---
                boolean isMusicOn = prefs.getBoolean("isMusicOn", true);
                switchSound.setChecked(isMusicOn);

                switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        prefs.edit().putBoolean("isMusicOn", isChecked).apply();
                        if (isChecked) startMusicService();
                        else stopMusicService();
                });

                // --- Тема ---
                boolean isDarkMode = prefs.getBoolean("isDarkMode", false);
                switchTheme.setChecked(isDarkMode);

                // Применяем текущую тему
                if (isDarkMode) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }

                switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        prefs.edit().putBoolean("isDarkMode", isChecked).apply();

                        if (isChecked) {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        } else {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        }
                });

                // Назад
                backButton.setOnClickListener(v -> finish());

                // Выход из аккаунта
                logoutButton.setOnClickListener(v -> showLogoutConfirmation());
        }

        private void startMusicService() {
                Intent musicServiceIntent = new Intent(this, MusicService.class);
                startService(musicServiceIntent);
        }

        private void stopMusicService() {
                Intent stopMusicServiceIntent = new Intent(this, MusicService.class);
                stopService(stopMusicServiceIntent);
        }

        private void showLogoutConfirmation() {
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("Выход из аккаунта")
                        .setMessage("Вы уверены, что хотите выйти?")
                        .setCancelable(false)
                        .setPositiveButton("Да", (dialog, which) -> logoutClicked())
                        .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                        .show();
        }

        private void logoutClicked() {
                stopMusicService();
                mAuth.signOut();

                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
        }
}
