package com.aply.barriocleandriver.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.aply.barriocleandriver.R;
import com.aply.barriocleandriver.adapters.HistoryAdapter;
import com.aply.barriocleandriver.utils.Credentials;
import com.aply.barriocleandriver.utils.Preference;
import com.aply.barriocleandriver.utils.RespuestaResponse;
import com.aply.barriocleandriver.utils.Utils;
import com.aply.barriocleandriver.utils.ViewDialog;
import com.google.gson.Gson;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryActivity extends AppCompatActivity implements View.OnClickListener {

    List<JSONObject> l = new ArrayList<>();
    @BindView(R.id.recycler_services)
    RecyclerView recycler_services;
    HistoryAdapter historyAdapter;
    @BindView(R.id.btn_cerrar)
    ImageView btn_cerrar;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipe_refresh;

    Credentials cred;
    Context ctx;
    ViewDialog viewDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        ctx = this;
        cred = new Credentials(ctx);
        new Utils().getUserDetails(ctx);
        new Utils().getConfigs(ctx);
        viewDialog = new ViewDialog(this);
        viewDialog.showDialog();
        recycler_services.setLayoutManager(new LinearLayoutManager(ctx));

        historyAdapter = new HistoryAdapter(l);
        recycler_services.setAdapter(historyAdapter);

        btn_cerrar.setOnClickListener(this);

        getServices();

        swipe_refresh.setOnRefreshListener(() -> {
            swipe_refresh.setRefreshing(true);
            getServices();
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_cerrar:
                startActivity(new Intent(this, MenuActivity.class));
                HistoryActivity.this.finish();
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                break;
        }

    }

    void getServices() {
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.getHistorial);
        Log.i("getServices_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        RespuestaResponse services = new Gson().fromJson(response, RespuestaResponse.class);
                        if (services.getIde_error() == 0) {
                            JSONArray obj = services.getRespuesta();
                            l.clear();
                            for (Object o : obj) {
                                JSONObject json = (JSONObject) o;
                                l.add(json);
                            }
                            historyAdapter.notifyDataSetChanged();
                            viewDialog.hideDialog(0);
                        } else {
                            viewDialog.hideDialog(0);
                            new Utils().createAlert(ctx, services.getMsg_error(), 1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        viewDialog.hideDialog(0);
                        new Utils().createAlert(ctx, e.getMessage(), 1);
                    }
                    swipe_refresh.setRefreshing(false);
                }, error -> {
            System.out.println("getServices_error: " + error.getMessage());
            viewDialog.hideDialog(0);
            new Utils().createAlert(ctx, error.getMessage(), 1);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", cred.getData(Preference.USERID));
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}