package com.aply.barrioclean.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.aply.barrioclean.R;
import com.aply.barrioclean.activities.CotizaActivity;
import com.aply.barrioclean.utils.Credentials;
import com.aply.barrioclean.utils.Culqi.Card;
import com.aply.barrioclean.utils.Culqi.Token;
import com.aply.barrioclean.utils.Culqi.TokenCallback;
import com.aply.barrioclean.utils.CulqiResponse;
import com.aply.barrioclean.utils.DriverRequest;
import com.aply.barrioclean.utils.Preference;
import com.aply.barrioclean.utils.Utils;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnidadAdapter extends RecyclerView.Adapter<UnidadAdapter.ViewHolder> {

    Credentials cred;
    Context ctx;
    List<JSONObject> l = new ArrayList<>();
    String metros = "0";

    public UnidadAdapter(List<JSONObject> l, String metros) {
        this.l = l;
        this.metros = metros;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ctx = parent.getContext();
        cred = new Credentials(ctx);
        return new ViewHolder(LayoutInflater.from(ctx).inflate(R.layout.item_unidad, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        JSONObject obj = l.get(holder.getAdapterPosition());
        Log.e("unidad", obj.toJSONString());
        String unidad = (String) obj.get("unidad");
        String unidad_id = (String) obj.get("unidad_id");
        String precio = (String) obj.get("precio_express");
        String servicio = (String) obj.get("servicio");
        String servicio_id = (String) obj.get("servicio_id");
        String show_btn = obj.get("show_btn") == null ? "0" : (String) obj.get("show_btn");
        String img_unidad = (String) obj.get("photo_url");
        Glide.with(ctx)
                .asDrawable()
                .load(img_unidad)
                .into(holder.img_unidad);
        if (show_btn.equals("0")) {
            holder.btn_choose.setVisibility(View.GONE);
            holder.servicio.setVisibility(View.GONE);
        } else {
            holder.servicio.setText(servicio);
            holder.servicio.setVisibility(View.GONE);
            holder.btn_choose.setVisibility(View.VISIBLE);
            holder.btn_choose.setOnClickListener(v -> {
                DriverRequest driverRequest = new DriverRequest();
                driverRequest.setMetodo_pago(cred.getData(Preference.METODO_PAGO));
                driverRequest.setDireccion(cred.getData(Preference.DIRECCION));
                driverRequest.setLatitud(Double.parseDouble(cred.getData(Preference.USER_LATITUD)));
                driverRequest.setLongitud(Double.parseDouble(cred.getData(Preference.USER_LONGITUD)));
                driverRequest.setUser_id(cred.getData(Preference.USERID));
                driverRequest.setPrecio(precio);
                driverRequest.setServicio(servicio);
                driverRequest.setServicio_id(servicio_id);
                driverRequest.setUnidad(unidad);
                driverRequest.setUnidad_id(unidad_id);
                driverRequest.setStatus(0);
                driverRequest.setDriver_id("0");
                driverRequest.setDriver_name("");
                driverRequest.setDriver_latitud("0");
                driverRequest.setDriver_longitud("0");
                driverRequest.setMetros(UnidadAdapter.this.metros);
                ((CotizaActivity) ctx).saveRequest(driverRequest);
            });
        }
        holder.unidad.setText(unidad);

        holder.precio.setText("S/ " + precio);
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
        TextView unidad, precio, servicio;
        ImageView img_unidad;
        LinearLayout btn_choose;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            img_unidad = itemView.findViewById(R.id.image_unidad);
            unidad = itemView.findViewById(R.id.tv_unidad);
            precio = itemView.findViewById(R.id.tv_precio);
            servicio = itemView.findViewById(R.id.tv_servicio);
            btn_choose = itemView.findViewById(R.id.linear_btn_choose);
        }
    }

    void createChargeCulqi(String monto) {
        Card card = new Card("4111111111111111", "123", 9, 2025, "test@barrioclean.com");
        Token token = new Token(ctx.getResources().getString(R.string.culqi_public));
        token.createToken(ctx, card, new TokenCallback() {

            @Override
            public void onSuccess(org.json.JSONObject token) {
                try {
                    Toast.makeText(ctx, token.get("id").toString(), Toast.LENGTH_LONG).show();
                    Log.e("culqi_token", token.get("id").toString());
                    sendToken(token.get("id").toString(), monto);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onError(Exception error) {
                error.printStackTrace();
            }
        });
    }

    void sendToken(String token, String monto) {
        RequestQueue queue = Volley.newRequestQueue(ctx);
        String url = ctx.getResources().getString(R.string.base_url) + ctx.getResources().getString(R.string.createCharge);
        Log.e("sendToken_url", url);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        CulqiResponse culqiResponse = new Gson().fromJson(response, CulqiResponse.class);
                        System.out.println(culqiResponse);
                        culqiResponse.savePago(ctx);
                        new Utils().createAlert(ctx, (String) culqiResponse.getOutcome().get("merchant_message"), 2);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> System.out.println("findUnidades_error: " + error.getMessage())) {
            @Override
            protected Map<String, String> getParams() {
                int culqi_amount = Integer.parseInt(monto) * 100;
                Map<String, String> params = new HashMap<>();
                params.put("amount", String.valueOf(culqi_amount));
                params.put("email", cred.getData("email"));
                params.put("source_id", token);
                params.put("phone", cred.getData("telefono"));
                return params;
            }
        };

        // Add the request to the RequestQueue.
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }
}
