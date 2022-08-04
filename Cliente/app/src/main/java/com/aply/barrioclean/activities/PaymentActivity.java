package com.aply.barrioclean.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.aply.barrioclean.R;
import com.aply.barrioclean.adapters.CardAdapter;
import com.aply.barrioclean.utils.Credentials;
import com.aply.barrioclean.utils.Preference;
import com.aply.barrioclean.utils.RespuestaResponse;
import com.aply.barrioclean.utils.Utils;
import com.aply.barrioclean.utils.ViewDialog;
import com.google.gson.Gson;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PaymentActivity extends AppCompatActivity implements View.OnClickListener {

    Context ctx;
    Credentials cred;
    ViewDialog viewDialog;

    @BindView(R.id.linear_add_card)
    LinearLayout linear_add_card;

    CardAdapter cardAdapter;
    List<JSONObject> l = new ArrayList<>();
    @BindView(R.id.recycler_tarjetas)
    RecyclerView recycler_tarjetas;
    @BindView(R.id.tv_default_card)
    TextView tv_default_card;
    @BindView(R.id.linear_efectivo)
    LinearLayout linear_efectivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Métodos de Pago");
        }
        ctx = this;
        cred = new Credentials(ctx);
        viewDialog = new ViewDialog(this);
        linear_add_card.setOnClickListener(this);
        linear_efectivo.setOnClickListener(this);

        recycler_tarjetas = findViewById(R.id.recycler_tarjetas);
        recycler_tarjetas.setLayoutManager(new LinearLayoutManager(ctx));
        cardAdapter = new CardAdapter(l);
        recycler_tarjetas.setAdapter(cardAdapter);
        getCards();

        try {
            JSONObject card = (JSONObject) new JSONParser().parse(cred.getData(Preference.DEFAULT_PAYMENT));
            if (card != null) {
                String terminacion = (String) card.get("terminacion");
                showDefaultCard(terminacion);
            } else {
                showDefaultCard("Efectivo");
            }
        } catch (Exception e) {
            showDefaultCard("Efectivo");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ctx, ProfileActivity.class));
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        PaymentActivity.this.finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.linear_add_card:
                showModalAddCard();
                break;
            case R.id.linear_efectivo:
                cred.saveData(Preference.DEFAULT_PAYMENT, "");
                showDefaultCard("Efectivo");
                break;
        }
    }

    void showModalAddCard() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        View view = View.inflate(ctx, R.layout.dialog_add_card, null);
        builder.setView(view);
        EditText card_number = view.findViewById(R.id.card_number);
        EditText card_month = view.findViewById(R.id.card_month);
        EditText card_year = view.findViewById(R.id.card_year);
        EditText card_ccv = view.findViewById(R.id.card_ccv);
        card_number.addTextChangedListener(new FourDigitCardFormatWatcher());

        builder.setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String str_card_number = card_number.getText().toString();
                String str_card_month = card_month.getText().toString();
                String str_card_year = card_year.getText().toString();
                String str_card_ccv = card_ccv.getText().toString();
                addCard(str_card_number, str_card_month, str_card_year, str_card_ccv);
            }
        });
        builder.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static class FourDigitCardFormatWatcher implements TextWatcher {

        // Change this to what you want... ' ', '-' etc..
        private static final char space = '-';

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Remove spacing char
            if (s.length() > 0 && (s.length() % 5) == 0) {
                final char c = s.charAt(s.length() - 1);
                if (space == c) {
                    s.delete(s.length() - 1, s.length());
                }
            }
            // Insert char where needed.
            if (s.length() > 0 && (s.length() % 5) == 0) {
                char c = s.charAt(s.length() - 1);
                // Only if its a digit where there should be a space we insert a space
                if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(space)).length <= 3) {
                    s.insert(s.length() - 1, String.valueOf(space));
                }
            }
        }
    }

    public void addCard(String number, String month, String year, String ccv) {
        viewDialog.showDialog();
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.addCard);
        Log.i("addCard_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            RespuestaResponse card = new Gson().fromJson(response, RespuestaResponse.class);
                            int error = card.getIde_error() == 0 ? 2 : 1;
                            viewDialog.hideDialog(0);
                            new Utils().createAlert(ctx, card.getMsg_error(), error);
                        } catch (Exception e) {
                            e.printStackTrace();
                            viewDialog.hideDialog(0);
                            new Utils().createAlert(ctx, e.getMessage(), 1);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("addCard_error: " + error.getMessage());
                viewDialog.hideDialog(0);
                new Utils().createAlert(ctx, error.getMessage(), 1);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", cred.getData("user_id"));
                params.put("card", number);
                params.put("month", month);
                params.put("year", year);
                params.put("ccv", ccv);
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void getCards() {
        viewDialog.showDialog();
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.getCards);
        Log.i("getCardsd_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            RespuestaResponse card = new Gson().fromJson(response, RespuestaResponse.class);
                            int error = card.getIde_error() == 0 ? 2 : 1;
                            JSONArray cards = card.getRespuesta();
                            l.clear();
                            cardAdapter.notifyDataSetChanged();
                            for (Object o : cards) {
                                JSONObject obj = (JSONObject) o;
                                l.add(obj);
                            }
                            cardAdapter.notifyDataSetChanged();
                            viewDialog.hideDialog(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                            viewDialog.hideDialog(0);
                            new Utils().createAlert(ctx, e.getMessage(), 1);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("getCards_error: " + error.getMessage());
                viewDialog.hideDialog(0);
                new Utils().createAlert(ctx, error.getMessage(), 1);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", cred.getData("user_id"));
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void showDefaultCard(String terminacion) {
        if (cred.getData(Preference.DEFAULT_PAYMENT).isEmpty()) {
            tv_default_card.setText(terminacion);
        } else {
            String default_card = "La tarjeta termina en " + terminacion;
            tv_default_card.setText(default_card);
        }

    }
}