package com.aply.barriocleandriver.activities;

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

import com.aply.barriocleandriver.BuildConfig;
import com.aply.barriocleandriver.R;
import com.aply.barriocleandriver.utils.Credentials;
import com.aply.barriocleandriver.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {
    Context ctx;
    Credentials cred;
    @BindView(R.id.img_back_arrow)
    ImageView img_back_arrow;
    @BindView(R.id.tv_usuario)
    TextView tv_usuario;
    @BindView(R.id.tv_actualizar)
    TextView tv_actualizar;
    @BindView(R.id.tv_version)
    TextView tv_version;
    @BindView(R.id.linear_documentos)
    LinearLayout linear_documentos;
    @BindView(R.id.linear_history)
    LinearLayout linear_history;
    @BindView(R.id.linear_profile)
    LinearLayout linear_profile;
    @BindView(R.id.linear_sign_out)
    LinearLayout linear_sign_out;
    @BindView(R.id.linear_share)
    LinearLayout linear_share;
    @BindView(R.id.linear_help)
    LinearLayout linear_help;
    @BindView(R.id.linear_pregunta)
    LinearLayout linear_pregunta;
    @BindView(R.id.icon_warning)
    ImageView icon_warning;

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
        linear_sign_out.setOnClickListener(this);
        linear_share.setOnClickListener(this);
        linear_help.setOnClickListener(this);
        linear_documentos.setOnClickListener(this);
        linear_history.setOnClickListener(this);
        linear_profile.setOnClickListener(this);
        linear_pregunta.setOnClickListener(this);
        if (cred.getData("is_validate").isEmpty()) {
            icon_warning.setVisibility(View.VISIBLE);
        } else {
            icon_warning.setVisibility(View.GONE);
        }
        tv_actualizar.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent;
        switch (id) {
            case R.id.img_back_arrow:
                startActivity(new Intent(ctx, PrincipalActivity.class));
                MenuActivity.this.finish();
                break;
            case R.id.tv_actualizar:
                new Utils().getUserDetails(ctx);
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
                //MenuActivity.this.recreate();
                break;
            case R.id.linear_documentos:
                intent = new Intent(ctx, DocumentActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                MenuActivity.this.finish();
                break;
            case R.id.linear_history:
                intent = new Intent(ctx, HistoryActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                MenuActivity.this.finish();
                break;
            case R.id.linear_profile:
                intent = new Intent(ctx, ProfileActivity.class);
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
//                intent = new Intent(ctx, ShareActivity.class);
//                startActivity(intent);
//                overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
//                MenuActivity.this.finish();
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