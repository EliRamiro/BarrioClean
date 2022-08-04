package com.aply.barriocleandriver.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.aply.barriocleandriver.R;
import com.aply.barriocleandriver.models.Service;
import com.aply.barriocleandriver.utils.Credentials;
import com.aply.barriocleandriver.utils.RespuestaResponse;
import com.aply.barriocleandriver.utils.Utils;
import com.aply.barriocleandriver.utils.ViewDialog;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class HistoryDetailActivity extends AppCompatActivity implements View.OnClickListener {
    Context ctx;
    Credentials cred;

    @BindView(R.id.cliente)
    TextView tv_cliente;
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
    @BindView(R.id.btn_calificar)
    Button btn_calificar;
    @BindView(R.id.rating_bar)
    RatingBar ratingBar;
    @BindView(R.id.img_usuario)
    CircleImageView img_usuario;
    @BindView(R.id.txt_timer)
    TextView txt_timer;
    @BindView(R.id.tv_arrive_time)
    TextView txt_arrive;
    @BindView(R.id.tv_service_time)
    TextView txt_service;

    Service details = new Service();
    ViewDialog viewDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Detalles");
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ctx = this;
        cred = new Credentials(ctx);
        viewDialog = new ViewDialog(this);
        new Utils().showLoaderDialog(viewDialog);
        String obj = getIntent().getStringExtra("obj");
        details = new Gson().fromJson(obj, Service.class);
        tv_cliente.setText(details.getCliente());
        tv_servicio.setText(details.getServicio());
        tv_direccion.setText(details.getDireccion());
        tv_precio.setText(getString(R.string.detail_price, details.getPrecio()));
        txt_timer.setText(details.getTotal_time());
        txt_arrive.setText(details.getArrive_time());
        txt_service.setText(details.getService_time());
        float points = Float.parseFloat(details.getUser_points());
        //ratingBar.setIsIndicator(points == 0);
        ratingBar.setEnabled(points == 0);
        ratingBar.setRating(points);
        btn_calificar.setVisibility((points == 0 ? View.VISIBLE : View.GONE));
        if (details.getProfile_photo() != null) {
            Bitmap bmp = new Utils().decodeImage(details.getProfile_photo());
            img_usuario.setImageBitmap(bmp);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_calificar:
                String points = String.valueOf(ratingBar.getRating());
                updatePoints(points);
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
        startActivity(new Intent(ctx, HistoryActivity.class));
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        HistoryDetailActivity.this.finish();
    }

    void updatePoints(String points) {
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
                            viewDialog.hideDialog(0);
                            ratingBar.setIsIndicator(false);
                            ratingBar.setEnabled(false);
                            details.setDriver_points(points);
                            btn_calificar.setVisibility(View.GONE);
                        } else {
                            viewDialog.hideDialog(0);
                            ratingBar.setIsIndicator(true);
                            ratingBar.setEnabled(true);
                            btn_calificar.setVisibility(View.VISIBLE);
                            new Utils().createAlert(ctx, services.getMsg_error(), 1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        viewDialog.hideDialog(0);
                        ratingBar.setIsIndicator(true);
                        ratingBar.setEnabled(true);
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
                params.put("points", points);
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

}