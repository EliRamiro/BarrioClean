package com.aply.barriocleandriver.activities;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.aply.barriocleandriver.R;
import com.aply.barriocleandriver.models.Service;
import com.aply.barriocleandriver.utils.Credentials;
import com.aply.barriocleandriver.utils.Preference;
import com.aply.barriocleandriver.utils.RespuestaResponse;
import com.aply.barriocleandriver.utils.Utils;
import com.aply.barriocleandriver.utils.ViewDialog;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ServiceDetailActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, GoogleMap.OnMyLocationChangeListener {
    Context ctx;
    Credentials cred;
    Utils utils;

    @BindView(R.id.cliente)
    TextView tv_cliente;
    @BindView(R.id.img_usuario)
    CircleImageView img_usuario;
    @BindView(R.id.servicio)
    TextView tv_servicio;
    @BindView(R.id.direccion)
    TextView tv_direccion;
    @BindView(R.id.precio)
    TextView tv_precio;
    @BindView(R.id.metros)
    TextView tv_metros;
    @BindView(R.id.distancia)
    TextView tv_distancia;
    @BindView(R.id.btn_accept)
    Button btn_accept;
    @BindView(R.id.btn_accesibilidad)
    Button btn_accesibilidad;
    @BindView(R.id.btn_cancelar)
    Button btn_cancelar;
    @BindView(R.id.img_start_route)
    ImageView img_start_route;
    @BindView(R.id.linear_start_route)
    LinearLayout linear_start_route;
    @BindView(R.id.btn_arrive)
    Button btn_arrive;
    @BindView(R.id.btn_start_service)
    Button btn_start_service;
    @BindView(R.id.btn_end_service)
    Button btn_end_service;
    @BindView(R.id.linear_timer)
    LinearLayout linear_timer;
    @BindView(R.id.chronometer)
    Chronometer chronometer;
    @BindView(R.id.linear_btn_aceptar)
    LinearLayout linear_btn_aceptar;
    @BindView(R.id.linear_btn_arrive)
    LinearLayout linear_btn_arrive;
    @BindView(R.id.linear_btn_start)
    LinearLayout linear_btn_start;
    @BindView(R.id.linear_btn_end)
    LinearLayout linear_btn_end;
    @BindView(R.id.linear_btn_accesibilidad)
    LinearLayout linear_btn_accesibilidad;
    @BindView(R.id.linear_btn_cancelar)
    LinearLayout linear_btn_cancelar;

    HashMap<String, Marker> hashMapMarker = new HashMap<String, Marker>();

    Service details = new Service();
    ViewDialog viewDialog;
    AlertDialog alertDialog;
    long minutes = 0, seconds = 0;
    int timePass = 0;
    FirebaseDatabase databaseReference = FirebaseDatabase.getInstance("https://barrioclean-c4329-default-rtdb.firebaseio.com/");
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Detalles");
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ctx = this;
        cred = new Credentials(ctx);
        utils = new Utils(ctx);
        viewDialog = new ViewDialog(this);
        viewDialog.showDialog();
        String obj = getIntent().getStringExtra("id");
        if(obj == null){
            obj = cred.getData(Preference.SERVICE_ID);
        }
        details.setId(obj);

        ActivityCompat.requestPermissions(this, new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}, 100);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getLastRequest();
        serviceListener();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            btn_accept.setOnClickListener(this);
            btn_cancelar.setOnClickListener(this);
            img_start_route.setOnClickListener(this);
            btn_arrive.setOnClickListener(this);
            btn_start_service.setOnClickListener(this);
            btn_end_service.setOnClickListener(this);
            btn_cancelar.setOnClickListener(v -> showPreventDialog());
            new Handler().postDelayed(() -> viewDialog.hideDialog(0), 1200);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        AlertDialog.Builder builder;
        switch (id) {
            case R.id.btn_accept:
                builder = new AlertDialog.Builder(ctx);
                View custom_view = View.inflate(ctx, R.layout.service_dialog, null);
                builder.setView(custom_view);
                builder.setCancelable(false);
                Button btn_10 = custom_view.findViewById(R.id.btn_10);
                Button btn_20 = custom_view.findViewById(R.id.btn_20);
                Button btn_30 = custom_view.findViewById(R.id.btn_30);
                Button btnCerrar = custom_view.findViewById(R.id.btn_cerrar);
                TextView txt_cancelar = custom_view.findViewById(R.id.txt_cancelar);
                btn_10.setOnClickListener(ServiceDetailActivity.this);
                btn_20.setOnClickListener(ServiceDetailActivity.this);
                btn_30.setOnClickListener(ServiceDetailActivity.this);
                txt_cancelar.setOnClickListener(this);
                btnCerrar.setOnClickListener(v1 -> {
                    alertDialog.dismiss();
                });
                alertDialog = builder.create();
                alertDialog.show();
                break;
            case R.id.btn_10:
                Toast.makeText(ctx, "10 min", Toast.LENGTH_SHORT).show();
                updateTime("10");
                break;
            case R.id.btn_20:
                Toast.makeText(ctx, "20 min", Toast.LENGTH_SHORT).show();
                updateTime("20");
                break;
            case R.id.btn_30:
                Toast.makeText(ctx, "30 min", Toast.LENGTH_SHORT).show();
                updateTime("30");
                break;
            case R.id.btn_arrive:
                details.setStatus("2");
                arrive();
                break;
            case R.id.btn_start_service:
                details.setStatus("3");
                startService();
                break;
            case R.id.btn_end_service:
                details.setStatus("4");
                showPreventEndDialog();
                break;
            case R.id.txt_cancelar:
            case R.id.btn_cancelar:
                builder = new AlertDialog.Builder(ctx);
                builder.setTitle("Cancelar Servicio");
                builder.setMessage("¿Está seguro de cancelar este servicio?");

                alertDialog = builder.create();
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", (dialog, which) -> alertDialog.dismiss());
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancelar", (dialog, which) -> showPreventDialog());
                alertDialog.show();
                break;
            case R.id.img_start_route:
                String uri = String.format(Locale.ENGLISH, "geo:%s,%s", details.getLatitud(), details.getLongitud());
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                ctx.startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ctx, PrincipalActivity.class));
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        ServiceDetailActivity.this.finish();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        cred.saveData(Preference.LATITUD, location.getLatitude() + "");
        cred.saveData(Preference.LONGITUD, location.getLongitude() + "");
        details.setDriver_latitud(location.getLatitude() + "");
        details.setDriver_longitud(location.getLongitude() + "");
        LatLng newLoc = new LatLng(location.getLatitude(), location.getLongitude());
        new Utils(ctx).updateLocation();
    }

    @Override
    public void onMyLocationChange(Location location) {
        cred.saveData(Preference.LATITUD, location.getLatitude() + "");
        cred.saveData(Preference.LONGITUD, location.getLongitude() + "");
        details.setDriver_latitud(location.getLatitude() + "");
        details.setDriver_longitud(location.getLongitude() + "");
        new Utils(ctx).updateLocation();
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
        mMap.setOnMyLocationChangeListener(this);
    }

    void updateTime(String time) {
        alertDialog.dismiss();
        viewDialog.showDialog();
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.aceptarSolicitud);
        Log.i("updateTime_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        System.out.println("updateTime: " + response);
                        RespuestaResponse services = new Gson().fromJson(response, RespuestaResponse.class);
                        if (services.getIde_error() == 0) {
                            details.setStatus("1");
                            showHideButtons(details.getStatus());
                            cred.saveData(Preference.TIME_PASS, "");
                            cred.saveData(Preference.ON_SERVICE, "1");
                            cred.saveData(Preference.SERVICE_ID, details.getId());
                            cred.saveData(Preference.SERVICE, new Gson().toJson(details));
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent().putExtra("obj", cred.getData(Preference.SERVICE)));
                            overridePendingTransition(0, 0);
                            utils.updateStatus(details.getId(), 1);
                        } else {
                            viewDialog.hideDialog(0);
                            cred.saveData(Preference.ON_SERVICE, "0");
                            new Utils().createAlert(ctx, services.getMsg_error(), 1);
                        }
                        viewDialog.hideDialog(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                        viewDialog.hideDialog(0);
                        cred.saveData(Preference.ON_SERVICE, "0");
                        new Utils().createAlert(ctx, e.getMessage(), 1);
                    }
                }, error -> {
            System.out.println("updateTime_error: " + error.getMessage());
            viewDialog.hideDialog(0);
            new Utils().createAlert(ctx, error.getMessage(), 1);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("driver_id", cred.getData(Preference.USERID));
                params.put("id", details.getId());
                params.put("time", time);
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    void showPreventDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Cancelar Servicio");
        builder.setMessage("¿Está seguro de cancelar este servicio?");

        AlertDialog alertDialog = builder.create();
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Regresar", (dialog, which) -> alertDialog.dismiss());
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancelar", (dialog, which) -> cancelarServicio());
        alertDialog.show();
    }

    void showPreventEndDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Finalizar Servicio");
        builder.setMessage("¿Está seguro de finalizar este servicio?");

        AlertDialog alertDialog = builder.create();
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Regresar", (dialog, which) -> alertDialog.dismiss());
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Finalizar", (dialog, which) -> endService());
        alertDialog.show();
    }

    void showRateDialog() {
        Dialog rateDialog = new Dialog(ctx);
        View rateView = View.inflate(ctx, R.layout.dialog_rate, null);
        rateDialog.setContentView(rateView);
        rateDialog.setCancelable(false);
        AppCompatRatingBar rateBar = rateView.findViewById(R.id.ratingBar);
        AppCompatButton btnCalificar = rateView.findViewById(R.id.btn_calificar);
        btnCalificar.setOnClickListener(v -> updatePoints(rateBar.getProgress() / 2, rateDialog));
        rateDialog.show();
    }

    void updatePoints(int points, Dialog dialog) {
        System.out.println("updatePoints: " + points);
        viewDialog.showDialog();
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.updatePoints);
        Log.i("updatePoints_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        RespuestaResponse services = new Gson().fromJson(response, RespuestaResponse.class);
                        if (services.getIde_error() == 0) {
                            dialog.dismiss();
                            viewDialog.hideDialog(3);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    details.setUser_points(String.valueOf(points));
                                    cred.saveData(Preference.TIME_PASS, "");
                                    details.setStatus("4");
                                    utils.updateStatus(details.getId(), 4);
                                    showHideButtons(details.getStatus());
                                }
                            }, 3000);

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
            new Utils().createAlert(ctx, error.getMessage(), 1);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", details.getId());
                params.put("points", String.valueOf(points));
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    void cancelarServicio() {
        viewDialog.showDialog();
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.cancelarSolicitud);
        Log.i("updateTime_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        RespuestaResponse services = new Gson().fromJson(response, RespuestaResponse.class);
                        if (services.getIde_error() == 0) {
                            viewDialog.hideDialog(0);
                            cred.saveData(Preference.ON_SERVICE, "0");
                            startActivity(new Intent(ServiceDetailActivity.this, PrincipalActivity.class));
                            ServiceDetailActivity.this.finish();
                            utils.updateStatus(details.getId(), -1);
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
            System.out.println("updateTime_error: " + error.getMessage());
            viewDialog.hideDialog(0);
            new Utils().createAlert(ctx, error.getMessage(), 1);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("cancelado_por", "1");
                params.put("id", details.getId());
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    void arrive() {
        viewDialog.showDialog();
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.arrive);
        Log.i("arrive_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        RespuestaResponse services = new Gson().fromJson(response, RespuestaResponse.class);
                        if (services.getIde_error() == 0) {
                            utils.updateStatus(details.getId(), 2);
                            viewDialog.hideDialog(0);
                            details.setStatus("2");
                            showHideButtons(details.getStatus());
                            startTimer();
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
            System.out.println("updateTime_error: " + error.getMessage());
            viewDialog.hideDialog(0);
            new Utils().createAlert(ctx, error.getMessage(), 1);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", details.getId());
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    void startService() {
        viewDialog.showDialog();
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.startService);
        Log.i("startService_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        RespuestaResponse services = new Gson().fromJson(response, RespuestaResponse.class);
                        if (services.getIde_error() == 0) {
                            viewDialog.hideDialog(0);
                            details.setStatus("3");
                            utils.updateStatus(details.getId(), 3);
                            showHideButtons(details.getStatus());
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
            System.out.println("updateTime_error: " + error.getMessage());
            viewDialog.hideDialog(0);
            new Utils().createAlert(ctx, error.getMessage(), 1);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", details.getId());
                params.put("user_id", cred.getData(Preference.USERID));
                params.put("status", details.getStatus());
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    void endService() {
        viewDialog.showDialog();
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.endService);
        Log.i("endService_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        RespuestaResponse services = new Gson().fromJson(response, RespuestaResponse.class);
                        if (services.getIde_error() == 0) {
                            showRateDialog();
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
            new Utils().createAlert(ctx, error.getMessage(), 1);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", details.getId());
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    void showHideButtons(String status) {
        cred.saveData(Preference.SERVICE, new Gson().toJson(details));
        switch (status) {
            case "0"://Pendiente
                linear_btn_aceptar.setVisibility(View.VISIBLE);
                linear_btn_accesibilidad.setVisibility(View.GONE);
                linear_btn_end.setVisibility(View.GONE);
                linear_btn_arrive.setVisibility(View.GONE);
                linear_btn_cancelar.setVisibility(View.GONE);
                linear_timer.setVisibility(View.GONE);
                linear_btn_start.setVisibility(View.GONE);
                linear_start_route.setVisibility(View.GONE);
                if (cred.getData(Preference.ON_SERVICE).equals("1")) {
                    btn_accept.setText("Tienes otro servicio en proceso");
                    btn_accept.setEnabled(false);
                }
                break;
            case "1"://Aceptado
                utils.updateStatus(cred.getData(Preference.SERVICE_ID), 1);
                cred.saveData(Preference.TIME_PASS, "");
                linear_btn_aceptar.setVisibility(View.GONE);
                linear_btn_arrive.setVisibility(View.VISIBLE);
                linear_btn_accesibilidad.setVisibility(View.GONE);
                linear_timer.setVisibility(View.GONE);
                linear_btn_start.setVisibility(View.GONE);
                linear_btn_cancelar.setVisibility(View.VISIBLE);
                linear_btn_end.setVisibility(View.GONE);
                linear_start_route.setVisibility(View.VISIBLE);
                break;
            case "2"://Llegado
                utils.updateStatus(cred.getData(Preference.SERVICE_ID), 2);
                cred.saveData(Preference.TIME_PASS, "");
                linear_btn_aceptar.setVisibility(View.GONE);
                linear_btn_arrive.setVisibility(View.GONE);
                linear_btn_accesibilidad.setVisibility(View.GONE);
                linear_btn_end.setVisibility(View.GONE);
                linear_start_route.setVisibility(View.GONE);
                linear_btn_cancelar.setVisibility(View.VISIBLE);
                linear_timer.setVisibility(View.VISIBLE);
                linear_btn_start.setVisibility(View.VISIBLE);
                startTimer();
                break;
            case "3"://Servicio Iniciado
                utils.updateStatus(cred.getData(Preference.SERVICE_ID), 3);
                cred.saveData(Preference.TIME_PASS, "0");
                linear_btn_aceptar.setVisibility(View.GONE);
                linear_btn_arrive.setVisibility(View.GONE);
                linear_start_route.setVisibility(View.GONE);
                linear_timer.setVisibility(View.GONE);
                linear_btn_start.setVisibility(View.GONE);
                linear_btn_end.setVisibility(View.VISIBLE);
                linear_btn_accesibilidad.setVisibility(View.VISIBLE);
                linear_btn_cancelar.setVisibility(View.VISIBLE);
                break;
            case "4"://Finalizado
                utils.updateStatus(cred.getData(Preference.SERVICE_ID), 4);
                cred.saveData(Preference.ON_SERVICE, "0");
                cred.saveData(Preference.TIME_PASS, "");
                startActivity(new Intent(ctx, PrincipalActivity.class));
                this.finish();
                break;
        }
    }

    void startTimer() {
        if (cred.getData(Preference.TIME_PASS).isEmpty())
            chronometer.setBase(SystemClock.elapsedRealtime());

        chronometer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    void serviceListener() {
        DatabaseReference ref = databaseReference.getReference("services");
        ref.child(details.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getLastRequest();
                HashMap<String, Object> data = (HashMap<String, Object>) snapshot.getValue();
                if (data != null) {
                    details.setStatus(data.get("status").toString());
                    showHideButtons(data.get("status").toString());
                    if (details.getStatus().equals("-1") || details.getStatus().equals("-2")) {
                        cred.saveData(Preference.ON_SERVICE, "0");
                        cred.saveData(Preference.SERVICE, "");
                        startActivity(new Intent(ServiceDetailActivity.this, PrincipalActivity.class));
                        ServiceDetailActivity.this.finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void getLastRequest() {
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.getLastRequest);
        Log.i("getLastRequest_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        RespuestaResponse services = new Gson().fromJson(response, RespuestaResponse.class);
                        System.out.println("service: " + services.getRespuesta().get(0).toString());
                        details = new Gson().fromJson(services.getRespuesta().get(0).toString(), Service.class);
                        setUpData();
                        cred.saveData(Preference.SERVICE, new Gson().toJson(details));
                        int status = Integer.parseInt(details.getStatus());
                        if (services.getIde_error() == 0) {
                            Log.e("serviceStatus", status + "");
                            if (status == -1 || status == -2) {
                                cred.saveData(Preference.ON_SERVICE, "0");
                                cred.saveData(Preference.SERVICE, "");
                                startActivity(new Intent(ServiceDetailActivity.this, PrincipalActivity.class));
                                ServiceDetailActivity.this.finish();
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
                params.put("id", details.getId());
                return params;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    void setUpData() {
        if (details.getProfile_photo() != null && !details.getProfile_photo().isEmpty()) {
            Bitmap bmp = new Utils().convertToImage(details.getProfile_photo());
            Glide.with(ctx)
                    .load(bmp)
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(img_usuario);
        } else {
            Glide.with(ctx)
                    .load(R.drawable.logo)
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(img_usuario);
        }
        tv_cliente.setText(details.getCliente());
        tv_servicio.setText(details.getServicio());
        tv_direccion.setText(details.getDireccion());
        tv_precio.setText("S/ " + details.getPrecio());
        tv_metros.setText(details.getMetros() + " m3");
        LatLng userLatLng = new LatLng(Double.parseDouble(details.getLatitud()), Double.parseDouble(details.getLongitud()));
        addMarker(userLatLng, 2);

        drawRoute();

        //showHideButtons(details.getStatus());
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.stop();
        if (details.getStatus().equals("2")) {
            startTimer();
        }
    }

    void drawRoute() {
        //Define list to get all latlng for the route
        List<LatLng> path = new ArrayList();

        //Execute Directions API request
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(getResources().getString(R.string.google_maps_key))
                .build();
        String origin = cred.getData(Preference.LATITUD) + "," + cred.getData(Preference.LONGITUD);
        String destino = details.getLatitud() + "," + details.getLongitud();
        System.out.println("origin: " + origin);
        System.out.println("destino: " + destino);
        getDurationForRoute(origin, destino);
        DirectionsApiRequest req = DirectionsApi.getDirections(context, origin, destino);
        try {
            DirectionsResult res = req.await();

            //Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.length > 0) {
                DirectionsRoute route = res.routes[0];

                if (route.legs != null) {
                    for (int i = 0; i < route.legs.length; i++) {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null) {
                            for (int j = 0; j < leg.steps.length; j++) {
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length > 0) {
                                    for (int k = 0; k < step.steps.length; k++) {
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;
                                        if (points1 != null) {
                                            //Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1) {
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                } else {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null) {
                                        //Decode polyline and add points to list of route coordinates
                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                        for (com.google.maps.model.LatLng coord : coords) {
                                            path.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //Draw the polyline
        if (path.size() > 0) {
            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(5);
            addMarker(opts.getPoints().get(path.size() - 1), 2);
            mMap.addPolyline(opts);

            MarkerOptions markerOptionsTaxi = new MarkerOptions();
            markerOptionsTaxi.draggable(false);
            markerOptionsTaxi.position(opts.getPoints().get(0));
            markerOptionsTaxi.icon(
                    BitmapDescriptorFactory.fromResource(R.drawable.icon_truck)
            );

            hashMapMarker.put("taxi", mMap.addMarker(markerOptionsTaxi));
            if (!cred.getData(Preference.LATITUD).isEmpty() && !cred.getData(Preference.LONGITUD).isEmpty()) {
                LatLng driverLatLng = new LatLng(Double.parseDouble(cred.getData(Preference.LATITUD)), Double.parseDouble(cred.getData(Preference.LONGITUD)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLatLng, 15));
            }
        }
    }

    public void getDurationForRoute(String origin, String destination) {
        try {
            GeoApiContext geoApiContext = new GeoApiContext.Builder()
                    .apiKey(ctx.getResources().getString(R.string.google_maps_key))
                    .build();

            DirectionsResult directionsResult = DirectionsApi.newRequest(geoApiContext)
                    .mode(TravelMode.DRIVING)
                    .origin(origin)
                    .destination(destination)
                    .await();

            if (directionsResult.routes.length > 0) {
                DirectionsRoute route = directionsResult.routes[0];
                DirectionsLeg leg = route.legs[0];
                tv_distancia.setText(leg.distance.toString());
                //solicitud.setDuration(leg.duration.toString());
                //solicitud.setDistance();
            } else {
                tv_distancia.setText("0 km");
                //solicitud.setDuration("0 min");
                //solicitud.setDistance("0 km");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addMarker(LatLng geopunto, int punto) {
        mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.draggable(false);
        markerOptions.position(geopunto);
        if (punto == 1) {
            markerOptions.icon(
                    BitmapDescriptorFactory.fromResource(R.drawable.icon_truck)
            )
                    .flat(true);
        } else {
            markerOptions
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .alpha(0.7f);
        }

        hashMapMarker.put("geopunto", mMap.addMarker(markerOptions));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(geopunto, 15));
    }
}