package com.aply.barrioclean.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatRatingBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.aply.barrioclean.R;
import com.aply.barrioclean.utils.Credentials;
import com.aply.barrioclean.utils.Preference;
import com.aply.barrioclean.utils.RespuestaResponse;
import com.aply.barrioclean.utils.Service;
import com.aply.barrioclean.utils.Utils;
import com.aply.barrioclean.utils.ViewDialog;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ServiceMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    Context ctx;
    Credentials cred;
    ViewDialog viewDialog;
    Service details;
    Utils utils;

    private GoogleMap mMap;

    @BindView(R.id.txt_driver_name)
    TextView txt_driver_name;
    @BindView(R.id.txt_service)
    TextView txt_service;
    @BindView(R.id.txt_total)
    TextView txt_total;
    @BindView(R.id.txt_arrive)
    TextView txt_arrive;
    @BindView(R.id.txt_placa)
    TextView txt_placa;
    @BindView(R.id.linear_cancelar)
    LinearLayout linear_cancelar;
    @BindView(R.id.linear_contactar)
    LinearLayout linear_contactar;
    @BindView(R.id.img_driver)
    CircleImageView img_driver;

    BitmapDescriptor icon, icon_truck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_maps);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        ctx = this;
        cred = new Credentials(ctx);
        viewDialog = new ViewDialog(this);
        utils = new Utils();
        icon = BitmapDescriptorFactory.fromResource(R.drawable.icon_marker);
        icon_truck = BitmapDescriptorFactory.fromResource(R.drawable.icon_truck);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        linear_cancelar.setOnClickListener(v -> showPreventDialog());
        System.out.println("serviceMapsActivity: "+cred.getData(Preference.SERVICE_DATA));
        details = new Gson().fromJson(cred.getData(Preference.SERVICE_DATA), Service.class);
        if (details.getProfile_photo() != null && !details.getProfile_photo().isEmpty()) {
            Bitmap bmp = new Utils().convertToImage(details.getProfile_photo());
            Glide.with(ctx)
                    .load(bmp)
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(img_driver);
        } else {
            Glide.with(ctx)
                    .load(R.drawable.logo)
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(img_driver);
        }

        txt_service.setText(details.getServicio());
        txt_driver_name.setText(details.getDriver_name());
        txt_placa.setText(details.getPlaca());
        txt_total.setText("S/ " + details.getPrecio());
        serviceListener();
        driverLocationListener();
        linear_contactar.setOnClickListener(view -> {
            Uri number = Uri.parse("tel:"+details.getDriver_phone());
            Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
            startActivity(callIntent);
        });
    }

    void changeInformation() {
        switch (details.getStatus()) {
            case "1":
                txt_arrive.setText(details.getDriver_name() + " está en camino");
                break;
            case "2":
                txt_arrive.setText(details.getDriver_name() + " ha llegado");
                break;
            case "3":
                txt_arrive.setText(details.getDriver_name() + " está cargando el desmonte");
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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

    void showPaymentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Servicio Finalizado");
        builder.setMessage("Recuerda pagar al conductor el monto de S/ "+details.getPrecio());

        AlertDialog alertDialog = builder.create();
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Aceptar", (dialog, which) -> {
            alertDialog.dismiss();
            showRateDialog();
        });
        alertDialog.show();
    }

    void showRateDialog() {
        Dialog rateDialog = new Dialog(ctx);
        View rateView = View.inflate(ctx,R.layout.dialog_rate, null);
        rateDialog.setContentView(rateView);
        rateDialog.setCancelable(false);
        AppCompatRatingBar rateBar = rateView.findViewById(R.id.ratingBar);
        AppCompatButton btnCalificar = rateView.findViewById(R.id.btn_calificar);
        btnCalificar.setOnClickListener(v->updatePoints(rateBar.getProgress()/2,rateDialog));
        rateDialog.show();
    }

    void updatePoints(int points, Dialog dialog) {
        System.out.println("updatePoints: "+points);
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
                                    details.setDriver_points(String.valueOf(points));
                                    cred.saveData(Preference.ON_SERVICE, "0");
                                    startActivity(new Intent(ctx, MapsActivity.class));
                                    ServiceMapsActivity.this.finish();
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
                    System.out.println("updatePoints_error: " + error.getMessage());
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
        Log.i("cancelarServicio_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        RespuestaResponse services = new Gson().fromJson(response, RespuestaResponse.class);
                        if (services.getIde_error() == 0) {
                            cred.saveData(Preference.ON_SERVICE, "0");
                            cred.saveData(Preference.TXT_LOADER, "");
                            viewDialog.hideDialog(0);
                            utils.updateStatus(details.getId(),-2);
                            startActivity(new Intent(ServiceMapsActivity.this, MapsActivity.class));
                            ServiceMapsActivity.this.finish();
                        } else {
                            viewDialog.hideDialog(0);
                            cred.saveData(Preference.TXT_LOADER, "");
                            utils.createAlert(ctx, services.getMsg_error(), 1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        viewDialog.hideDialog(0);
                        cred.saveData(Preference.TXT_LOADER, "");
                        utils.createAlert(ctx, e.getMessage(), 1);
                    }
                }, error -> {
            System.out.println("cancelarServicio_error: " + error.getMessage());
            viewDialog.hideDialog(0);
            utils.createAlert(ctx, error.getMessage(), 1);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("cancelado_por", "2");
                params.put("id", details == null ? "19" : details.getId());
                return params;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
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
                        details = new Gson().fromJson(services.getRespuesta().get(0).toString(), Service.class);
                        int status = Integer.parseInt(details.getStatus());
                        if (services.getIde_error() == 0) {
                            if (status != 0 && status != -1) {
                                cred.saveData(Preference.SERVICE_DATA, services.getRespuesta().get(0).toString());
                                if (status == 4) {
                                    if(details.getMetodo_pago().equals("Efectivo")) {
                                        showPaymentDialog();
                                    }else{
                                        showRateDialog();
                                    }
                                }
                                changeInformation();
                            } else {//solo cuando está pendiente == 0
                                if (status == -1) {
                                    cred.saveData(Preference.ON_SERVICE, "0");
                                    cred.saveData(Preference.SERVICE_DATA, "");
                                    startActivity(new Intent(ServiceMapsActivity.this, MapsActivity.class));
                                    ServiceMapsActivity.this.finish();
                                }
                            }
                        } else {
                            utils.createAlert(ctx, "Ocurrió un error\nPor favor intentelo nuevamente", 1);
                            new Handler().postDelayed(() -> {
                                cred.saveData(Preference.ON_SERVICE, "0");
                                cred.saveData(Preference.SERVICE_DATA, "");
                                ServiceMapsActivity.this.startActivity(null);
                            }, 5000);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        utils.createAlert(ctx, e.getMessage(), 1);
                    }
                }, error -> {
            System.out.println("getLastRequest_error: " + error.getMessage());
            viewDialog.hideDialog(0);
            utils.createAlert(ctx, error.getMessage(), 1);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", details.getUser_id());
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

    void serviceListener() {
        DatabaseReference ref = utils.getDatabaseReference("services");
        ref.child(details.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getLastRequest();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void driverLocationListener() {
        DatabaseReference ref = utils.getDatabaseReference("drivers");
        ref.child(details.getDriver_id()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, Object> driver = (HashMap<String, Object>) snapshot.getValue();
                if (driver != null) {
                    details.setDriver_latitud(String.valueOf(driver.get("latitud")));
                    details.setDriver_longitud(String.valueOf(driver.get("longitud")));
                    createMarkers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void createMarkers() {
        mMap.clear();

        LatLng recojo = new LatLng(Double.parseDouble(details.getLatitud()), Double.parseDouble(details.getLongitud()));
        mMap.addMarker(new MarkerOptions()
                .position(recojo)
                .title(details.getDireccion())
                .draggable(false)
                .icon(icon));

        LatLng driver_location = new LatLng(Double.parseDouble(details.getDriver_latitud()), Double.parseDouble(details.getDriver_longitud()));
        mMap.addMarker(new MarkerOptions()
                .position(driver_location)
                .title(details.getDriver_name())
                .draggable(false)
                .icon(icon_truck));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(driver_location, 14));
    }
}