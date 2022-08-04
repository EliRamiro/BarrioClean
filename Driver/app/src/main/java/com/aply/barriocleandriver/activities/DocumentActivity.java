package com.aply.barriocleandriver.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.aply.barriocleandriver.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DocumentActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.linear_brevete)
    LinearLayout linear_brevete;
    @BindView(R.id.linear_soat)
    LinearLayout linear_soat;
    @BindView(R.id.linear_tarjeta)
    LinearLayout linear_tarjeta;
    @BindView(R.id.btn_cerrar)
    ImageView btn_cerrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        linear_brevete.setOnClickListener(this);
        linear_soat.setOnClickListener(this);
        linear_tarjeta.setOnClickListener(this);
        btn_cerrar.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.linear_brevete:
                startActivity(new Intent(this, BreveteActivity.class));
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                break;
            case R.id.linear_soat:
                startActivity(new Intent(this, SoatActivity.class));
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                break;
            case R.id.linear_tarjeta:
                startActivity(new Intent(this, TarjetaPropiedadActivity.class));
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                break;
            case R.id.btn_cerrar:
                startActivity(new Intent(this, MenuActivity.class));
                DocumentActivity.this.finish();
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                break;
        }
    }
}