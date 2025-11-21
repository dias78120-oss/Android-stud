package com.example.play;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private EditText nameEditText;
    private TextView emailTextView;
    private Button editSaveButton;
    private Button deleteButton;
    private TextView coinsTextView;
    private TextView timeSpentTextView;
    private ImageButton backButton;

    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    private boolean isEditing = false;
    private int coins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Инициализация вью
        backButton = findViewById(R.id.buttonBack);
        emailTextView = findViewById(R.id.emailTextView);
        nameEditText = findViewById(R.id.nameEditText);
        editSaveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        coinsTextView = findViewById(R.id.coinsTextView);
        timeSpentTextView = findViewById(R.id.timeSpentTextView);
        timeSpentTextView.setText("Время в игре: 00:00");

        // Обработка кнопки "Назад"
        backButton.setOnClickListener(v -> finish());

        // Получаем текущего пользователя Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        emailTextView.setText(currentUser.getEmail());

        // Ссылка на узел пользователя
        String uid = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        // Загрузка данных профиля
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Первый вход — создаем поля
                    userRef.child("email").setValue(currentUser.getEmail());
                    userRef.child("name").setValue("");
                    userRef.child("coins").setValue(0);
                    userRef.child("totalPlayTime").setValue(0L);
                    nameEditText.setText("");
                    coinsTextView.setText("Монеты: 0");
                    timeSpentTextView.setText("Время в игре: 00:00");
                } else {
                    // Профиль существует — загружаем данные
                    String nameFromDB = snapshot.child("name").getValue(String.class);
                    Integer coinsFromDB = snapshot.child("coins").getValue(Integer.class);
                    Long totalSeconds = snapshot.child("totalPlayTime").getValue(Long.class);

                    if (nameFromDB != null) nameEditText.setText(nameFromDB);
                    if (coinsFromDB != null) {
                        coins = coinsFromDB;
                        coinsTextView.setText("Монеты: " + coins);
                    }
                    if (totalSeconds != null) {
                        long minutes = totalSeconds / 60;
                        long hours = minutes / 60;
                        minutes %= 60;
                        String formatted = String.format("Время в игре: %02d:%02d", hours, minutes);
                        timeSpentTextView.setText(formatted);
                    }
                }
                nameEditText.setEnabled(false);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Ошибка загрузки профиля: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Кнопка редактировать/сохранить имя
        editSaveButton.setOnClickListener(v -> {
            if (isEditing) {
                String newName = nameEditText.getText().toString().trim();
                userRef.child("name").setValue(newName)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Имя сохранено!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Ошибка сохранения: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                nameEditText.setEnabled(false);
                editSaveButton.setText("Редактировать");
            } else {
                nameEditText.setEnabled(true);
                nameEditText.requestFocus();
                editSaveButton.setText("Сохранить");
            }
            isEditing = !isEditing;
        });

        // Кнопка удаления аккаунта
        deleteButton.setOnClickListener(v -> {
            if (currentUser != null) {
                String uidToDelete = currentUser.getUid();

                // Удаляем из Firebase Realtime DB
                userRef.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Удаляем аккаунт из Firebase Auth
                        currentUser.delete().addOnCompleteListener(authTask -> {
                            if (authTask.isSuccessful()) {
                                Toast.makeText(ProfileActivity.this, "Аккаунт удалён", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                                finish();
                            } else {
                                Toast.makeText(ProfileActivity.this, "Ошибка удаления аккаунта: " + authTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(ProfileActivity.this, "Ошибка удаления данных пользователя", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // Метод для начисления монеты
    public void awardCoin() {
        coins++;
        coinsTextView.setText("Монеты: " + coins);
        userRef.child("coins").setValue(coins);
    }
}
