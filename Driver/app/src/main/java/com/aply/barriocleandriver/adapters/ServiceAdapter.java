package com.aply.barriocleandriver.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aply.barriocleandriver.R;
import com.aply.barriocleandriver.activities.DetailActivity;
import com.aply.barriocleandriver.activities.ServiceDetailActivity;
import com.aply.barriocleandriver.models.Driver;
import com.aply.barriocleandriver.utils.Credentials;
import com.aply.barriocleandriver.utils.Utils;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {
    Credentials cred;
    Context ctx;
    List<JSONObject> l = new ArrayList<>();
    AlertDialog alertDialog;

    public ServiceAdapter(List<JSONObject> l) {
        this.l = l;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ctx = parent.getContext();
        cred = new Credentials(ctx);
        return new ViewHolder(LayoutInflater.from(ctx).inflate(R.layout.item_service, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JSONObject json = l.get(position);
        Driver driver = new Gson().fromJson(json.toJSONString(), Driver.class);
        if (driver.getProfile_photo() != null && !driver.getProfile_photo().isEmpty()) {
            Bitmap bmp = new Utils().decodeImage(driver.getProfile_photo());
            holder.image.setImageBitmap(bmp);
        }

        holder.user_points.setText(driver.getUser_points());
        holder.cliente.setText(driver.getCliente());
        holder.direccion.setText(driver.getDireccion());
        holder.servicio.setText(driver.getServicio());
        holder.btn_aceptar.setOnClickListener(v -> Toast.makeText(ctx, "Aceptado", Toast.LENGTH_SHORT).show());
        holder.btn_details.setOnClickListener(v -> {
            try{
                ctx.startActivity(new Intent(ctx, ServiceDetailActivity.class).putExtra("id", driver.getId()));
            }catch (Exception e) {
                e.printStackTrace();
            }

                }
        );
        switch (driver.getStatus()) {
            case 1:
                holder.btn_details.setText("En ruta");
                break;
            case 2:
                holder.btn_details.setText("Esperando al cliente");
                break;
            case 3:
                holder.btn_details.setText("Cargando...");
                break;
        }

        if (driver.getProfile_photo() != null && !driver.getProfile_photo().isEmpty()) {
            Bitmap bmp = new Utils().convertToImage(driver.getProfile_photo());
            Glide.with(ctx)
                    .load(bmp)
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(holder.image);
        } else {
            Glide.with(ctx)
                    .load(R.drawable.logo)
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(holder.image);
        }
    }

    @Override
    public int getItemCount() {
        return l.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView cliente, direccion, servicio, user_points;
        Button btn_aceptar, btn_details;
        ImageView image;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cliente = itemView.findViewById(R.id.cliente);
            direccion = itemView.findViewById(R.id.direccion);
            servicio = itemView.findViewById(R.id.servicio);
            btn_aceptar = itemView.findViewById(R.id.btn_accept);
            btn_details = itemView.findViewById(R.id.btn_view_details);
            user_points = itemView.findViewById(R.id.user_points);
            image = itemView.findViewById(R.id.image);
        }
    }
}
