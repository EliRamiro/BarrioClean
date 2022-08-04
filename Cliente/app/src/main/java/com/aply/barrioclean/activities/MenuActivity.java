package com.aply.barrioclean.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aply.barrioclean.BuildConfig;
import com.aply.barrioclean.R;
import com.aply.barrioclean.utils.Credentials;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {
    Context ctx;
    Credentials cred;
    @BindView(R.id.img_back_arrow)
    ImageView img_back_arrow;
    @BindView(R.id.tv_usuario)
    TextView tv_usuario;
    @BindView(R.id.txt_actualizar)
    TextView txt_actualizar;
    @BindView(R.id.tv_version)
    TextView tv_version;
    @BindView(R.id.linear_information)
    LinearLayout linear_information;
    @BindView(R.id.linear_sign_out)
    LinearLayout linear_sign_out;
    @BindView(R.id.linear_share)
    LinearLayout linear_share;
    @BindView(R.id.linear_help)
    LinearLayout linear_help;
    @BindView(R.id.linear_pregunta)
    LinearLayout linear_pregunta;
    @BindView(R.id.linear_historial)
    LinearLayout linear_historial;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);
        ctx = this;
        cred = new Credentials(ctx);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        String welcome = "!Hola " + cred.getData("full_name") + "!";
        tv_usuario.setText(welcome);
        String app_version = "Versión. " + BuildConfig.VERSION_NAME;
        tv_version.setText(app_version);

        img_back_arrow.setOnClickListener(this);
        linear_historial.setOnClickListener(this);
        linear_sign_out.setOnClickListener(this);
        linear_share.setOnClickListener(this);
        linear_help.setOnClickListener(this);
        linear_information.setOnClickListener(this);
        linear_pregunta.setOnClickListener(this);
        txt_actualizar.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent;
        switch (id) {
            case R.id.img_back_arrow:
                startActivity(new Intent(ctx, MapsActivity.class));
                MenuActivity.this.finish();
                break;
            case R.id.txt_actualizar:

                break;
            case R.id.linear_information:
                intent = new Intent(ctx, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                MenuActivity.this.finish();
                break;
            case R.id.linear_historial:
                intent = new Intent(ctx, HistoryActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                MenuActivity.this.finish();
                break;
            case R.id.linear_sign_out:
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setTitle("¡Upss!");
                builder.setMessage("¿Seguro que quieres cerrar sesión?");
                builder.setPositiveButton("Cerrar Sesión", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cred.logout();
                    }
                });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                break;
            case R.id.linear_share:
                intent = new Intent(ctx, ShareActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                MenuActivity.this.finish();
                break;
            case R.id.linear_help:
                intent = new Intent(ctx, HelpActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                MenuActivity.this.finish();
                break;
            case R.id.linear_pregunta:
                intent = new Intent(ctx, PreguntasActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                MenuActivity.this.finish();
                break;
        }
    }
}