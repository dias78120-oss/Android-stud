package com.example.play;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import android.widget.ImageButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

        private ImageButton backButton;
        private MaterialButton logoutButton;
        private SwitchMaterial switchSound, switchTheme;
        private MaterialButton buttonTrack1, buttonTrack2, buttonTrack3, buttonTrack4;
        private List<MaterialButton> trackButtons;
        private FirebaseAuth mAuth;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                // Применение темы
                SharedPreferences settingsPrefs = getSharedPreferences("Settings", MODE_PRIVATE);
                boolean isDarkMode = settingsPrefs.getBoolean("isDarkMode", false);
                if (isDarkMode) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }

                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_settings);

                // Инициализация всех View
                mAuth = FirebaseAuth.getInstance();
                backButton = findViewById(R.id.buttonBack);
                logoutButton = findViewById(R.id.buttonLogout);
                switchTheme = findViewById(R.id.switchTheme);
                switchSound = findViewById(R.id.switchSound);

                buttonTrack1 = findViewById(R.id.buttonTrack1);
                buttonTrack2 = findViewById(R.id.buttonTrack2);
                buttonTrack3 = findViewById(R.id.buttonTrack3);
                buttonTrack4 = findViewById(R.id.buttonTrack4);

                trackButtons = new ArrayList<>();
                trackButtons.add(buttonTrack1);
                trackButtons.add(buttonTrack2);
                trackButtons.add(buttonTrack3);
                trackButtons.add(buttonTrack4);

                SharedPreferences musicPrefs = getSharedPreferences("MusicPrefs", MODE_PRIVATE);

                // --- Настройка музыки ---
                boolean isMusicOn = settingsPrefs.getBoolean("isMusicOn", true);
                switchSound.setChecked(isMusicOn);
                switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        settingsPrefs.edit().putBoolean("isMusicOn", isChecked).apply();
                        if (isChecked) {
                                startMusicService();
                        } else {
                                stopMusicService();
                        }
                });

                // --- Настройка выбора трека (ИСПРАВЛЕННАЯ ЛОГИКА) ---
                View.OnClickListener trackClickListener = v -> {
                        int trackResId = R.raw.background_music;
                        int selectedButtonId = v.getId();

                        if (selectedButtonId == R.id.buttonTrack1) {
                                trackResId = R.raw.background_music;
                        } else if (selectedButtonId == R.id.buttonTrack2) {
                                trackResId = R.raw.background_music1;
                        } else if (selectedButtonId == R.id.buttonTrack3) {
                                trackResId = R.raw.background_music2;
                        } else if (selectedButtonId == R.id.buttonTrack4) {
                                trackResId = R.raw.background_music3;
                        }

                        musicPrefs.edit().putInt("selected_track", trackResId).apply();
                        if (switchSound.isChecked()) {
                                playTrack(trackResId);
                        }
                        updateTrackButtons(selectedButtonId);
                };

                buttonTrack1.setOnClickListener(trackClickListener);
                buttonTrack2.setOnClickListener(trackClickListener);
                buttonTrack3.setOnClickListener(trackClickListener);
                buttonTrack4.setOnClickListener(trackClickListener);

                // Устанавливаем начальное состояние кнопок при загрузке
                int selectedTrackResId = musicPrefs.getInt("selected_track", R.raw.background_music);
                int selectedButtonId = R.id.buttonTrack1; // Кнопка по умолчанию
                if (selectedTrackResId == R.raw.background_music) {
                        selectedButtonId = R.id.buttonTrack1;
                } else if (selectedTrackResId == R.raw.background_music1) {
                        selectedButtonId = R.id.buttonTrack2;
                } else if (selectedTrackResId == R.raw.background_music2) {
                        selectedButtonId = R.id.buttonTrack3;
                } else if (selectedTrackResId == R.raw.background_music3) {
                        selectedButtonId = R.id.buttonTrack4;
                }
                updateTrackButtons(selectedButtonId);


                // --- Настройка темы ---
                switchTheme.setChecked(isDarkMode);
                switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        settingsPrefs.edit().putBoolean("isDarkMode", isChecked).apply();
                        recreate();
                });

                // --- Кнопки Назад и Выход ---
                backButton.setOnClickListener(v -> finish());
                logoutButton.setOnClickListener(v -> showLogoutConfirmation());
        }

        // Метод для обновления вида кнопок
        private void updateTrackButtons(int selectedId) {
                for (MaterialButton button : trackButtons) {
                        if (button.getId() == selectedId) {
                                // Стиль для ВЫБРАННОЙ кнопки (заливка)
                                button.setBackgroundColor(Color.WHITE);
                                button.setTextColor(Color.BLACK);
                                button.setStrokeWidth(0); // Убираем рамку у выбранной
                        } else {
                                // Стиль для НЕВЫБРАННЫХ кнопок (прозрачный фон, белая рамка)
                                button.setBackgroundColor(Color.TRANSPARENT);
                                button.setTextColor(Color.WHITE);
                                // Возвращаем рамку. Конвертируем dp в пиксели для установки толщины.
                                int strokeWidthPx = (int) (1 * getResources().getDisplayMetrics().density);
                                button.setStrokeWidth(strokeWidthPx);
                                button.setStrokeColor(ContextCompat.getColorStateList(this, R.color.white));
                        }
                }
        }

        // ... (остальные ваши методы остаются без изменений)
        private void playTrack(int trackRes) {
                stopMusicService(); // Останавливаем старую музыку перед запуском новой
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