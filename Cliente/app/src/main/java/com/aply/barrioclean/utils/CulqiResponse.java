package com.aply.barrioclean.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.aply.barrioclean.R;

import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CulqiResponse {
    int current_amount;
    JSONObject source;
    int fraud_score;
    String reference_code;
    String authorization_code;
    int total_fee;
    int transfer_amount;
    long capture_date;
    JSONObject outcome;
    JSONObject client;

    public CulqiResponse() {
    }

    public String getCurrent_amount() {
        return String.valueOf(current_amount);
    }

    public void setCurrent_amount(int current_amount) {
        this.current_amount = current_amount;
    }

    public JSONObject getSource() {
        return source;
    }

    public void setSource(JSONObject source) {
        this.source = source;
    }

    public String getFraud_score() {
        return String.valueOf(fraud_score);
    }

    public void setFraud_score(int fraud_score) {
        this.fraud_score = fraud_score;
    }

    public String getReference_code() {
        return reference_code;
    }

    public void setReference_code(String reference_code) {
        this.reference_code = reference_code;
    }

    public String getAuthorization_code() {
        return authorization_code;
    }

    public void setAuthorization_code(String authorization_code) {
        this.authorization_code = authorization_code;
    }

    public String getTotal_fee() {
        return String.valueOf(total_fee);
    }

    public void setTotal_fee(int total_fee) {
        this.total_fee = total_fee;
    }

    public String getTransfer_amount() {
        return String.valueOf(transfer_amount);
    }

    public void setTransfer_amount(int transfer_amount) {
        this.transfer_amount = transfer_amount;
    }

    public String getCapture_date() {
        return String.valueOf(capture_date);
    }

    public void setCapture_date(int capture_date) {
        this.capture_date = capture_date;
    }

    public JSONObject getOutcome() {
        return outcome;
    }

    public void setOutcome(JSONObject outcome) {
        this.outcome = outcome;
    }

    public JSONObject getClient() {
        return client;
    }

    public void setClient(JSONObject client) {
        this.client = client;
    }

    public void savePago(Context ctx) {
        Credentials cred = new Credentials(ctx);
        RequestQueue queue = Volley.newRequestQueue(ctx);
        String url = ctx.getResources().getString(R.string.base_url) + ctx.getResources().getString(R.string.savePago);
        Log.e("savePago_url", url);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            System.out.println("savePago: " + response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("savePago_error: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", cred.getData("user_id"));
                params.put("current_amount", getCurrent_amount());
                params.put("fraud_score", getFraud_score());
                params.put("reference_code", getReference_code());
                params.put("authorization_code", getAuthorization_code());
                params.put("total_fee", getTotal_fee());
                params.put("transfer_amount", getTransfer_amount());
                params.put("capture_date", getCapture_date());
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
