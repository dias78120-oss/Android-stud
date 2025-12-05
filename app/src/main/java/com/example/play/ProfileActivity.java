package com.example.play;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// Импорты для работы с файлами
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    // ... все ваши переменные остаются без изменений ...
    private EditText nameEditText;
    private TextView emailTextView;
    private MaterialButton saveButton;
    private MaterialButton deleteButton;
    private TextView coinsTextView;
    private TextView timeSpentTextView;
    private ImageButton backButton;
    private ImageView avatarImageView;
    private ImageView editAvatarIcon;

    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    private boolean isEditMode = false;
    private Uri newAvatarUri = null;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    newAvatarUri = result.getData().getData();
                    Glide.with(this).load(newAvatarUri).circleCrop().into(avatarImageView);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Инициализация
        backButton = findViewById(R.id.buttonBack);
        emailTextView = findViewById(R.id.emailTextView);
        nameEditText = findViewById(R.id.nameEditText);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        coinsTextView = findViewById(R.id.coinsTextView);
        timeSpentTextView = findViewById(R.id.timeSpentTextView);
        avatarImageView = findViewById(R.id.avatarImageView);
        editAvatarIcon = findViewById(R.id.editAvatarIcon);

        // Firebase (остается для имени, монет и т.д.)
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish(); // Если пользователя нет, закрываем профиль
            return;
        }
        String uid = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        // Начальная настройка UI
        setEditMode(false);
        loadUserProfile(); // Загружает имя, монеты и т.д. из Firebase
        loadAvatarFromLocalFile(); // Загружает аватар из файла на телефоне

        // Слушатели
        backButton.setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> handleSaveButtonClick());
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
        editAvatarIcon.setOnClickListener(v -> openImagePicker());
    }

    private void handleSaveButtonClick() {
        if (isEditMode) {
            saveUserProfile();
        } else {
            setEditMode(true);
        }
    }

    // --- ИЗМЕНЕННЫЙ МЕТОД СОХРАНЕНИЯ ---
    private void saveUserProfile() {
        // 1. Сохраняем имя в Firebase
        String newName = nameEditText.getText().toString().trim();
        userRef.child("name").setValue(newName);

        // 2. Если выбран новый аватар, сохраняем его в локальный файл
        if (newAvatarUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), newAvatarUri);
                saveAvatarToLocalFile(bitmap); // Новый метод сохранения
                newAvatarUri = null;
            } catch (IOException e) {
                Toast.makeText(this, "Ошибка сохранения аватара", Toast.LENGTH_SHORT).show();
            }
        }

        Toast.makeText(this, "Профиль сохранен!", Toast.LENGTH_SHORT).show();
        setEditMode(false);
    }

    // --- НОВЫЕ МЕТОДЫ ДЛЯ РАБОТЫ С ФАЙЛАМИ ---

    /**
     * Сохраняет Bitmap как файл во внутреннем хранилище приложения.
     */
    private void saveAvatarToLocalFile(Bitmap bitmap) {
        // Сжимаем изображение, чтобы оно не было слишком большим
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true);

        String fileName = "avatar_" + currentUser.getUid() + ".png";
        try (FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE)) {
            resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Не удалось сохранить файл", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Загружает аватар из внутреннего хранилища и отображает его.
     */
    private void loadAvatarFromLocalFile() {
        String fileName = "avatar_" + currentUser.getUid() + ".png";
        File file = new File(getFilesDir(), fileName);

        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                Bitmap bitmap = BitmapFactory.decodeStream(fis);
                Glide.with(this).load(bitmap).circleCrop().into(avatarImageView);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Можно установить аватар по умолчанию, если файл не найден
            // avatarImageView.setImageResource(R.drawable.default_avatar);
        }
    }


    // --- Метод загрузки данных из Firebase (без аватара) ---
    private void loadUserProfile() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Загрузка имени
                    String nameFromDB = snapshot.child("name").getValue(String.class);
                    nameEditText.setText(nameFromDB != null ? nameFromDB : "Игрок");

                    // Загрузка монет
                    Integer coinsFromDB = snapshot.child("coins").getValue(Integer.class);
                    coinsTextView.setText(coinsFromDB != null ? String.valueOf(coinsFromDB) : "0");

                    // Загрузка времени в игре
                    Long totalSeconds = snapshot.child("totalPlayTime").getValue(Long.class);
                    if (totalSeconds != null) {
                        long minutes = totalSeconds / 60;
                        long hours = minutes / 60;
                        minutes %= 60;
                        String formatted = String.format("%d ч %d мин", hours, minutes);
                        timeSpentTextView.setText(formatted);
                    } else {
                        timeSpentTextView.setText("0 ч 0 мин");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Ошибка загрузки профиля: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Остальные ваши методы (setEditMode, openImagePicker, deleteAccount и т.д.) остаются без изменений ---
    private void setEditMode(boolean enable) {
        isEditMode = enable;
        nameEditText.setEnabled(enable);
        editAvatarIcon.setVisibility(enable ? View.VISIBLE : View.GONE);
        saveButton.setText(enable ? "Сохранить" : "Редактировать");

        if (enable) {
            nameEditText.setFocusableInTouchMode(true);
            nameEditText.requestFocus();
            showKeyboard(nameEditText);
        } else {
            nameEditText.setFocusable(false);
            hideKeyboard(nameEditText);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Удаление аккаунта")
                .setMessage("Вы уверены, что хотите навсегда удалить свой аккаунт? Это действие нельзя будет отменить.")
                .setPositiveButton("Удалить", (dialog, which) -> deleteAccount())
                .setNegativeButton("Отмена", null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }

    private void deleteAccount() {
        // При удалении аккаунта также удаляем локальный файл аватара
        String fileName = "avatar_" + currentUser.getUid() + ".png";
        File file = new File(getFilesDir(), fileName);
        if (file.exists()) {
            file.delete();
        }

        if (currentUser != null) {
            userRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    currentUser.delete().addOnCompleteListener(authTask -> {
                        if (authTask.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Аккаунт удалён", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Ошибка удаления аккаунта: " + authTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(ProfileActivity.this, "Ошибка удаления данных пользователя: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}