package com.aply.barrioclean.utils;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.aply.barrioclean.Firebase.MyNotificationManager;
import com.aply.barrioclean.R;
import com.desai.vatsal.mydynamictoast.MyDynamicToast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import org.json.simple.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Utils {

    private static final String[] ALPHA_NUMERIC_STRING = {"a", "A", "b", "B", "c", "C", "d", "D", "e", "E", "f", "F", "g", "G", "h", "H", "i", "I", "j", "J", "k", "K", "l", "L", "m", "M", "n", "N", "o", "O", "p", "P", "q", "Q", "r", "R", "s", "S", "t", "T", "u", "U", "v", "V", "w", "W", "x", "X", "y", "Y", "z", "Z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    //Spanish, PeruLocale locale = new Locale("es", "pe"); //Spanish, Peru
    Locale locale = new Locale("es", "ES");
    private RequestQueue queue;
    private final String url = "";
    private Context ctx;
    private Credentials cred;
    private final int zeroComplete = 6;
    FirebaseDatabase databaseReference = FirebaseDatabase.getInstance("https://barrioclean-c4329-default-rtdb.firebaseio.com/");
    DatabaseReference ref = databaseReference.getReference();

    public Utils(Context ctx) {
        this.ctx = ctx;
        cred = new Credentials(ctx);
    }

    public Utils() {
    }

    public void createAlert(Context ctx, String error, int type) {
        Dialog dialog = new Dialog(ctx);
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_alert, null);
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        TextView et_title = dialogView.findViewById(R.id.et_title);
        String title = "";
        if (type == 1)
            title = "¡Error!";
        else if (type == 2)
            title = "¡Éxito!";
        else if (type == 3)
            title = "¡Alerta!";

        TextView et_message = dialogView.findViewById(R.id.et_message);

        et_title.setText(title);
        et_message.setText(error);
        dialog.show();
        new Handler().postDelayed(() -> dialog.dismiss(), 1500);
    }

    public Dialog createPreventDialog(Context ctx) {
        Dialog dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
        View dialogView = View.inflate(ctx, R.layout.dialog_prevent, null);

        Button btn_cancelar = dialogView.findViewById(R.id.btn_cancelar);
        btn_cancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.setContentView(dialogView);
        return dialog;
    }

    public Bitmap convertToImage(String base64Str) throws IllegalArgumentException {
        byte[] decodedBytes = Base64.decode(base64Str,
                Base64.DEFAULT
        );

        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public String convertToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }

    public void initializeFirebase(Context ctx) {
        this.ctx = ctx;
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w("fcm_instance", "getInstanceId failed", task.getException());
                return;
            }
            String token = task.getResult().getToken();
            String msg = token;
            Log.e("fcm_token", msg);
            Utils.this.updateToken(msg);
        });
    }

    public void updateToken(final String token) {
        try {
            String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.updateFcm);
            Log.i("updateToken_url", url);
            queue = Volley.newRequestQueue(this.ctx);

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        try {
                            RespuestaResponse usuario = new Gson().fromJson(response, RespuestaResponse.class);
                            if (usuario.getIde_error() == 0) {
                                Log.i("TokenFcmUpdated", usuario.getMsg_error());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }, error -> System.out.println("updateToken_error: " + error.getMessage())) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("user_id", cred.getData("user_id"));
                    params.put("fcm", token);
                    return params;
                }
            };
            // Add the request to the RequestQueue.
            queue.add(stringRequest);
        } catch (Exception e) {
            Log.e("updateToken_error", Objects.requireNonNull(e.getMessage()));
        }
    }

    public void showNotification(Context ctx, String body, String title) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager =
                    (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel("1", "1", importance);
            mChannel.setDescription("1");
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{0, 500, 250, 500, 250, 500, 250});
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(mChannel);
            MyNotificationManager.getInstance(ctx).displayNotification(body, title);
        } else {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ctx)
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setVibrate(new long[]{0, 500, 250, 500, 250, 500, 250})
                    .setPriority(importance)
                    .setAutoCancel(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            NotificationManager notificationManager =
                    (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0, notificationBuilder.build());
        }
    }

    public void showToast(Context ctx, String message, int type) {
        /*
        1: success
        2: error
        3: warning
        4: information
         */
        switch (type) {
            case 1:
                MyDynamicToast.successMessage(ctx, message);
                break;
            case 2:
                MyDynamicToast.errorMessage(ctx, message);
                break;
            case 3:
                MyDynamicToast.warningMessage(ctx, message);
                break;
            case 4:
                MyDynamicToast.informationMessage(ctx, message);
                break;
        }
    }

    public void getConfigs(Context ctx) {
        cred = new Credentials(ctx);
        String url = ctx.getString(R.string.base_url) + ctx.getString(R.string.getConfigs);
        Log.i("getConfiguraciones_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        RespuestaResponse settings = new Gson().fromJson(response, RespuestaResponse.class);
                        if (settings.getIde_error() == 0) {
                            JSONObject obj = (JSONObject) settings.getRespuesta().get(0);
                            String accesibilidad = (String) obj.get("accesibilidad");
                            String penalidad = (String) obj.get("penalidad");
                            String precioEscala = (String) obj.get("escala_price");
                            cred.saveData(Preference.ACCESIBILIDAD, accesibilidad);
                            cred.saveData(Preference.PENALIDAD, penalidad);
                            cred.saveData(Preference.ESCALA_PRICE, precioEscala);
                        } else {
                            cred.saveData(Preference.ACCESIBILIDAD, "0");
                            cred.saveData(Preference.PENALIDAD, "0");
                            cred.saveData(Preference.ESCALA_PRICE, "0");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        new Utils().createAlert(ctx, e.getMessage(), 1);
                    }
                }, error -> System.out.println("getUserSettings_error: " + error.getMessage()));

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void saveData(Credentials cred, JSONObject obj) {
        String id = (String) obj.get("id");
        String nombre = (String) obj.get("nombre");
        String email = (String) obj.get("email");
        String telefono = (String) obj.get("telefono");
        String codigo_promocional = (String) obj.get("codigo_promocional");
        String profile_photo = (String) obj.get("profile_photo");
        String documento_identidad = (String) obj.get("documento_identidad");
        cred.saveData(Preference.USERID, id);
        cred.saveData(Preference.FULLNAME, nombre);
        cred.saveData(Preference.EMAIL, email);
        cred.saveData(Preference.TELEFONO, telefono);
        cred.saveData(Preference.CODIGOPROMO, codigo_promocional);
        cred.saveData(Preference.PROFILE_PHOTO, profile_photo);
        cred.saveData(Preference.DOCUMENTO, documento_identidad);
    }

    public void showLoaderDialog(ViewDialog viewDialog) {
        viewDialog.showDialog();
        new Handler().postDelayed(() -> viewDialog.hideDialog(0), 1500);
    }

    public void saveService(DriverRequest req) {
        FirebaseDatabase databaseReference = FirebaseDatabase.getInstance("https://barrioclean-c4329-default-rtdb.firebaseio.com/");
        DatabaseReference ref = databaseReference.getReference();
        ref.child("services").child(req.id).setValue(req);

    }

    public void updateStatus(String id, int status) {
        ref.child("services").child(id).child("status").setValue(status);
    }

    public DatabaseReference getDatabaseReference(String database) {
        FirebaseDatabase databaseReference = FirebaseDatabase.getInstance("https://barrioclean-c4329-default-rtdb.firebaseio.com/");
        return databaseReference.getReference(database);
    }
}
