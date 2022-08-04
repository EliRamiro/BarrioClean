package com.aply.barriocleandriver;

import android.app.Application;
import android.content.Context;

public class BaseApplication extends Application {
    public static Context ctx;

    @Override
    public void onCreate() {
        super.onCreate();
        ctx = this;
    }
}
