package com.example.play;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.content.SharedPreferences;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    private static final String PREFS_NAME = "MusicPrefs";
    private static final String KEY_POSITION = "music_position";

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int lastPosition = prefs.getInt(KEY_POSITION, 0);

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
            mediaPlayer.setLooping(true);
        }

        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(lastPosition);
            mediaPlayer.start();
        }

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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
