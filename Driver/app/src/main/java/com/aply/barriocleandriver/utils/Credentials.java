package com.aply.barriocleandriver.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.aply.barriocleandriver.activities.LoginActivity;

public class Credentials {
    private final Context ctx;
    private final SharedPreferences sp1;

    public Credentials(Context ctx) {
        this.ctx = ctx;
        sp1 = ctx.getSharedPreferences("LOGIN", MODE_PRIVATE);
    }

    public void saveData(String key, String data) {
        SharedPreferences.Editor Ed = sp1.edit();
        Ed.putString(key, data);
        Ed.commit();
    }

    public String getData(String key) {
        return sp1.getString(key, "");
    }

    public void logout() {
        SharedPreferences.Editor Ed = sp1.edit();
        saveData(Preference.STEP, "1");
        saveData(Preference.LOGIN, "0");
        saveData(Preference.ON_SERVICE, "0");
        saveData(Preference.TERMINOS, "0");
        saveData(Preference.TELEFONO, "");
        saveData(Preference.EMAIL, "");
        saveData(Preference.FULLNAME, "");
        saveData(Preference.TIME_PASS, "");
        Ed.commit();

        Intent intent = new Intent(ctx, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }
}
