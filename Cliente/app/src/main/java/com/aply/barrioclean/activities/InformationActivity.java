package com.aply.barrioclean.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class InformationActivity extends AppCompatActivity implements View.OnClickListener {

    Context ctx;
    Credentials cred;
    ViewDialog viewDialog;
    @BindView(R.id.linear_change_password)
    LinearLayout change_password;
    @BindView(R.id.linear_change_phone)
    LinearLayout change_phone;
    @BindView(R.id.linear_metodos_pagos)
    LinearLayout metodos_pago;
    @BindView(R.id.tv_nombre)
    TextView tv_nombre;
    @BindView(R.id.tv_email)
    TextView tv_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mi Información");
        }
        ctx = this;
        cred = new Credentials(ctx);
        viewDialog = new ViewDialog(this);
        String nombre = cred.getData("full_name");
        String email = cred.getData("email");
        String telefono = cred.getData("telefono");
        tv_nombre.setText(nombre);
        tv_email.setText(email);
        metodos_pago.setOnClickListener(this);
        change_password.setOnClickListener(this);
        change_phone.setOnClickListener(this);
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
        InformationActivity.this.finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.linear_metodos_pagos:
                startActivity(new Intent(ctx, PaymentActivity.class));
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                InformationActivity.this.finish();
                break;
            case R.id.linear_change_password:
                AlertDialog.Builder builder_password = new AlertDialog.Builder(ctx);
                View custom_view_password = View.inflate(ctx, R.layout.dialog_change_password, null);
                TextView et_pin = custom_view_password.findViewById(R.id.et_pin);
                builder_password.setTitle("¡Hola " + cred.getData("full_name") + "!");
                builder_password.setMessage("Por favor, ingresa tu nuevo PIN");
                builder_password.setView(custom_view_password);
                builder_password.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String str_pin = et_pin.getText().toString();
                        if (str_pin.isEmpty()) {
                            new Utils().createAlert(ctx, "PIN no puede estar vacío", 1);
                            return;
                        }
                        if (str_pin.length() < 4) {
                            new Utils().createAlert(ctx, "PIN debe contener 4 dígitos", 1);
                            return;
                        }
                        dialog.dismiss();
                        udpatePin(str_pin);
                    }
                });
                builder_password.setNeutralButton("Cerrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialogbuilder_password = builder_password.create();
                alertDialogbuilder_password.show();
                break;
            case R.id.linear_change_phone:
                AlertDialog.Builder builder_phone = new AlertDialog.Builder(ctx);
                View custom_view_phone = View.inflate(ctx, R.layout.dialog_change_phone, null);
                TextView et_phone = custom_view_phone.findViewById(R.id.et_phone);
                builder_phone.setTitle("¡Hola " + cred.getData("full_name") + "!");
                builder_phone.setMessage("Por favor, ingresa tu nuevo número de celular");
                builder_phone.setView(custom_view_phone);
                builder_phone.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String str_phone = et_phone.getText().toString();
                        if (str_phone.isEmpty()) {
                            new Utils().createAlert(ctx, "Celular no puede estar vacío", 1);
                            return;
                        }
                        if (str_phone.length() < 9) {
                            new Utils().createAlert(ctx, "Celular debe contener 9 dígitos", 1);
                            return;
                        }
                        dialog.dismiss();
                        udpatePin(str_phone);
                    }
                });
                builder_phone.setNeutralButton("Cerrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog_phone = builder_phone.create();
                alertDialog_phone.show();
                break;
        }
    }

    public void udpatePin(String pin) {
        viewDialog.showDialog();
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.updatePin);
        Log.i("udpatePin_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            RespuestaResponse login = new Gson().fromJson(response, RespuestaResponse.class);
                            if (login.getIde_error() == 0) {
                                viewDialog.hideDialog(0);
                                new Utils().createAlert(ctx, login.getMsg_error(), 2);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        cred.logout();
                                    }
                                }, 1500);
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
                System.out.println("udpatePin_error: " + error.getMessage());
                viewDialog.hideDialog(0);
                new Utils().createAlert(ctx, error.getMessage(), 1);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("pin", pin);
                params.put("telefono", cred.getData("telefono"));
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void udpatePhone(String phone) {
        viewDialog.showDialog();
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.updatePin);
        Log.i("udpatePin_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            RespuestaResponse login = new Gson().fromJson(response, RespuestaResponse.class);
                            if (login.getIde_error() == 0) {
                                viewDialog.hideDialog(0);
                                new Utils().createAlert(ctx, login.getMsg_error(), 2);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        cred.logout();
                                    }
                                }, 1500);
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
                System.out.println("udpatePin_error: " + error.getMessage());
                viewDialog.hideDialog(0);
                new Utils().createAlert(ctx, error.getMessage(), 1);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", cred.getData("user_id"));
                params.put("telefono", phone);
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}