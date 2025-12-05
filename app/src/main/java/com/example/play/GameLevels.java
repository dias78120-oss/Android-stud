package com.example.play;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool; // ИМПОРТИРУЕМ SoundPool
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GameLevels extends AppCompatActivity {

    private static final int TOTAL_LEVELS = 20;
    private RecyclerView levelsRecyclerView;
    private LevelsAdapter adapter;

    // --- ИЗМЕНЕНИЯ ДЛЯ ЗВУКА ---
    private SoundPool soundPool;
    private int clickSoundId;
    private boolean soundLoaded = false;
    // --- КОНЕЦ ИЗМЕНЕНИЙ ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gamelevels);

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // --- ИЗМЕНЕНИЯ ДЛЯ ЗВУКА ---
        // Настраиваем SoundPool
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME) // Указываем, что это игровой звук
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .setMaxStreams(5) // Максимум 5 звуков одновременно
                .build();

        // Загружаем звук и получаем его ID
        clickSoundId = soundPool.load(this, R.raw.ui_click, 1);

        // Устанавливаем слушатель, чтобы знать, когда звук загрузился
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                // Успешная загрузка
                soundLoaded = true;
            }
        });
        // --- КОНЕЦ ИЗМЕНЕНИЙ ---

        Button button_back = findViewById(R.id.button_back);
        button_back.setOnClickListener(view -> {
            playSound();
            try {
                Intent intent = new Intent(GameLevels.this, MainActivity.class);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        levelsRecyclerView = findViewById(R.id.levelsRecyclerView);
        fetchUserMaxLevelAndSetupAdapter();
    }

    // УДАЛЯЕМ onStart и onStop, так как SoundPool управляется иначе

    private void fetchUserMaxLevelAndSetupAdapter() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid());

        userRef.child("maxLevel").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int maxLevel = 1;
                if (snapshot.exists()) {
                    Integer levelFromDb = snapshot.getValue(Integer.class);
                    if (levelFromDb != null) maxLevel = levelFromDb;
                } else {
                    userRef.child("maxLevel").setValue(1);
                }

                List<Level> levels = new ArrayList<>();
                for (int i = 1; i <= TOTAL_LEVELS; i++) {
                    levels.add(new Level(i, i <= maxLevel));
                }

                adapter = new LevelsAdapter(levels, maxLevel);
                levelsRecyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GameLevels.this, "Не удалось загрузить прогресс", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- ИЗМЕНЕНИЯ ДЛЯ ЗВУКА ---
    private void playSound() {
        // Проигрываем звук только если он загружен
        if (soundLoaded) {
            soundPool.play(clickSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Освобождаем ресурсы SoundPool, когда активити уничтожается
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
    // --- КОНЕЦ ИЗМЕНЕНИЙ ---

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            Intent intent = new Intent(GameLevels.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ... (остальной код Level, LevelsAdapter, onBindViewHolder и т.д. остается БЕЗ ИЗМЕНЕНИЙ) ...
    private static class Level {
        final int number;
        final boolean isUnlocked;
        Level(int number, boolean isUnlocked) { this.number = number; this.isUnlocked = isUnlocked; }
    }

    private class LevelsAdapter extends RecyclerView.Adapter<LevelsAdapter.LevelViewHolder> {

        private final List<Level> levels;
        private final int maxLevel;
        private int lastPosition = -1;

        LevelsAdapter(List<Level> levels, int maxLevel) {
            this.levels = levels;
            this.maxLevel = maxLevel;
        }

        @NonNull
        @Override
        public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.level_item_card, parent, false);
            return new LevelViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {

            Level level = levels.get(position);
            holder.levelNumberTextView.setText(String.valueOf(level.number));

            if (level.isUnlocked) {
                holder.levelNumberTextView.setVisibility(View.VISIBLE);
                holder.lockIcon.setVisibility(View.GONE);
                holder.cardBackground.setBackgroundResource(R.drawable.level_card_unlocked_bg);

                if (level.number == maxLevel) {
                    Animation pulse = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.pulse_animation);
                    holder.itemView.startAnimation(pulse);
                } else {
                    holder.itemView.clearAnimation();
                }

                holder.itemView.setOnClickListener(v -> {
                    int currentPosition = holder.getAdapterPosition();

                    if (currentPosition != RecyclerView.NO_POSITION) {
                        Level clickedLevel = levels.get(currentPosition);

                        playSound();
                        v.postDelayed(() -> {
                            try {
                                Intent intent;
                                switch (clickedLevel.number) {
                                    case 1: intent = new Intent(GameLevels.this, Level1.class); break;
                                    case 2: intent = new Intent(GameLevels.this, Level2.class); break;
                                    case 3: intent = new Intent(GameLevels.this, Level3.class); break;
                                    case 4: intent = new Intent(GameLevels.this, Level4.class); break;
                                    case 5: intent = new Intent(GameLevels.this, Level5.class); break;
                                    case 6: intent = new Intent(GameLevels.this, Level6.class); break;
                                    case 7: intent = new Intent(GameLevels.this, Level7.class); break;
                                    case 8: intent = new Intent(GameLevels.this, Level8.class); break;
                                    case 9: intent = new Intent(GameLevels.this, Level9.class); break;
                                    case 10: intent = new Intent(GameLevels.this, Level10.class); break;
                                    default: return;
                                }
                                startActivity(intent);
                                finish();
                            } catch (Exception e) { e.printStackTrace(); }
                        }, 150);
                    }
                });
            } else {
                holder.levelNumberTextView.setVisibility(View.GONE);
                holder.lockIcon.setVisibility(View.VISIBLE);
                holder.cardBackground.setBackgroundResource(R.drawable.level_card_locked_bg);
                holder.itemView.clearAnimation();
                holder.itemView.setOnClickListener(null);
            }

            if (position > lastPosition) {
                Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.level_item_animation);
                holder.itemView.startAnimation(animation);
                lastPosition = position;
            }
        }

        @Override
        public int getItemCount() {
            return levels.size();
        }

        class LevelViewHolder extends RecyclerView.ViewHolder {
            final TextView levelNumberTextView;
            final ImageView lockIcon;
            final FrameLayout cardBackground;

            LevelViewHolder(@NonNull View itemView) {
                super(itemView);
                levelNumberTextView = itemView.findViewById(R.id.levelNumberTextView);
                lockIcon = itemView.findViewById(R.id.lockIcon);
                cardBackground = itemView.findViewById(R.id.card_background);
            }
        }
    }
}