package com.aply.barriocleandriver.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.aply.barriocleandriver.R;
import com.aply.barriocleandriver.adapters.ServiceAdapter;
import com.aply.barriocleandriver.models.Service;
import com.aply.barriocleandriver.utils.Credentials;
import com.aply.barriocleandriver.utils.Preference;
import com.aply.barriocleandriver.utils.RespuestaResponse;
import com.aply.barriocleandriver.utils.Utils;
import com.aply.barriocleandriver.utils.ViewDialog;
import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class PrincipalActivity extends AppCompatActivity implements View.OnClickListener, LocationListener {

    @BindView(R.id.btn_menu)
    Button btn_menu;
    @BindView(R.id.toggle_btn)
    ToggleButton toggle_btn;
    @BindView(R.id.linear_status)
    LinearLayout linear_status;
    @BindView(R.id.txt_linear_status)
    TextView txt_linear_status;
    @BindView(R.id.linear_documents)
    LinearLayout linear_documents;
    @BindView(R.id.txt_linear_documents)
    TextView txt_linear_documents;
    @BindView(R.id.carga_disponible)
    TextView txt_carga_disponible;
    @BindView(R.id.txt_ganancia)
    TextView txt_ganancia;
    @BindView(R.id.card_active_service)
    CardView card_active_service;
    @BindView(R.id.txt_customer)
    TextView txt_customer;
    @BindView(R.id.txt_address)
    TextView txt_address;
    @BindView(R.id.img_customer)
    CircleImageView img_customer;
    @BindView(R.id.linear_activar)
    LinearLayout linear_activar;
    @BindView(R.id.linear_liberar)
    LinearLayout linear_liberar;
    @BindView(R.id.btn_liberar)
    Button btn_liberar;
    @BindView(R.id.swipePrincipal)
    SwipeRefreshLayout swipePrincipal;

    List<JSONObject> l = new ArrayList<>();
    @BindView(R.id.recycler_services)
    RecyclerView recycler_services;
    ServiceAdapter serviceAdapter;

    Credentials cred;
    Context ctx;
    Utils utils;
    ViewDialog viewDialog;

    LocationManager locationManager;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        ctx = this;
        cred = new Credentials(ctx);
        utils = new Utils(ctx);
        viewDialog = new ViewDialog(this);
        utils.getUserDetails(ctx);
        utils.getConfigs(ctx);
        recycler_services.setLayoutManager(new LinearLayoutManager(ctx));
        serviceAdapter = new ServiceAdapter(l);
        recycler_services.setAdapter(serviceAdapter);
        txt_carga_disponible.setText(cred.getData(Preference.CARGA_DISPONIBLE) + " m3");
        txt_ganancia.setText(cred.getData(Preference.GANANCIA));

        btn_menu.setOnClickListener(this);

        btn_liberar.setOnClickListener(v -> {
            Toast.makeText(ctx, "Liberando...", Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle("Liberar carga");
            builder.setMessage("¿Está seguro de liberar de carga?");
            builder.setCancelable(false);
            AlertDialog alertDialog = builder.create();
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", (dialog, which) -> alertDialog.dismiss());
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Liberar", (dialog, which) -> liberar(ctx));
            alertDialog.show();
        });

        toggle_btn.setOnClickListener(v -> {
            if (!cred.getData(Preference.VALIDATE).equals("1")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setTitle("Documentos Pendientes");
                builder.setMessage("Sus documentos se encuentras pendientes de aprobacion.\nNo podra activar su estado hasta que el administrador apruebe sus documentos");
                builder.setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss());
                builder.show();
            }
        });

        toggle_btn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                setAvailable("1");
                //new Handler().postDelayed(this::validateDriverStatus,1500);
            } else {
                setAvailable("0");
            }
        });

        validateDriverStatus();

        if (cred.getData(Preference.ON_SERVICE).equals("1")) {
            card_active_service.setVisibility(View.VISIBLE);
            Service service = new Gson().fromJson(cred.getData(Preference.SERVICE), Service.class);
            setUpService(service);
            card_active_service.setOnClickListener(v -> {
                startActivity(
                        new Intent(PrincipalActivity.this, ServiceDetailActivity.class)
                                .putExtra("id", cred.getData(Preference.SERVICE_ID))
                );
                overridePendingTransition(0, 0);
            });
        } else {
            card_active_service.setVisibility(View.GONE);
        }


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        swipePrincipal.setOnRefreshListener(this::validateDriverStatus);

        updateAvailable("1");
    }

    void validateDriverStatus() {
        viewDialog.showDialog();
        viewDialog.hideDialog(3000);
        utils.getUserDetails(ctx);
        new Handler().postDelayed(this::validateUserDetails, 3000);
    }

    void validateUserDetails() {
        if (cred.getData(Preference.VALIDATE).equals("1")) {
            Log.e("validate", "1");
            linear_documents.setVisibility(View.GONE);
            swipePrincipal.setRefreshing(false);
            if (cred.getData(Preference.AVAILABLE).equals("1")) {
                setAvailable("1");
                Log.e("validate", "4");
                System.out.println("carga mayor a 0");
                linear_status.setVisibility(View.GONE);
                linear_liberar.setVisibility(View.GONE);
                linear_activar.setVisibility(View.VISIBLE);
                //toggle_btn.setEnabled(true);
                txt_carga_disponible.setText(cred.getData(Preference.CARGA_DISPONIBLE) + " m3");
                txt_ganancia.setText(cred.getData(Preference.GANANCIA));
                viewDialog.hideDialog(0);
                getNewService();
                recycler_services.setVisibility(View.VISIBLE);
                swipePrincipal.setRefreshing(false);
            } else {
                Log.e("validate", "5");
                setAvailable("0");
                linear_status.setVisibility(View.VISIBLE);
                txt_linear_status.setText("No recibirás más notificaciones hasta que cambies tu estado");
                recycler_services.setVisibility(View.INVISIBLE);
                l.clear();
                serviceAdapter.notifyDataSetChanged();
                viewDialog.hideDialog(0);
            }
            viewDialog.hideDialog(0);
        } else {
            Log.e("validate", "6");
            if (cred.getData(Preference.SOAT).isEmpty() || cred.getData(Preference.BREVETE).isEmpty() || cred.getData(Preference.TARJETA_PROPIEDAD).isEmpty()) {
                Log.e("validate", "7");
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setTitle("Documentos Pendientes");
                builder.setMessage("Sus documentos se encuentras pendientes de aprobacion.\nNo podra activar su estado hasta que el administrador apruebe sus documentos");
                builder.setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss());
                builder.show();
                setAvailable("0");
                //toggle_btn.setEnabled(false);
                linear_documents.setVisibility(View.VISIBLE);

                if (cred.getData(Preference.SOAT).isEmpty()) {
                    txt_linear_documents.setText("Debes completar la carga de tu SOAT");
                    txt_linear_documents.setOnClickListener(v -> {
                        startActivity(new Intent(ctx, SoatActivity.class).putExtra("fromMain", true));
                        PrincipalActivity.this.finish();
                    });
                }
                if (cred.getData(Preference.BREVETE).isEmpty()) {
                    txt_linear_documents.setText("Debes completar la carga de tu Brevete");
                    txt_linear_documents.setOnClickListener(v -> {
                        startActivity(new Intent(ctx, BreveteActivity.class).putExtra("fromMain", true));
                        PrincipalActivity.this.finish();
                    });
                }
                if (cred.getData(Preference.TARJETA_PROPIEDAD).isEmpty()) {
                    txt_linear_documents.setText("Debes completar la carga de tu Tarjeta de Propiedad");
                    txt_linear_documents.setOnClickListener(v -> {
                        startActivity(new Intent(ctx, TarjetaPropiedadActivity.class).putExtra("fromMain", true));
                        PrincipalActivity.this.finish();
                    });
                }
            }

            viewDialog.hideDialog(0);
        }
        swipePrincipal.setRefreshing(false);
        viewDialog.hideDialog(0);
    }

    void setUpService(Service service) {
        if (service != null) {
            txt_customer.setText(service.getCliente());
            txt_address.setText(service.getDireccion());
            if (service.getProfile_photo() != null && !service.getProfile_photo().isEmpty()) {
                Bitmap bmp = new Utils().convertToImage(service.getProfile_photo());
                Glide.with(ctx)
                        .load(bmp)
                        .placeholder(R.drawable.logo)
                        .error(R.drawable.logo)
                        .into(img_customer);
            } else {
                Glide.with(ctx)
                        .load(R.drawable.logo)
                        .placeholder(R.drawable.logo)
                        .error(R.drawable.logo)
                        .into(img_customer);
            }
        }

    }

    void updateAvailable(String status) {
        Log.e("setAvailableStatus", status);
        if (status.equals("1")) {
            utils.setAvailable(ctx, "1");
            Log.e("setAvailable", "1");
            toggle_btn.setChecked(true);
            toggle_btn.setBackground(getDrawable(R.drawable.btn_default_gris));
            linear_status.setVisibility(View.GONE);
            cred.saveData(Preference.AVAILABLE, "1");
        } else {
            utils.setAvailable(ctx, "0");
            Log.e("setAvailable", "0");
            toggle_btn.setBackground(getDrawable(R.drawable.btn_default_verde));
            linear_status.setVisibility(View.VISIBLE);
            cred.saveData(Preference.AVAILABLE, "0");
            l.clear();
            serviceAdapter.notifyDataSetChanged();
        }
    }

    void setAvailable(String status) {
        Log.e("setAvailableStatus", status);
        if (status.equals("1")) {
            utils.setAvailable(ctx, "1");
            Log.e("setAvailable", "1");
            toggle_btn.setChecked(true);
            toggle_btn.setBackground(getDrawable(R.drawable.btn_default_gris));
            linear_status.setVisibility(View.GONE);
            cred.saveData(Preference.AVAILABLE, "1");
        } else {
            utils.setAvailable(ctx, "0");
            Log.e("setAvailable", "0");
            toggle_btn.setBackground(getDrawable(R.drawable.btn_default_verde));
            linear_status.setVisibility(View.VISIBLE);
            cred.saveData(Preference.AVAILABLE, "0");
            l.clear();
            serviceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_menu:
                startActivity(new Intent(ctx, MenuActivity.class));
                PrincipalActivity.this.finish();
                break;
        }
    }

    public void getServices() {
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.getServicios);
        Log.i("getServices_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        RespuestaResponse services = new Gson().fromJson(response, RespuestaResponse.class);
                        l.clear();
                        if (services.getIde_error() == 0) {
                            JSONArray obj = services.getRespuesta();
                            for (Object o : obj) {
                                JSONObject json = (JSONObject) o;
                                l.add(json);
                            }
                            viewDialog.hideDialog(0);
                        } else {
                            viewDialog.hideDialog(0);
                        }
                        serviceAdapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        e.printStackTrace();
                        viewDialog.hideDialog(0);
                        new Utils().createAlert(ctx, e.getMessage(), 1);
                    }
                }, error -> {
            System.out.println("getServices_error: " + error.getMessage());
            viewDialog.hideDialog(0);
            if (error.getMessage() != null) {
                new Utils().createAlert(ctx, error.getMessage(), 1);
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        String user_id = cred.getData(Preference.USERID);
        new Utils(ctx).saveDriverLocation(user_id, location.getLatitude(), location.getLongitude());
    }

    void getNewService() {
        FirebaseDatabase databaseReference = FirebaseDatabase.getInstance("https://barrioclean-c4329-default-rtdb.firebaseio.com/");
        DatabaseReference ref = databaseReference.getReference("services");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                getServices();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                getServices();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                getServices();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        viewDialog.hideDialog(0);
    }

    public void liberar(Context ctx) {
        cred = new Credentials(ctx);
        String url = ctx.getString(R.string.base_url) + ctx.getString(R.string.liberar);
        Log.i("getLiberar_url", url);
        Log.i("user_id", cred.getData(Preference.USERID));
        RequestQueue queue = Volley.newRequestQueue(ctx);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        System.out.println("liberar: " + response);
                        RespuestaResponse settings = new Gson().fromJson(response, RespuestaResponse.class);
                        if (settings.getIde_error() == 0) {
                            utils.getUserDetails(ctx);
                            utils.getConfigs(ctx);
                            txt_carga_disponible.setText(cred.getData(Preference.CARGA_DISPONIBLE) + "m3");
                            //validateDriverStatus();
                        } else {
                            utils.createAlert(ctx, settings.getMsg_error(), 1);
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
}