package com.aply.barrioclean.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.aply.barrioclean.R;
import com.aply.barrioclean.utils.Credentials;
import com.aply.barrioclean.utils.RespuestaResponse;
import com.aply.barrioclean.utils.Utils;
import com.aply.barrioclean.utils.ViewDialog;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HelpActivity extends AppCompatActivity implements View.OnClickListener {
    Context ctx;
    Credentials cred;
    ViewDialog viewDialog;

    @BindView(R.id.img_close)
    ImageView img_close;
    @BindView(R.id.linear_use_service)
    LinearLayout linear_use_service;
    @BindView(R.id.linear_services)
    LinearLayout linear_services;
    @BindView(R.id.linear_metodos_pagos)
    LinearLayout linear_metodos_pagos;
    @BindView(R.id.linear_emergency)
    LinearLayout linear_emergency;

    /*** Dialogs ***/
    AlertDialog.Builder builder;
    View view;
    ImageView img_dialog_close;
    Button btn_enviar;
    AlertDialog alertDialog;
    EditText et_message;
    int tipo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        ButterKnife.bind(this);
        ctx = this;
        cred = new Credentials(ctx);
        viewDialog = new ViewDialog(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        img_close.setOnClickListener(this);
        linear_use_service.setOnClickListener(this);
        linear_services.setOnClickListener(this);
        linear_metodos_pagos.setOnClickListener(this);
        linear_emergency.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.img_close:
                Intent i2 = new Intent(ctx, MenuActivity.class);
                startActivity(i2);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                HelpActivity.this.finish();
                break;
            case R.id.linear_use_service:
            case R.id.linear_metodos_pagos:
            case R.id.linear_services:
            case R.id.linear_emergency:
                if (id == R.id.linear_use_service) {
                    tipo = 1;
                    view = View.inflate(ctx, R.layout.use_service_dialog, null);
                } else if (id == R.id.linear_services) {
                    tipo = 2;
                    view = View.inflate(ctx, R.layout.services_dialog, null);
                } else if (id == R.id.linear_metodos_pagos) {
                    tipo = 3;
                    view = View.inflate(ctx, R.layout.payments_methods_dialog, null);
                } else if (id == R.id.linear_emergency) {
                    tipo = 4;
                    view = View.inflate(ctx, R.layout.emergency_dialog, null);
                }
                builder = new AlertDialog.Builder(ctx);
                builder.setView(view);
                img_dialog_close = view.findViewById(R.id.img_dialog_close);
                btn_enviar = view.findViewById(R.id.btn_enviar);
                et_message = view.findViewById(R.id.et_message);
                alertDialog = builder.create();
                img_dialog_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                btn_enviar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String mensaje = et_message.getText().toString();
                        if (mensaje.isEmpty()) {
                            new Utils().createAlert(ctx, "Mensaje no puede estar vacío", 1);
                            return;
                        }
                        sendMailHelp(alertDialog, mensaje);
                    }
                });

                alertDialog.show();
                break;
        }
    }

    public void sendMailHelp(AlertDialog alert, String mensaje) {
        viewDialog.showDialog();
        String url = ctx.getResources().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.sendMailHelp);
        Log.i("sendMailHelp_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            RespuestaResponse login = new Gson().fromJson(response, RespuestaResponse.class);
                            if (login.getIde_error() == 0) {
                                alert.dismiss();
                                viewDialog.hideDialog(0);
                                new Utils().createAlert(ctx, login.getMsg_error(), 2);
                            } else {
                                viewDialog.hideDialog(0);
                                new Utils().createAlert(ctx, login.getMsg_error(), 1);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            viewDialog.hideDialog(0);
                            new Utils().createAlert(ctx, e.getMessage(), 1);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("sendMailHelp_error: " + error.getMessage());
                viewDialog.hideDialog(0);
                new Utils().createAlert(ctx, error.getMessage(), 1);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("mensaje", mensaje);
                params.put("name", cred.getData("full_name"));
                params.put("email", cred.getData("email"));
                params.put("tipo", String.valueOf(tipo));
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}