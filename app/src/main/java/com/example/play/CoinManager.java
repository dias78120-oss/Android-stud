package com.example.play;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class CoinManager {
    private static final String TAG = "CoinManager";

    /**
     * Вызывается после успешного прохождения уровня {@code level}.
     * Если этот уровень выше сохранённого maxLevel — даёт 1 монету
     * и обновляет maxLevel в базе.
     *
     * @param context контекст для Toast
     * @param level   номер уровня, который только что пройден
     */
    public static void addCoin(Context context, int level) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid());

        // 1) Читаем текущее maxLevel
        userRef.child("maxLevel").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int currentMax = snapshot.exists()
                        ? snapshot.getValue(Integer.class)
                        : 1;  // если нет — считаем, что по умолчанию 1

                // 2) Если прошли новый уровень выше текущего maxLevel
                if (level > currentMax) {
                    // a) Обновляем maxLevel в базе
                    userRef.child("maxLevel").setValue(level);

                    // b) Теперь выдаём 1 монету
                    userRef.child("coins").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapCoins) {
                            int currentCoins = snapCoins.exists()
                                    ? snapCoins.getValue(Integer.class)
                                    : 0;  // если нет — начинаем с 0

                            currentCoins += 1;

                            // c) Сохраняем обновлённое количество монет
                            int finalCurrentCoins = currentCoins;
                            userRef.child("coins").setValue(currentCoins)
                                    .addOnSuccessListener(aVoid -> Toast.makeText(context,
                                            "Вы прошли уровень " + level +
                                                    "! Получено 1 монета. Всего монет: " + finalCurrentCoins,
                                            Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Ошибка сохранения монет", e);
                                        Toast.makeText(context,
                                                "Ошибка при сохранении монет: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    });
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.e(TAG, "Ошибка чтения монет: " + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Ошибка чтения maxLevel: " + error.getMessage());
            }
        });
    }
}
