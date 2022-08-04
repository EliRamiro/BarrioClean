package com.aply.barrioclean.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aply.barrioclean.R;
import com.aply.barrioclean.utils.Credentials;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PreguntaAdapter extends RecyclerView.Adapter<PreguntaAdapter.ViewHolder> {

    Credentials cred;
    Context ctx;
    List<JSONObject> l = new ArrayList<>();

    public PreguntaAdapter(List<JSONObject> l) {
        this.l = l;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ctx = parent.getContext();
        cred = new Credentials(ctx);
        return new ViewHolder(LayoutInflater.from(ctx).inflate(R.layout.item_pregunta, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        JSONObject obj = l.get(holder.getAdapterPosition());
        String pregunta = (String) obj.get("pregunta");
        String respuesta = (String) obj.get("respuesta");
        holder.pregunta.setText(pregunta);
        holder.linear_pregunta.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle(pregunta);
            builder.setMessage(respuesta);
            builder.setPositiveButton("Cerrar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        });
    }

    @Override
    public int getItemCount() {
        if (l == null) {
            return 0;
        } else {
            return l.size();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linear_pregunta;
        TextView pregunta;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            pregunta = itemView.findViewById(R.id.tv_pregunta);
            linear_pregunta = itemView.findViewById(R.id.linear_pregunta);
        }
    }
}
