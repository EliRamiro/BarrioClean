package com.aply.barriocleandriver.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.aply.barriocleandriver.R;
import com.aply.barriocleandriver.utils.Credentials;
import com.aply.barriocleandriver.utils.Preference;
import com.aply.barriocleandriver.utils.RespuestaResponse;
import com.aply.barriocleandriver.utils.Utils;
import com.aply.barriocleandriver.utils.ViewDialog;
import com.google.gson.Gson;

import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.chk_term)
    CheckBox chk_term;
    @BindView(R.id.et_telefono)
    AppCompatEditText et_telefono;
    @BindView(R.id.et_email)
    AppCompatEditText et_email;
    @BindView(R.id.et_full_name)
    AppCompatEditText et_full_name;
    @BindView(R.id.btn_continuar)
    Button btn_continuar;
    @BindView(R.id.btn_atras)
    Button btn_atras;
    @BindView(R.id.linear_telefono)
    LinearLayout linear_telefono;
    @BindView(R.id.linear_email)
    LinearLayout linear_email;
    @BindView(R.id.linear_full_name)
    LinearLayout linear_full_name;
    @BindView(R.id.linear_pin)
    LinearLayout linear_pin;
    @BindView(R.id.forgot_password)
    TextView forgot_password;
    @BindView(R.id.et_pin)
    PinEntryEditText et_pin;

    Context ctx;
    Credentials cred;
    ViewDialog viewDialog;
    AlertDialog alertDialog;

    String chk_termns = "", telefono = "", full_name = "", email = "";

    int step = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        ctx = this;
        cred = new Credentials(ctx);
        viewDialog = new ViewDialog(this);
        Log.e("step", cred.getData("step"));
        if (cred.getData("step").isEmpty()) {
            step = 1;
        } else {
            step = Integer.parseInt(cred.getData("step"));
        }

        chk_termns = cred.getData("terminos");
        if (chk_termns.equals("1")) {
            chk_term.setChecked(true);
        }
        switch (step) {
            case 1:
                linear_telefono.setVisibility(View.VISIBLE);
                linear_email.setVisibility(View.GONE);
                linear_full_name.setVisibility(View.GONE);
                linear_pin.setVisibility(View.GONE);
                btn_atras.setVisibility(View.GONE);
                cred.saveData("telefono", et_telefono.getText().toString());
                step = 1;
                break;
            case 2:
                linear_telefono.setVisibility(View.GONE);
                linear_email.setVisibility(View.VISIBLE);
                linear_full_name.setVisibility(View.GONE);
                linear_pin.setVisibility(View.GONE);
                cred.saveData("email", et_email.getText().toString());
                step = 2;
                break;
            case 3:
                linear_telefono.setVisibility(View.GONE);
                linear_email.setVisibility(View.GONE);
                linear_full_name.setVisibility(View.VISIBLE);
                linear_pin.setVisibility(View.GONE);
                cred.saveData("full_name", et_full_name.getText().toString());
                step = 3;
                break;
            case 4:
                linear_telefono.setVisibility(View.GONE);
                linear_email.setVisibility(View.GONE);
                linear_full_name.setVisibility(View.GONE);
                linear_pin.setVisibility(View.VISIBLE);
                step = 4;
                break;
        }
        telefono = cred.getData("telefono");
        if (!telefono.isEmpty()) {
            et_telefono.setText(telefono);
        }

        chk_term.setOnClickListener(v -> {

            if (chk_termns.equals("1"))
                chk_term.setChecked(true);
            else {
                telefono = et_telefono.getText().toString();
                cred.saveData("terminos", "1");
                cred.saveData("telefono", telefono);
//                    startActivity(new Intent(ctx, TermsActivity.class));
            }
        });
        btn_continuar.setOnClickListener(this);
        btn_atras.setOnClickListener(this);
        forgot_password.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_continuar:
                switch (step) {
                    case 1:
                        if (et_telefono.getText().toString().isEmpty()) {
                            new Utils().createAlert(ctx, "Debe ingresar su celular", 1);
                            return;
                        }
                        if (chk_term.isChecked()) {
                            findUsuario(et_telefono.getText().toString());
                        } else {
                            Toast.makeText(ctx, "Debe aceptar los términos y condiciones", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 2:
                        if (et_email.getText().toString().isEmpty()) {
                            new Utils().createAlert(ctx, "Debe ingresar su email", 1);
                            return;
                        }
                        linear_email.setVisibility(View.GONE);
                        linear_full_name.setVisibility(View.VISIBLE);
                        step = 3;
                        cred.saveData(Preference.EMAIL, et_email.getText().toString());
                        break;
                    case 3:
                        if (et_full_name.getText().toString().isEmpty()) {
                            new Utils().createAlert(ctx, "Debe ingresar su nombre", 1);
                            return;
                        }
                        linear_full_name.setVisibility(View.GONE);
                        linear_pin.setVisibility(View.VISIBLE);
                        step = 4;
                        cred.saveData(Preference.FULLNAME, et_full_name.getText().toString());
                        telefono = cred.getData(Preference.TELEFONO);
                        email = cred.getData(Preference.EMAIL);
                        full_name = cred.getData(Preference.FULLNAME);
                        register(telefono, email, full_name);
                        break;
                    case 4:
                        telefono = cred.getData(Preference.TELEFONO);
                        email = cred.getData(Preference.EMAIL);
                        full_name = cred.getData(Preference.FULLNAME);
                        String pin = et_pin.getText().toString();
                        validatePin(telefono, email, pin);
                        break;
                }
                cred.saveData(Preference.STEP, step + "");
                break;
            case R.id.btn_atras:
                switch (step) {
                    case 4:
                        linear_pin.setVisibility(View.GONE);
                        linear_full_name.setVisibility(View.VISIBLE);
                        step = 3;
                        break;
                    case 3:
                        linear_full_name.setVisibility(View.GONE);
                        linear_email.setVisibility(View.VISIBLE);
                        step = 2;
                        cred.saveData(Preference.FULLNAME, et_full_name.getText().toString());
                        break;
                    case 2:
                        linear_email.setVisibility(View.GONE);
                        linear_telefono.setVisibility(View.VISIBLE);
                        btn_atras.setVisibility(View.GONE);
                        step = 1;
                        break;
                }
                cred.saveData(Preference.STEP, step + "");
                break;
            case R.id.forgot_password:
                if (et_telefono.getText().toString().isEmpty()) {
                    new Utils().createAlert(ctx, "Debe ingresar su número de celular", 1);
                } else {
                    cred.saveData(Preference.TELEFONO, et_telefono.getText().toString());
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setTitle("Reenviar PIN");
                    builder.setMessage("¿Cómo deseas recibir tu PIN?");
                    builder.setNeutralButton("Por SMS", (dialog, which) -> {
                        Toast.makeText(ctx, "Enviando SMS", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        resendPin(cred.getData(Preference.TELEFONO), "1");
                    });
                    builder.setPositiveButton("Por Email", (dialog, which) -> {
                        Toast.makeText(ctx, "Enviando Email", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        resendPin(cred.getData(Preference.TELEFONO), "2");
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                break;
        }
    }

    public void register(String phone, String email, String full_name) {
        viewDialog.showDialog();
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.register);
        Log.i("register_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        RespuestaResponse login = new Gson().fromJson(response, RespuestaResponse.class);
                        if (login.getIde_error() == 0) {
                            viewDialog.hideDialog(0);
                            new Utils().createAlert(ctx, login.getMsg_error(), 2);
                        } else {
                            viewDialog.hideDialog(1);
                            new Utils().createAlert(ctx, login.getMsg_error(), 1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        viewDialog.hideDialog(0);
                        new Utils().createAlert(ctx, e.getMessage(), 1);
                    }
                }, error -> {
            System.out.println("register_error: " + error.getMessage());
            viewDialog.hideDialog(0);
            new Utils().createAlert(ctx, error.getMessage(), 1);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("phone", phone);
                params.put("email", email);
                params.put("full_name", full_name);
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void findUsuario(String phone) {
        viewDialog.showDialog();
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.findUsuario);
        Log.i("findUsuario_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        RespuestaResponse findUsuario = new Gson().fromJson(response, RespuestaResponse.class);
                        if (findUsuario.getIde_error() == 0) {
                            JSONObject obj = (JSONObject) findUsuario.getRespuesta().get(0);
                            String id = (String) obj.get("id");
                            String nombre = (String) obj.get("nombre");
                            String email = (String) obj.get("email");
                            String telefono = (String) obj.get("telefono");
                            String codigo_promocional = (String) obj.get("codigo_promocional");
                            et_full_name.setText(nombre);
                            et_email.setText(email);
                            cred.saveData(Preference.USERID, id);
                            cred.saveData(Preference.FULLNAME, nombre);
                            cred.saveData(Preference.EMAIL, email);
                            cred.saveData(Preference.TELEFONO, telefono);
                            cred.saveData(Preference.CODIGOPROMO, codigo_promocional);
                            linear_telefono.setVisibility(View.GONE);
                            linear_pin.setVisibility(View.VISIBLE);
                            step = 4;
                            viewDialog.hideDialog(0);
                        } else {
                            viewDialog.hideDialog(0);
                            linear_telefono.setVisibility(View.GONE);
                            linear_email.setVisibility(View.VISIBLE);
                            step = 2;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        viewDialog.hideDialog(0);
                        new Utils().createAlert(ctx, e.getMessage(), 1);
                    }
                }, error -> {
            System.out.println("findUsuario_error: " + error.getMessage());
            viewDialog.hideDialog(0);
            new Utils().createAlert(ctx, error.getMessage(), 1);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("phone", phone);
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void validatePin(String phone, String email, String pin) {
        viewDialog.showDialog();
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.validatePin);
        Log.i("validatePin_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        RespuestaResponse login = new Gson().fromJson(response, RespuestaResponse.class);
                        if (login.getIde_error() == 0) {
                            JSONObject obj = (JSONObject) login.getRespuesta().get(0);
                            new Utils().saveData(cred, obj);
                            new Utils().getUserDetails(ctx);
                            new Utils().getConfigs(ctx);
                            viewDialog.hideDialog(0);
                            new Utils().showToast(ctx, "Inicio de sesión correcto", 1);
                            startActivity(new Intent(ctx, PrincipalActivity.class));
                            LoginActivity.this.finish();
                        } else {
                            viewDialog.hideDialog(0);
                            new Utils().showToast(ctx, login.getMsg_error(), 2);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        viewDialog.hideDialog(0);
                        new Utils().createAlert(ctx, e.getMessage(), 1);
                    }
                }, error -> {
            System.out.println("validatePin_error: " + error.getMessage());
            viewDialog.hideDialog(0);
            new Utils().createAlert(ctx, error.getMessage(), 1);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("phone", phone);
                params.put("email", email);
                params.put("pin", pin);
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void resendPin(String phone, String metodo) {
        viewDialog.showDialog();
        String url = ctx.getString(R.string.base_url) + ctx.getString(R.string.resendPin);
        Log.i("resendPin_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        RespuestaResponse resend = new Gson().fromJson(response, RespuestaResponse.class);
                        if (resend.getIde_error() == 0) {
                            viewDialog.hideDialog(0);
                            new Utils().createAlert(ctx, resend.getMsg_error(), 2);
                        } else {
                            viewDialog.hideDialog(0);
                            new Utils().createAlert(ctx, resend.getMsg_error(), 1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        viewDialog.hideDialog(0);
                        new Utils().createAlert(ctx, e.getMessage(), 1);
                    }
                }, error -> {
            System.out.println("resendPin_error: " + error.getMessage());
            viewDialog.hideDialog(0);
            new Utils().createAlert(ctx, error.getMessage(), 1);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("phone", phone);
                params.put("email", email);
                params.put("metodo", metodo);
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}