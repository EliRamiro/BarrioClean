package com.aply.barrioclean.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.aply.barrioclean.R;
import com.aply.barrioclean.adapters.UnidadAdapter;
import com.aply.barrioclean.utils.Credentials;
import com.aply.barrioclean.utils.DriverRequest;
import com.aply.barrioclean.utils.Preference;
import com.aply.barrioclean.utils.RespuestaResponse;
import com.aply.barrioclean.utils.Utils;
import com.aply.barrioclean.utils.ViewDialog;
import com.google.gson.Gson;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CotizaActivity extends AppCompatActivity implements View.OnClickListener {
    public AlertDialog alertDialog;
    Context ctx;
    Credentials cred;
    ViewDialog viewDialog;
    @BindView(R.id.btn_minus_metros)
    ImageView btn_minus_metros;
    @BindView(R.id.btn_plus_metros)
    ImageView btn_plus_metros;
    @BindView(R.id.tv_metros)
    EditText tv_metros;
    @BindView(R.id.linear_btn_asesoria)
    LinearLayout linear_btn_asesoria;
    @BindView(R.id.spinner_metodos_pagos)
    Spinner spinner_metodos_pagos;
    @BindView(R.id.btn_cotizar)
    LinearLayout btn_cotizar;
    @BindView(R.id.txt_btn_cotizar)
    TextView txt_btn_cotizar;
    @BindView(R.id.img_information)
    ImageView img_information;
    @BindView(R.id.rd_express)
    RadioButton rd_express;
    @BindView(R.id.rd_escala)
    RadioButton rd_escala;
    @BindView(R.id.et_metros)
    TextView et_metros;
    @BindView(R.id.et_sacos)
    TextView et_sacos;
    @BindView(R.id.linear_escala_price)
    LinearLayout linear_escala_price;
    @BindView(R.id.escala_price)
    TextView escala_price;

    String service = "1";
    SpinnerAdapter metodos_pagos_adapter;
    List<JSONObject> cards = new ArrayList<>();
    List<String> metodos = new ArrayList<>();
    List<JSONObject> unidades = new ArrayList<>();
    UnidadAdapter unidadAdapter;
    RecyclerView recyler_unidades;
    String alert_show = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cotiza);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Cotiza");
        }
        ctx = this;
        cred = new Credentials(ctx);
        viewDialog = new ViewDialog(this);

        alert_show = cred.getData("alert_show");
        img_information.setOnClickListener(this);
        btn_minus_metros.setOnClickListener(this);
        btn_plus_metros.setOnClickListener(this);
        rd_escala.setOnClickListener(this);
        rd_express.setOnClickListener(this);
        linear_btn_asesoria.setOnClickListener(this);
        btn_cotizar.setOnClickListener(this);
        getCards();
        spinner_metodos_pagos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e("method_selected", cards.get(position).get("id") + "-" + cards.get(position).get("card_number"));
                cred.saveData("metodo_pago_id", (String) cards.get(position).get("id"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ctx, MapsActivity.class));
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        CotizaActivity.this.finish();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        String new_cant;
        String metro;
        double sacos = 0;
        switch (id) {
            case R.id.img_information:
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setTitle("Conversión de unidades");
                builder.setMessage("1 saco = 35kg\n1 m3 = 700kg");
                builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                builder.show();
                break;
            case R.id.btn_minus_metros:
                new_cant = getCant(-0.05);
                sacos = Math.ceil(Double.parseDouble(new_cant) / 0.05);
                tv_metros.setText(new_cant);
                et_metros.setText(new_cant);
                et_sacos.setText(sacos + "");
                break;
            case R.id.btn_plus_metros:
                new_cant = getCant(0.05);
                tv_metros.setText(new_cant);
                et_metros.setText(new_cant);
                sacos = Math.ceil(Double.parseDouble(new_cant) / 0.05);
                et_sacos.setText(sacos + "");
                break;
            case R.id.rd_express:
                service = "1";
                linear_escala_price.setVisibility(View.GONE);
                break;
            case R.id.rd_escala:
                getCant(0);
                service = "2";
                linear_escala_price.setVisibility(View.VISIBLE);
                txt_btn_cotizar.setText("Solicitar");
                break;
            case R.id.linear_btn_asesoria:
                Intent i2 = new Intent(ctx, AsesoriaActivity.class);
                startActivity(i2);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                CotizaActivity.this.finish();
                break;
            case R.id.btn_cotizar:
                if (service.equals("1"))
                    showModalUnidades();
                else {
                    String price = escala_price.getText().toString().split(" ")[1];
                    DriverRequest driverRequest = new DriverRequest();
                    driverRequest.setMetodo_pago(cred.getData(Preference.METODO_PAGO));
                    driverRequest.setDireccion(cred.getData(Preference.DIRECCION));
                    driverRequest.setLatitud(Double.parseDouble(cred.getData(Preference.USER_LATITUD)));
                    driverRequest.setLongitud(Double.parseDouble(cred.getData(Preference.USER_LONGITUD)));
                    driverRequest.setUser_id(cred.getData(Preference.USERID));
                    driverRequest.setPrecio(price);
                    driverRequest.setServicio("Escala");
                    driverRequest.setServicio_id(service);
                    driverRequest.setUnidad("");
                    driverRequest.setUnidad_id("0");
                    driverRequest.setStatus(0);
                    driverRequest.setDriver_id("0");
                    driverRequest.setDriver_name("");
                    driverRequest.setDriver_latitud("0");
                    driverRequest.setDriver_longitud("0");
                    driverRequest.setMetros(tv_metros.getText().toString());
                    saveRequest(driverRequest);
                }
                break;
        }
    }

    void showModalUnidades() {
        String str_metros = tv_metros.getText().toString();
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Unidades");
        View v = View.inflate(ctx, R.layout.dialog_cotiza_unidades, null);
        builder.setView(v);
        builder.setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss());
        recyler_unidades = v.findViewById(R.id.recycler_cotiza_unidades);
        recyler_unidades.setLayoutManager(new LinearLayoutManager(ctx));

        unidadAdapter = new UnidadAdapter(unidades, str_metros);
        recyler_unidades.setAdapter(unidadAdapter);
        unidades.clear();
        unidadAdapter.notifyDataSetChanged();
        findUnidades(str_metros);
        alertDialog = builder.create();
        alertDialog.show();
    }

    String getCant(double btn) {
        String cantidad = tv_metros.getText().toString();
        String new_cant = "";
        double cant = Double.parseDouble(cantidad);
        if (btn == -0.05) {
            if (cant > 0.2) {
                cant += btn;
            }
        } else {
            cant += btn;
        }
        new_cant = String.format(Locale.US, "%.2f", cant);
        double escalaPrice = Double.parseDouble(cred.getData(Preference.ESCALA_PRICE));
        String str_escala_price = String.format(Locale.US, "S/ %.2f", (escalaPrice * (cant / 0.05)));
        escala_price.setText(str_escala_price);
        return new_cant;
    }

    public void getCards() {
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.getCards);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        RespuestaResponse card = new Gson().fromJson(response, RespuestaResponse.class);
                        int error = card.getIde_error() == 0 ? 2 : 1;
                        JSONArray array_cards = card.getRespuesta();

