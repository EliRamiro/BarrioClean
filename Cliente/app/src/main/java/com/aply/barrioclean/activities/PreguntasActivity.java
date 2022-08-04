package com.aply.barrioclean.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aply.barrioclean.R;
import com.aply.barrioclean.adapters.PreguntaAdapter;
import com.aply.barrioclean.utils.Credentials;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class PreguntasActivity extends AppCompatActivity {

    Context ctx;
    Credentials cred;

    PreguntaAdapter preguntaAdapter;
    List<JSONObject> l = new ArrayList<>();
    @BindView(R.id.recycler_preguntas)
    RecyclerView recycler_preguntas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preguntas);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Preguntas Frecuentes");
        }
        ctx = this;
        cred = new Credentials(ctx);

        recycler_preguntas = findViewById(R.id.recycler_preguntas);
        recycler_preguntas.setLayoutManager(new LinearLayoutManager(ctx));
        preguntaAdapter = new PreguntaAdapter(l);
        recycler_preguntas.setAdapter(preguntaAdapter);
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
}