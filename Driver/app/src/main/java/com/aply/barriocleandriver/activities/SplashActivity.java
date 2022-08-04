package com.aply.barriocleandriver.activities;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.aply.barriocleandriver.R;
import com.aply.barriocleandriver.utils.Credentials;
import com.aply.barriocleandriver.utils.GoogleService;
import com.aply.barriocleandriver.utils.Preference;
import com.aply.barriocleandriver.utils.RespuestaResponse;
import com.aply.barriocleandriver.utils.Utils;
import com.google.gson.Gson;

import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {
    Context ctx;
    Credentials cred;
    Utils utils = new Utils();
    int granted = 0;

    String[] permissions = new String[]{WRITE_EXTERNAL_STORAGE, READ_PHONE_STATE, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, CAMERA, CALL_PHONE};
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double latitud = Double.valueOf(intent.getStringExtra(Preference.LATITUD));
            double longitud = Double.valueOf(intent.getStringExtra(Preference.LONGITUD));
            try {
                new Utils(ctx).updateLocation();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        ctx = this;
        cred = new Credentials(ctx);
        utils = new Utils(ctx);
        utils.initializeFirebase(ctx);
        utils.getConfigs(ctx);
        //new Handler().postDelayed(() -> requestPermission(), 1500);
        requestPermissions(permissions, 100);
    }

    public void getUserSettings() {
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.getUsuarioSettings);
        Log.i("getUserSettings_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        RespuestaResponse settings = new Gson().fromJson(response, RespuestaResponse.class);
                        if (settings.getIde_error() == 0) {
                            JSONObject obj = (JSONObject) settings.getRespuesta().get(0);
                            String is_available = (String) obj.get("is_available");
                            String is_validate = (String) obj.get("is_validate");
                            String is_enable = (String) obj.get("status");
                            String carga_disponible = (String) obj.get("carga_disponible");
                            String ganancia = (String) obj.get("ganancia");
                            String imgSoat = (String) obj.get("img_soat");
                            String imgBrevete = (String) obj.get("img_brevete");
                            String imgTarjeta = (String) obj.get("img_tarjeta_propiedad");
                            cred.saveData(Preference.ENABLE, is_enable);
                            cred.saveData(Preference.VALIDATE, is_validate);
                            cred.saveData(Preference.AVAILABLE, is_available);
                            cred.saveData(Preference.CARGA_DISPONIBLE, carga_disponible);
                            cred.saveData(Preference.GANANCIA, "S/ " + ganancia);
                            cred.saveData(Preference.SOAT, imgSoat);
                            cred.saveData(Preference.BREVETE, imgBrevete);
                            cred.saveData(Preference.TARJETA_PROPIEDAD, imgTarjeta);
                        } else {
                            cred.saveData(Preference.ENABLE, "0");
                            cred.saveData(Preference.VALIDATE, "0");
                            cred.saveData(Preference.AVAILABLE, "0");
                            cred.saveData(Preference.GANANCIA, "S/ 0");
                            cred.saveData(Preference.CARGA_DISPONIBLE, "0 m3");
                            cred.saveData(Preference.SOAT, "");
                            cred.saveData(Preference.BREVETE, "");
                            cred.saveData(Preference.TARJETA_PROPIEDAD, "");
                        }
                        if (cred.getData(Preference.LOGIN).equals("1")) {
                            if (cred.getData(Preference.ENABLE).equals("0")) {
                                AlertDialog alertDialog;
                                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                                builder.setTitle("Usuario Deshabilitado");
                                builder.setMessage("Su usuario se encuentra deshabilitado, por favor contacte con el proveedor");
                                alertDialog = builder.create();
                                alertDialog.show();
                                new Handler().postDelayed(() -> {
                                    alertDialog.dismiss();
                                    startActivity(new Intent(ctx, LoginActivity.class));
                                    cred.saveData(Preference.LOGIN, "0");
                                    SplashActivity.this.finish();
                                }, 1000);
                            } else {
                                startActivity(new Intent(ctx, PrincipalActivity.class));
                                SplashActivity.this.finish();
                            }
                        } else {
                            startActivity(new Intent(ctx, LoginActivity.class));
                            SplashActivity.this.finish();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        new Utils().createAlert(ctx, e.getMessage(), 1);
                    }
                }, error -> System.out.println("getUserSettings_error: " + error.getMessage())) {
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

    @Override
    protected void onResume() {
        super.onResume();
        if (cred.getData(Preference.LOCATION_SERVICE).equals("0") || cred.getData(Preference.LOCATION_SERVICE).isEmpty()) {
            registerReceiver(broadcastReceiver, new IntentFilter(GoogleService.str_receiver));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        cred.saveData(Preference.LOCATION_SERVICE, "0");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length != permissions.length) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            } else {
                getUserSettings();
            }
        }
    }
}
