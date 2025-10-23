package com.example.play;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.SystemClock;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class AppTime extends Application {

    private long sessionStartTime;
    private long totalPlayTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            private int activityCount = 0;

            @Override
            public void onActivityResumed(Activity activity) {
                if (activityCount == 0) {
                    // Приложение стало активным
                    sessionStartTime = SystemClock.elapsedRealtime();
                }
                activityCount++;
            }

            @Override
            public void onActivityPaused(Activity activity) {
                activityCount--;
                if (activityCount == 0) {
                    // Приложение ушло в фон
                    long sessionEndTime = SystemClock.elapsedRealtime();
                    long sessionSeconds = (sessionEndTime - sessionStartTime) / 1000;

                    totalPlayTime += sessionSeconds;

                    // Сохраняем в Firebase
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        FirebaseDatabase.getInstance().getReference("users")
                                .child(uid)
                                .child("totalPlayTime")
                                .setValue(totalPlayTime);
                    }
                }
            }

            // Остальные методы можно оставить пустыми
            @Override public void onActivityCreated(Activity activity, Bundle bundle) {}
            @Override public void onActivityStarted(Activity activity) {}
            @Override public void onActivityStopped(Activity activity) {}
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {}
            @Override public void onActivityDestroyed(Activity activity) {}
        });
    }
}
