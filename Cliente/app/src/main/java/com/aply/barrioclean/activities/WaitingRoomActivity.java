package com.aply.barrioclean.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.aply.barrioclean.R;
import com.aply.barrioclean.utils.Credentials;
import com.aply.barrioclean.utils.Preference;
import com.aply.barrioclean.utils.RespuestaResponse;
import com.aply.barrioclean.utils.Service;
import com.aply.barrioclean.utils.Utils;
import com.aply.barrioclean.utils.ViewDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WaitingRoomActivity extends AppCompatActivity implements OnMapReadyCallback {
    Context ctx;
    Credentials cred;
    Utils utils;

    @BindView(R.id.txt_searching)
    TextView txt_searching;
    @BindView(R.id.btn_cancelar)
    Button btn_cancelar;

    Animation animBlink;
    BitmapDescriptor icon, icon_truck;
    Service details;
    Dialog prevent;
    ViewDialog viewDialog;

    private GoogleMap mMap;
    SupportMapFragment mapFragment;

    FirebaseDatabase databaseReference = FirebaseDatabase.getInstance("https://barrioclean-c4329-default-rtdb.firebaseio.com/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);
        ButterKnife.bind(this);
        ctx = this;
        cred = new Credentials(ctx);
        utils = new Utils(ctx);
        viewDialog = new ViewDialog(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        animBlink = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.blink);
        txt_searching.startAnimation(animBlink);
        icon = BitmapDescriptorFactory.fromResource(R.drawable.icon_marker);
        icon_truck = BitmapDescriptorFactory.fromResource(R.drawable.icon_truck);
        btn_cancelar.setOnClickListener(v -> showPreventDialog());
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        new Handler().postDelayed(this::init, 1500);
    }

    void init() {
        if (getIntent().getData() == null) {
            if (cred.getData(Preference.SERVICE_DATA).isEmpty()) {
                cred.saveData(Preference.ON_SERVICE, "0");
                startActivity(new Intent(this, MapsActivity.class));
                WaitingRoomActivity.this.finish();
            } else {
                details = new Gson().fromJson(cred.getData(Preference.SERVICE_DATA), Service.class);
                mapFragment.getMapAsync(this);
            }
        } else {
            details = new Gson().fromJson(getIntent().getStringExtra("obj"), Service.class);
            mapFragment.getMapAsync(this);
        }
        serviceListener();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    void showPreventDialog() {
        prevent = new Utils().createPreventDialog(ctx);
        String title = "Cancelar Servicio";
        String body = "¿Está seguro de cancelar el servicio?";
        String aceptar = "Cancelar";
        String cancelar = "Cerrar";
        TextView et_title = prevent.findViewById(R.id.et_title);
        TextView et_message = prevent.findViewById(R.id.et_message);
        Button btn_aceptar = prevent.findViewById(R.id.btn_aceptar);
        Button btn_cancelar = prevent.findViewById(R.id.btn_cancelar);

        et_title.setText(title);
        et_message.setText(body);
        btn_aceptar.setText(aceptar);
        btn_cancelar.setText(cancelar);
        btn_cancelar.setBackground(getDrawable(R.drawable.btn_default_celeste));
        btn_aceptar.setBackground(getDrawable(R.drawable.btn_default_rojo));
        btn_aceptar.setOnClickListener(v -> cancelarServicio());
        prevent.create();
        prevent.show();
    }

    @Override
    public void onBackPressed() {
        showPreventDialog();
    }

    void cancelarServicio() {
        prevent.dismiss();
        viewDialog.showDialog();
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.cancelarSolicitud);
        Log.i("cancelarServicio_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        RespuestaResponse services = new Gson().fromJson(response, RespuestaResponse.class);
                        if (services.getIde_error() == 0) {
                            utils.updateStatus(details.getId(), -1);
                            cred.saveData(Preference.ON_SERVICE, "0");
                            startActivity(new Intent(ctx, MapsActivity.class));
                            WaitingRoomActivity.this.finish();
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
                }, error -> {
            viewDialog.hideDialog(0);
            System.out.println("cancelarServicio_error: " + error.getMessage());
            new Utils().createAlert(ctx, error.getMessage(), 1);
        }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("cancelado_por", "2");
                params.put("id", details.getId());
                return params;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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
        mMap.setMyLocationEnabled(true);
        LatLng start_point = new LatLng(Double.parseDouble(details.getLatitud()), Double.parseDouble(details.getLongitud()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start_point, 14));
        getDriversLocation();
    }

    void getLastRequest() {
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.getLastRequest);
        Log.i("getLastRequest_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        System.out.println("getLasRequestResponse: "+response);
                        RespuestaResponse services = new Gson().fromJson(response, RespuestaResponse.class);
                        details = new Gson().fromJson(services.getRespuesta().get(0).toString(), Service.class);
                        cred.saveData(Preference.SERVICE_DATA, new Gson().toJson(details));
                        int status = Integer.parseInt(details.getStatus());
                        System.out.println("lastRequestStatus: "+status);
                        if (services.getIde_error() == 0) {
                            if (status > 0) {
                                cred.saveData(Preference.ON_SERVICE, "1");
                                startActivity(new Intent(WaitingRoomActivity.this, ServiceMapsActivity.class));
                                WaitingRoomActivity.this.finish();
                            }
                        } else {
                            new Utils().createAlert(ctx, "Ocurrió un error\nPor favor espere un momento", 1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        new Utils().createAlert(ctx, e.getMessage(), 1);
                    }
                }, error -> {
            System.out.println("getLastRequest_error: " + error.getMessage());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    void getDriversLocation() {
        mMap.clear();
        DatabaseReference ref = databaseReference.getReference("drivers");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                createMarker(snapshot.getValue());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                createMarker(snapshot.getValue());
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void createMarker(Object snapshot) {
        HashMap<String, Object> driver = (HashMap<String, Object>) snapshot;
        double latitud = (double) driver.get("latitud");
        double longitud = (double) driver.get("longitud");
        details.setDriver_latitud(String.valueOf(latitud));
        details.setDriver_longitud(String.valueOf(longitud));
        cred.saveData(Preference.SERVICE_DATA, new Gson().toJson(details));
        LatLng driver_location = new LatLng(latitud, longitud);
        mMap.addMarker(new MarkerOptions()
                .position(driver_location)
                .title(details.getDriver_name())
                .draggable(false)
                .icon(icon_truck));
    }

    void serviceListener() {
        DatabaseReference ref = databaseReference.getReference("services");
        ref.child(details.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, Object> data = (HashMap<String, Object>) snapshot.getValue();
                if (data != null) {
                    System.out.println("serviceStatus: "+data.get("status"));
                    details.setStatus(data.get("status").toString());
                    getLastRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}

