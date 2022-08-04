package com.aply.barrioclean.activities;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aply.barrioclean.R;
import com.aply.barrioclean.utils.Credentials;
import com.aply.barrioclean.utils.Preference;
import com.aply.barrioclean.utils.Utils;

import butterknife.ButterKnife;

public class SplashActivity extends AppCompatActivity {
    Context ctx;
    Credentials cred;
    Utils utils = new Utils();
    int granted = 0;
    String[] permissions = new String[]{WRITE_EXTERNAL_STORAGE, READ_PHONE_STATE, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, CAMERA, CALL_PHONE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        ctx = this;
        cred = new Credentials(ctx);
        utils = new Utils(ctx);
        utils.initializeFirebase(ctx);
        utils.getConfigs(ctx);
        requestPermissions(permissions, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length != permissions.length) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            } else {
                new Handler().postDelayed(() -> {
                    String on_boarding = cred.getData(Preference.ONBOARDING);
                    if (on_boarding.isEmpty() || on_boarding.equals("0")) {
                        ctx.startActivity(new Intent(ctx, IntroActivity.class));
                    } else {
                        if (cred.getData(Preference.LOGIN).equals("1")) {
                            ctx.startActivity(new Intent(ctx, MapsActivity.class));
                        } else {
                            ctx.startActivity(new Intent(ctx, LoginActivity.class));
                        }
                    }
                    SplashActivity.this.finish();
                }, 1500);
            }
        }
    }
}