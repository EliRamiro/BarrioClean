package com.aply.barrioclean.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aply.barrioclean.R;
import com.aply.barrioclean.utils.Credentials;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShareActivity extends AppCompatActivity implements View.OnClickListener {
    Context ctx;
    Credentials cred;
    @BindView(R.id.img_close)
    ImageView img_close;
    @BindView(R.id.tv_codigo_promocion)
    TextView tv_codigo_promocion;
    @BindView(R.id.tv_copiar)
    TextView tv_copiar;
    @BindView(R.id.btn_share)
    Button btn_share;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        ButterKnife.bind(this);
        ctx = this;
        cred = new Credentials(ctx);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        img_close.setOnClickListener(this);
        String codigo = cred.getData("codigo_promocional");
        tv_codigo_promocion.setText(codigo);
        tv_copiar.setOnClickListener(this);
        btn_share.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.img_close:
                Intent i2 = new Intent(ctx, MenuActivity.class);
                startActivity(i2);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                ShareActivity.this.finish();
                break;
            case R.id.tv_copiar:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // Creates a new text clip to put on the clipboard
                ClipData clip = ClipData.newPlainText(null, cred.getData("codigo_promocional"));
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ctx, "Código copiado", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, cred.getData("codigo_promocional"));
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);
                break;
        }
    }
}