package com.aply.barrioclean.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.aply.barrioclean.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TermsActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.back_arrow)
    ImageView back_arrow;

    Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);
        ButterKnife.bind(this);
        ctx = this;
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        back_arrow.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ctx, LoginActivity.class));
        TermsActivity.this.finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.back_arrow:
                onBackPressed();
                break;
        }
    }
}