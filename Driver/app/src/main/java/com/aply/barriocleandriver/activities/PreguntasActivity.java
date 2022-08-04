package com.aply.barriocleandriver.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aply.barriocleandriver.R;
import com.aply.barriocleandriver.adapters.PreguntaAdapter;
import com.aply.barriocleandriver.utils.Credentials;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PreguntasActivity extends AppCompatActivity implements View.OnClickListener {

    Context ctx;
    Credentials cred;

    @BindView(R.id.btn_cerrar)
    ImageView btn_cerrar;

    PreguntaAdapter preguntaAdapter;
    List<JSONObject> l = new ArrayList<>();
    @BindView(R.id.recycler_preguntas)
    RecyclerView recycler_preguntas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preguntas);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        ctx = this;
        cred = new Credentials(ctx);

        recycler_preguntas.setLayoutManager(new LinearLayoutManager(ctx));
        preguntaAdapter = new PreguntaAdapter(l);
        recycler_preguntas.setAdapter(preguntaAdapter);
        btn_cerrar.setOnClickListener(this);
        getPreguntas();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ctx, MenuActivity.class));
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        PreguntasActivity.this.finish();
    }

    public void getPreguntas() {
        try {
            JSONArray faqs = (JSONArray) new JSONParser().parse(ctx.getResources().getString(R.string.faqs));
            l.clear();
            preguntaAdapter.notifyDataSetChanged();
            for (Object o : faqs) {
                JSONObject obj = (JSONObject) o;
                l.add(obj);
            }
            preguntaAdapter.notifyDataSetChanged();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_cerrar:
                Intent i2 = new Intent(ctx, MenuActivity.class);
                startActivity(i2);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                PreguntasActivity.this.finish();
                break;
        }
    }
}