package com.aply.barriocleandriver.utils;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.aply.barriocleandriver.BaseApplication;
import com.aply.barriocleandriver.Firebase.MyNotificationManager;
import com.aply.barriocleandriver.R;
import com.aply.barriocleandriver.models.Driver;
import com.desai.vatsal.mydynamictoast.MyDynamicToast;
import com.google.android.gms.location.LocationCallback;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Utils {

    private LocationCallback locationCallback;

    private static final String[] ALPHA_NUMERIC_STRING = {"a", "A", "b", "B", "c", "C", "d", "D", "e", "E", "f", "F", "g", "G", "h", "H", "i", "I", "j", "J", "k", "K", "l", "L", "m", "M", "n", "N", "o", "O", "p", "P", "q", "Q", "r", "R", "s", "S", "t", "T", "u", "U", "v", "V", "w", "W", "x", "X", "y", "Y", "z", "Z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    //Spanish, PeruLocale locale = new Locale("es", "pe"); //Spanish, Peru
    Locale locale = new Locale("es", "pe");
    private RequestQueue queue;
    private final String url = "";
    private Context ctx;
    private Credentials cred;
    private final int zeroComplete = 6;
    String currentPhotoPath;
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
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
        View dialog_view = View.inflate(ctx, R.layout.dialog_alert, null);
        TextView txt_title = dialog_view.findViewById(R.id.et_title);
        TextView txt_msg = dialog_view.findViewById(R.id.et_message);


        String title = "";
        if (type == 1)
            title = "¡Error!";
        else if (type == 2)
            title = "¡Éxito!";
        else if (type == 3)
            title = "¡Alerta!";

        txt_title.setText(title);
        txt_msg.setText(error);
        dialog.setContentView(dialog_view);
        dialog.show();

        new Handler().postDelayed(() -> dialog.dismiss(), 1500);
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
            queue = Volley.newRequestQueue(ctx);

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

    public String toEngDate(String esp_date) {
        String eng_date = "";
        String[] ar_fec_nac = esp_date.split("/");
        eng_date = ar_fec_nac[2] + "-" + ar_fec_nac[1] + "-" + ar_fec_nac[0];
        return eng_date;
    }

    public static boolean internetChack() {
        ConnectivityManager ConnectionManager = (ConnectivityManager) BaseApplication.ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = ConnectionManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected() == true;
    }

    public void finishActivity(final Activity act) {
        new Handler().postDelayed(() -> {
            act.finish();
            act.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        }, 1500);
    }

    public void setAvailable(Context ctx,String status) {
        cred = new Credentials(ctx);
        String url = ctx.getString(R.string.base_url) + ctx.getString(R.string.updateDriverStatus);
        Log.i("updateDriverStatus", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        RespuestaResponse settings = new Gson().fromJson(response, RespuestaResponse.class);
                        System.out.println("updateAvailable: "+response);
                        if (settings.getIde_error() == 1) {
                            createAlert(ctx,settings.getMsg_error(),1);
                        }else{
                            cred.saveData(Preference.AVAILABLE,status);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        new Utils().createAlert(ctx, e.getMessage(), 1);
                    }
                }, error -> System.out.println("getUserSettings_error: " + error.getMessage())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", cred.getData(Preference.USERID));
                params.put("status", status);
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void getUserDetails(Context ctx) {
        cred = new Credentials(ctx);
        String url = ctx.getString(R.string.base_url) + ctx.getString(R.string.getUsuarioSettings);
        Log.i("getUserSettings_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        RespuestaResponse settings = new Gson().fromJson(response, RespuestaResponse.class);
                        if (settings.getIde_error() == 0) {
                            JSONObject obj = (JSONObject) settings.getRespuesta().get(0);
                            String is_available = (String) obj.get("is_available");
                            String is_validate = (String) obj.get("is_validate");
                            String carga_disponible = (String) obj.get("carga_disponible");
                            String ganancia = (String) obj.get("ganancia");
                            String comision = cred.getData(Preference.COMISION_CONDUCTOR);
                            double d_ganancia = Double.parseDouble(ganancia) * (Double.parseDouble(comision) / 100);
                            cred.saveData(Preference.VALIDATE, is_validate);
                            cred.saveData(Preference.AVAILABLE, is_available);
                            cred.saveData(Preference.CARGA_DISPONIBLE, String.format(Locale.US, "%.2f", Double.parseDouble(carga_disponible)));
                            cred.saveData(Preference.GANANCIA, String.format(Locale.US, "%s %.2f","S/", d_ganancia));
                        } else {
                            cred.saveData(Preference.VALIDATE, "0");
                            cred.saveData(Preference.AVAILABLE, "0");
                            cred.saveData(Preference.CARGA_DISPONIBLE, "0");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        new Utils().createAlert(ctx, e.getMessage(), 1);
                    }
                }, error -> System.out.println("getUserSettings_error: " + error.getMessage())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", cred.getData(Preference.USERID));
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
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
                            String comision_conductor = (String) obj.get("comision_conductor");
                            cred.saveData(Preference.COMISION_CONDUCTOR, comision_conductor);
                        } else {
                            cred.saveData(Preference.COMISION_CONDUCTOR, "0");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        new Utils().createAlert(ctx, e.getMessage(), 1);
                    }
                }, error -> System.out.println("getUserSettings_error: " + error.getMessage()));

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void updateLocation() {
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.updateLocation);
        Log.i("updateLocation_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                },
                error -> {
                    System.out.println("updateLocation_error: " + error.getMessage());
                    new Utils().createAlert(ctx, error.getMessage(), 1);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", cred.getData(Preference.USERID));
                params.put("latitud", cred.getData(Preference.LATITUD));
                params.put("longitud", cred.getData(Preference.LONGITUD));
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

        saveDriverLocation(
                cred.getData(Preference.USERID),
                Double.parseDouble(cred.getData(Preference.LATITUD)),
                Double.parseDouble(cred.getData(Preference.LONGITUD))
        );
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

    public void saveData(Credentials cred, JSONObject obj) {
        cred.saveData(Preference.USERID, (String) obj.get("id"));
        cred.saveData(Preference.LOGIN, "1");
        cred.saveData(Preference.STEP, "1");
        cred.saveData(Preference.VALIDATE, (String) obj.get("is_validate"));
        cred.saveData(Preference.AVAILABLE, (String) obj.get("is_available"));
        cred.saveData(Preference.ENABLE, (String) obj.get("status"));
        cred.saveData(Preference.BREVETE, (String) obj.get("img_brevete"));
        cred.saveData(Preference.SOAT, (String) obj.get("img_soat"));
        cred.saveData(Preference.TARJETA_PROPIEDAD, (String) obj.get("img_tarjeta_propiedad"));
        cred.saveData(Preference.PROFILE_PHOTO, (String) obj.get("profile_photo"));
    }

    public Bitmap decodeImage(String base64) {
        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return bmp;
    }

    public void showLoaderDialog(ViewDialog viewDialog) {
        viewDialog.showDialog();
        new Handler().postDelayed(() -> viewDialog.hideDialog(0), 1500);
    }

    public Bitmap convertToImage(String base64Str) throws IllegalArgumentException {
        byte[] decodedBytes = Base64.decode(base64Str,
                Base64.DEFAULT
        );

        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public void saveDriverLocation(String driver_id, double lat, double lng) {
        Driver driver = new Driver(lat, lng);
        ref.child("drivers").child(driver_id).setValue(driver);
    }

    public void updateStatus(String id, int status) {
        ref.child("services").child(id).child("status").setValue(status);
        ref.child("services").child(id).child("driver_id").setValue(cred.getData(Preference.USERID));
    }
}
