package com.example.play;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;
import androidx.annotation.Nullable;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    private static final String PREFS_NAME = "MusicPrefs";
    private static final String KEY_POSITION = "music_position";
    private static final String KEY_TRACK = "selected_track";

    // Треки
    public static final int TRACK_1 = R.raw.background_music;
    public static final int TRACK_2 = R.raw.background_music1;
    public static final int TRACK_3 = R.raw.background_music2;
    public static final int TRACK_4 = R.raw.background_music3;

    private int currentTrackResId = -1; // Храним ID текущего играющего трека

    @Override
    public void onCreate() {
        super.onCreate();
        // В onCreate ничего не создаем, вся логика перенесена в onStartCommand
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Определяем, какой трек должен играть
        int trackToPlay = prefs.getInt(KEY_TRACK, TRACK_1);
        if (intent != null && intent.hasExtra("track")) {
            trackToPlay = intent.getIntExtra("track", TRACK_1);
        }

        // --- ГЛАВНАЯ ЛОГИКА ---

        // Сценарий 1: Музыка уже играет, и трек тот же самый. Ничего не делаем.
        if (mediaPlayer != null && mediaPlayer.isPlaying() && currentTrackResId == trackToPlay) {
            return START_STICKY; // Просто продолжаем играть
        }

        // Сценарий 2: Плеер существует, но трек нужно сменить (или он не играет).
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // Сценарий 3: Создаем и запускаем плеер с нужным треком.
        mediaPlayer = MediaPlayer.create(this, trackToPlay);

        if (mediaPlayer == null) {
            // Не удалось создать плеер (например, файл поврежден)
            stopSelf(); // Останавливаем сервис
            return START_NOT_STICKY;
        }

        currentTrackResId = trackToPlay; // Запоминаем текущий трек
        mediaPlayer.setLooping(true);

        // Восстанавливаем позицию только если трек не менялся
        boolean trackChanged = intent != null && intent.hasExtra("track") && (intent.getIntExtra("track", TRACK_1) != prefs.getInt(KEY_TRACK, TRACK_1));
        if (!trackChanged) {
            int lastPosition = prefs.getInt(KEY_POSITION, 0);
            mediaPlayer.seekTo(lastPosition);
        }

        mediaPlayer.start();

        // Сохраняем новый выбранный трек в SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_TRACK, trackToPlay);
        if (trackChanged) {
            editor.putInt(KEY_POSITION, 0); // Сбрасываем позицию при смене трека
        }
        editor.apply();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            // Сохраняем позицию только если музыка играла
            if (mediaPlayer.isPlaying()) {
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(KEY_POSITION, mediaPlayer.getCurrentPosition());
                editor.apply();
            }
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