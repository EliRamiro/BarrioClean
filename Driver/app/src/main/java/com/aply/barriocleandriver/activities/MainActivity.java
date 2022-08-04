package com.aply.barriocleandriver.activities;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.aply.barriocleandriver.R;
import com.aply.barriocleandriver.utils.Credentials;
import com.aply.barriocleandriver.utils.Utils;

public class MainActivity extends AppCompatActivity {

    Context ctx;
    Credentials cred;
    Utils utils = new Utils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this;
        utils = new Utils(ctx);
        utils.initializeFirebase(ctx);
    }
}