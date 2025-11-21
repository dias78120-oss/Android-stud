package com.example.play;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    private static final String PREFS_NAME = "MusicPrefs";
    private static final String KEY_POSITION = "music_position";
    private static final String KEY_TRACK = "selected_track";

    // Доступные треки
    public static final int TRACK_1 = R.raw.background_music;
    public static final int TRACK_2 = R.raw.background_music1;


    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int selectedTrack = prefs.getInt(KEY_TRACK, TRACK_1);
        mediaPlayer = MediaPlayer.create(this, selectedTrack);
        mediaPlayer.setLooping(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int lastPosition = prefs.getInt(KEY_POSITION, 0);
        int selectedTrack = prefs.getInt(KEY_TRACK, TRACK_1);

        // Получаем трек от пользователя через Intent
        if (intent != null && intent.hasExtra("track")) {
            selectedTrack = intent.getIntExtra("track", TRACK_1);
            // Сохраняем выбор трека
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_TRACK, selectedTrack);
            editor.apply();
        }

        // Если MediaPlayer уже играет, остановим и создадим заново с новым треком
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(this, selectedTrack);
        mediaPlayer.setLooping(true);

        // Воспроизводим с последней позиции
        mediaPlayer.seekTo(lastPosition);
        mediaPlayer.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            // Сохраняем текущую позицию
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_POSITION, mediaPlayer.getCurrentPosition());
            editor.apply();

            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
