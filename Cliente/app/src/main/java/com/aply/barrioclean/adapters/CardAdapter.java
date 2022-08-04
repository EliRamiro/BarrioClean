package com.aply.barrioclean.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aply.barrioclean.R;
import com.aply.barrioclean.activities.PaymentActivity;
import com.aply.barrioclean.utils.Credentials;
import com.aply.barrioclean.utils.Preference;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    Credentials cred;
    Context ctx;
    List<JSONObject> l = new ArrayList<>();

    public CardAdapter(List<JSONObject> l) {
        this.l = l;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ctx = parent.getContext();
        cred = new Credentials(ctx);
        return new ViewHolder(LayoutInflater.from(ctx).inflate(R.layout.item_card, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        JSONObject obj = l.get(holder.getAdapterPosition());
        String card_number = (String) obj.get("card_number");
        holder.card_number.setText(card_number);
        holder.linear_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = (String) l.get(holder.getAdapterPosition()).get("id");
                cred.saveData(Preference.DEFAULT_PAYMENT, l.get(holder.getAdapterPosition()).toJSONString());
                String terminacion = (String) l.get(holder.getAdapterPosition()).get("terminacion");
                ((PaymentActivity) ctx).showDefaultCard(terminacion);
            }
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
        LinearLayout linear_card;
        TextView card_number;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card_number = itemView.findViewById(R.id.card_number);
            linear_card = itemView.findViewById(R.id.linear_card);
        }
    }
}
