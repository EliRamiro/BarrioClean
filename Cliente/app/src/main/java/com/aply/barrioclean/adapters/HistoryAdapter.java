package com.aply.barrioclean.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aply.barrioclean.R;
import com.aply.barrioclean.activities.HistoryDetailActivity;
import com.aply.barrioclean.utils.Credentials;
import com.aply.barrioclean.utils.DriverRequest;
import com.aply.barrioclean.utils.Utils;
import com.google.gson.Gson;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    Credentials cred;
    Context ctx;
    List<JSONObject> l = new ArrayList<>();

    public HistoryAdapter(List<JSONObject> l) {
        this.l = l;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ctx = parent.getContext();
        cred = new Credentials(ctx);
        return new ViewHolder(LayoutInflater.from(ctx).inflate(R.layout.item_history, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        JSONObject json = l.get(holder.getAdapterPosition());
        DriverRequest driverRequest = new Gson().fromJson(json.toJSONString(), DriverRequest.class);
        String cliente = (String) json.get("user_name");
        if (driverRequest.getProfile_photo() != null && !driverRequest.getProfile_photo().isEmpty()) {
            Bitmap bmp = new Utils().convertToImage(driverRequest.getProfile_photo());
            holder.image.setImageBitmap(bmp);
        }
        holder.cliente.setText(cliente);
        holder.direccion.setText(driverRequest.getDireccion());
        holder.servicio.setText(driverRequest.getServicio());
        holder.btn_details.setOnClickListener(v -> {
            ctx.startActivity(new Intent(ctx, HistoryDetailActivity.class).putExtra("obj", l.get(position).toString()));
        });
        switch (driverRequest.getStatus()) {
            case 1:
                holder.btn_details.setText("En ruta");
                break;
            case 2:
                holder.btn_details.setText("Esperando al cliente");
                break;
            case 3:
                holder.btn_details.setText("Cargando...");
                break;
            case 4:
                holder.btn_details.setText("Finalizado");
                break;
            default:
                holder.btn_details.setText("Cancelado");
                break;
        }
    }

    @Override
    public int getItemCount() {
        return l.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView cliente, direccion, servicio;
        Button btn_details;
        ImageView image;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cliente = itemView.findViewById(R.id.cliente);
            direccion = itemView.findViewById(R.id.direccion);
            servicio = itemView.findViewById(R.id.servicio);
            btn_details = itemView.findViewById(R.id.btn_view_details);
            image = itemView.findViewById(R.id.image);
        }
    }
}
