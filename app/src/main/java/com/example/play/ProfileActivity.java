package com.example.play;

import android.annotation.SuppressLint;
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
    private TextView coinsTextView;
    private TextView timeSpentTextView;
    private ImageButton backButton;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    private boolean isEditing = false;
    private int coins;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        backButton = findViewById(R.id.buttonBack);

        // Нажали "Назад" — просто закрываем текущую активность
        backButton.setOnClickListener(v -> finish());

        // Инициализация вью
        emailTextView = findViewById(R.id.emailTextView);
        nameEditText = findViewById(R.id.nameEditText);
        editSaveButton = findViewById(R.id.saveButton);
        coinsTextView = findViewById(R.id.coinsTextView);
        timeSpentTextView = findViewById(R.id.timeSpentTextView);
        timeSpentTextView.setText("Время в игре: 00:00");

        // Получаем текущего пользователя
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

        // Загрузка и инициализация профиля
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Первый вход — создаем все нужные поля
                    userRef.child("email").setValue(currentUser.getEmail());
                    userRef.child("name").setValue("");
                    userRef.child("coins").setValue(0);
                    userRef.child("maxLevel").setValue(1);  // Первый уровень доступен
                    userRef.child("totalPlayTime").setValue(0L);

                    nameEditText.setText("");
                    coinsTextView.setText("Монеты: 0");
                    timeSpentTextView.setText("Время в игре: 00:00");
                } else {
                    // Профиль уже существует — загружаем данные
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

                // Обновляем email каждый раз
                userRef.child("email").setValue(currentUser.getEmail());

                nameEditText.setEnabled(false);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Ошибка загрузки профиля: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Обработка кнопки "Редактировать / Сохранить"
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
    }

    public void awardCoin() {
        coins++;
        coinsTextView.setText("Монеты: " + coins);
        userRef.child("coins").setValue(coins);
    }
}
