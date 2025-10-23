package com.example.play;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class GameLevels extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gamelevels);

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Button button_back = findViewById(R.id.button_back);
        button_back.setOnClickListener(view -> {
            try {
                Intent intent = new Intent(GameLevels.this, MainActivity.class);
                startActivity(intent);
                finish();
            } catch (Exception e) {
            }
        });

        // Получаем maxLevel из Firebase и блокируем уровни выше
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userRef.child("maxLevel").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int maxLevel = 1;

                if (!snapshot.exists()) {
                    // Новый пользователь — создаём начальное значение
                    userRef.child("maxLevel").setValue(1);
                } else {
                    Integer level = snapshot.getValue(Integer.class);
                    if (level != null) maxLevel = level;
                }

                for (int i = 1; i <= 6; i++) {
                    int resID = getResources().getIdentifier("textView" + i, "id", getPackageName());
                    TextView levelBtn = findViewById(resID);

                    if (i <= maxLevel) {
                        levelBtn.setEnabled(true);
                        levelBtn.setAlpha(1f);
                        int levelToOpen = i;
                        levelBtn.setOnClickListener(v -> {
                            try {
                                Intent intent;
                                switch (levelToOpen) {
                                    case 1: intent = new Intent(GameLevels.this, Level1.class); break;
                                    case 2: intent = new Intent(GameLevels.this, Level2.class); break;
                                    case 3: intent = new Intent(GameLevels.this, Level3.class); break;
                                    case 4: intent = new Intent(GameLevels.this, Level4.class); break;
                                    case 5: intent = new Intent(GameLevels.this, Level5.class); break;
                                    case 6: intent = new Intent(GameLevels.this, Level6.class); break;
                                    case 7: intent = new Intent(GameLevels.this, Level7.class); break;
                                    default: return;
                                }
                                startActivity(intent);
                                finish();
                            } catch (Exception e) { }
                        });
                    } else {
                        levelBtn.setEnabled(false);
                        levelBtn.setAlpha(0.5f);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        try {
            Intent intent = new Intent(GameLevels.this, MainActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
        }
    }
}
