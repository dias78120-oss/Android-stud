package com.example.play;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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
        private Switch switchSound, switchTheme;
        private Button buttonTrack1, buttonTrack2; // Кнопки для треков
        private FirebaseAuth mAuth;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_settings);

                mAuth = FirebaseAuth.getInstance();

                backButton = findViewById(R.id.buttonBack);
                logoutButton = findViewById(R.id.buttonLogout);
                switchTheme = findViewById(R.id.switchTheme);
                switchSound = findViewById(R.id.switchSound);
                buttonTrack1 = findViewById(R.id.buttonTrack1);
                buttonTrack2 = findViewById(R.id.buttonTrack2);

                SharedPreferences settingsPrefs = getSharedPreferences("Settings", MODE_PRIVATE);
                SharedPreferences musicPrefs = getSharedPreferences("MusicPrefs", MODE_PRIVATE);

                // --- Музыка ---
                boolean isMusicOn = settingsPrefs.getBoolean("isMusicOn", true);
                switchSound.setChecked(isMusicOn);

                switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        settingsPrefs.edit().putBoolean("isMusicOn", isChecked).apply();
                        if (isChecked) startMusicService();
                        else stopMusicService();
                });

                // --- Выбор трека ---
                int selectedTrack = musicPrefs.getInt("selected_track", R.raw.background_music);

                buttonTrack1.setOnClickListener(v -> {
                        musicPrefs.edit().putInt("selected_track", R.raw.background_music).apply();
                        if (switchSound.isChecked()) playTrack(R.raw.background_music);
                });

                buttonTrack2.setOnClickListener(v -> {
                        musicPrefs.edit().putInt("selected_track", R.raw.background_music1).apply();
                        if (switchSound.isChecked()) playTrack(R.raw.background_music1);
                });

                // --- Тема ---
                boolean isDarkMode = settingsPrefs.getBoolean("isDarkMode", false);
                switchTheme.setChecked(isDarkMode);

                if (isDarkMode) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }

                switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        settingsPrefs.edit().putBoolean("isDarkMode", isChecked).apply();
                        if (isChecked) {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        } else {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        }
                });

                // --- Кнопка Назад ---
                backButton.setOnClickListener(v -> finish());

                // --- Выход из аккаунта ---
                logoutButton.setOnClickListener(v -> showLogoutConfirmation());
        }

        private void playTrack(int trackRes) {
                Intent intent = new Intent(this, MusicService.class);
                intent.putExtra("track", trackRes);
                startService(intent);
        }

        private void startMusicService() {
                SharedPreferences musicPrefs = getSharedPreferences("MusicPrefs", MODE_PRIVATE);
                int selectedTrack = musicPrefs.getInt("selected_track", R.raw.background_music);

                Intent musicServiceIntent = new Intent(this, MusicService.class);
                musicServiceIntent.putExtra("track", selectedTrack);
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