//                        for (Object o : array_cards) {
//                            JSONObject obj = (JSONObject) o;
//                            String card_number = (String) obj.get("card_number");
//                            cards.add(obj);
//                            metodos.add(card_number);
//                        }
                        JSONObject cash = new JSONObject();
                        cash.put("id", "0");
                        cash.put("is_default", "1");
                        cash.put("terminacion", "");
                        cash.put("card_number", "Efectivo");
                        cards.add(cash);
                        metodos.add("Efectivo");

                        metodos_pagos_adapter = new ArrayAdapter<>(ctx, android.R.layout.simple_list_item_1, metodos);
                        spinner_metodos_pagos.setAdapter(metodos_pagos_adapter);
                    } catch (Exception e) {
                        e.printStackTrace();
                        new Utils().createAlert(ctx, e.getMessage(), 1);
                    }
                }, error -> {
            System.out.println("getCards_error: " + error.getMessage());
            new Utils().createAlert(ctx, error.getMessage(), 1);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", cred.getData("user_id"));
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void findUnidades(String metros) {
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.findUnidades);
        Log.i("findUnidades_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        RespuestaResponse unidades_response = new Gson().fromJson(response, RespuestaResponse.class);
                        unidades.clear();
                        unidadAdapter.notifyDataSetChanged();
                        for (Object o : unidades_response.getRespuesta()) {
                            JSONObject obj = (JSONObject) o;
                            obj.put("show_btn", "1");
                            unidades.add(obj);
                        }
                        unidadAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> System.out.println("findUnidades_error: " + error.getMessage())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("metros", metros);
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void saveRequest(final DriverRequest request) {
        if(alertDialog != null)
            alertDialog.dismiss();
        viewDialog.showDialog();
        RequestQueue queue = Volley.newRequestQueue(ctx);
        String url = ctx.getResources().getString(R.string.base_url) + ctx.getResources().getString(R.string.saveRequest);
        Log.e("saveRequest_url", url);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        RespuestaResponse new_request = new Gson().fromJson(response, RespuestaResponse.class);
                        JSONObject obj = (JSONObject) new_request.getRespuesta().get(0);
                        String message = "¡Espere mientras buscamos un conductor!";
                        new Utils().showToast(ctx, message, 1);
                        viewDialog.hideDialog(0);
                        cred.saveData(Preference.ON_SERVICE, "1");
                        cred.saveData(Preference.SERVICE_DATA, obj.toString());
                        DriverRequest driverRequest = new Gson().fromJson(obj.toString(), DriverRequest.class);
                        new Utils(ctx).saveService(driverRequest);
                        ctx.startActivity(new Intent(ctx, WaitingRoomActivity.class).putExtra("obj", obj.toString()));
                        CotizaActivity.this.finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                        viewDialog.hideDialog(0);
                        new Utils().showToast(ctx, e.getMessage(), 2);
                        cred.saveData(Preference.ON_SERVICE, "0");
                    }
                }, error -> System.out.println("saveRequest_error: " + error.getMessage())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("driverRequest", new Gson().toJson(request));
                return params;
            }
        };

        // Add the request to the RequestQueue.
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }
}