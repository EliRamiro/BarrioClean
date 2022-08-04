package com.aply.barriocleandriver.utils;

import android.app.Application;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public class ArchLifecycleApp extends Application implements LifecycleObserver {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        //App in background
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
        // App in foreground
    }
}
