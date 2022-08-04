package com.aply.barrioclean.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.aply.barrioclean.R;
import com.aply.barrioclean.adapters.UnidadAdapter;
import com.aply.barrioclean.utils.Credentials;
import com.aply.barrioclean.utils.RespuestaResponse;
import com.aply.barrioclean.utils.ViewDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AsesoriaActivity extends AppCompatActivity implements View.OnClickListener {

    Context ctx;
    Credentials cred;
    ViewDialog viewDialog;

    @BindView(R.id.linear_consulta)
    LinearLayout linear_consulta;
    @BindView(R.id.linear_unidades)
    LinearLayout linear_unidades;
    @BindView(R.id.linear_video)
    LinearLayout linear_video;

    TabLayout tab_tipos;

    List<JSONObject> l_express = new ArrayList<>();
    List<JSONObject> l_escala = new ArrayList<>();
    UnidadAdapter unidadAdapter;
    RecyclerView recyler_unidades;

    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_asesoria);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Asesoría");
        }
        ctx = this;
        cred = new Credentials(ctx);
        viewDialog = new ViewDialog(this);
        linear_consulta.setOnClickListener(this);
        linear_video.setOnClickListener(this);
        linear_unidades.setOnClickListener(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ctx, CotizaActivity.class));
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        AsesoriaActivity.this.finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        AlertDialog.Builder builder;
        View view;
        switch (id) {
            case R.id.linear_consulta:
                Uri uri = Uri.parse("https://wa.me/51980858922?text=Tengo%20una%20consulta");
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                i.setPackage("com.whatsapp");
                startActivity(Intent.createChooser(i, "Consulta en línea"));
                break;
            case R.id.linear_unidades:
                builder = new AlertDialog.Builder(ctx);
                view = View.inflate(ctx, R.layout.dialog_unidades, null);
                builder.setView(view);
                builder.setPositiveButton("Cerrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                recyler_unidades = view.findViewById(R.id.recycler_unidades);
                recyler_unidades.setLayoutManager(new LinearLayoutManager(ctx));
                tab_tipos = view.findViewById(R.id.tab_tipos);
                tab_tipos.addTab(tab_tipos.newTab().setText("Express"));
                tab_tipos.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        if (tab.getPosition() == 0) {
                            unidadAdapter = new UnidadAdapter(l_express, "");
                            recyler_unidades.setAdapter(unidadAdapter);
                            unidadAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                });

                unidadAdapter = new UnidadAdapter(l_express, "");
                recyler_unidades.setAdapter(unidadAdapter);
                getUnidades();
                alertDialog = builder.create();
                break;
            case R.id.linear_video:
                builder = new AlertDialog.Builder(ctx);
                view = View.inflate(ctx, R.layout.dialog_video, null);
                builder.setView(view);
                builder.setPositiveButton("Cerrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                YouTubePlayerView youTubePlayerView = view.findViewById(R.id.youtube_player_view);
                getLifecycle().addObserver(youTubePlayerView);
                alertDialog = builder.create();
                alertDialog.show();
                break;
        }
    }

    public void getUnidades() {
        viewDialog.showDialog();
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.getUnidades);
        Log.i("getUnidades_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            RespuestaResponse producto = new Gson().fromJson(response, RespuestaResponse.class);
                            l_express.clear();
                            l_escala.clear();
                            unidadAdapter.notifyDataSetChanged();
                            for (Object o : producto.getRespuesta()) {
                                JSONObject obj = (JSONObject) o;
                                obj.put("show_btn", "0");
                                l_express.add(obj);
                                l_escala.add(obj);
                            }
                            unidadAdapter.notifyDataSetChanged();
                            viewDialog.hideDialog(0);
                            alertDialog.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            viewDialog.hideDialog(0);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("getUnidades_error: " + error.getMessage());
                viewDialog.hideDialog(0);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}