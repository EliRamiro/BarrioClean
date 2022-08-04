package com.aply.barriocleandriver.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aply.barriocleandriver.R;
import com.aply.barriocleandriver.activities.HistoryDetailActivity;
import com.aply.barriocleandriver.utils.Credentials;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PagosAdapter extends RecyclerView.Adapter<PagosAdapter.ViewHolder> {
    Credentials cred;
    Context ctx;
    List<JSONObject> l = new ArrayList<>();
    AlertDialog alertDialog;

    public PagosAdapter(List<JSONObject> l) {
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
        String cliente = (String) json.get("user_name");
        String direccion = (String) json.get("direccion");
        String servicio = (String) json.get("servicio");
        String status = (String) json.get("status");
        holder.cliente.setText(cliente);
        holder.direccion.setText(direccion);
        holder.servicio.setText(servicio);
        holder.btn_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ctx.startActivity(new Intent(ctx, HistoryDetailActivity.class).putExtra("obj", l.get(position).toString()));
            }
        });
        switch (status) {
            case "1":
                holder.btn_details.setText("En ruta");
                break;
            case "2":
                holder.btn_details.setText("Esperando al cliente");
                break;
            case "3":
                holder.btn_details.setText("Cargando...");
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

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cliente = itemView.findViewById(R.id.cliente);
            direccion = itemView.findViewById(R.id.direccion);
            servicio = itemView.findViewById(R.id.servicio);
            btn_details = itemView.findViewById(R.id.btn_view_details);
        }
    }
}
