package com.aply.barrioclean.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.aply.barrioclean.activities.LoginActivity;

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
        saveData("step", "1");
        saveData("login", "0");
        saveData("terminos", "0");
        saveData("telefono", "");
        saveData("email", "");
        saveData("full_name", "");
        Ed.commit();

        Intent intent = new Intent(ctx, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }
}
