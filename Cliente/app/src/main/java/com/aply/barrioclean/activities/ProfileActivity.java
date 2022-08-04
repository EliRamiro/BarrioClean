package com.aply.barrioclean.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.aply.barrioclean.R;
import com.aply.barrioclean.utils.Credentials;
import com.aply.barrioclean.utils.Image;
import com.aply.barrioclean.utils.Preference;
import com.aply.barrioclean.utils.RespuestaResponse;
import com.aply.barrioclean.utils.Utils;
import com.aply.barrioclean.utils.ViewDialog;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    Context ctx;
    Credentials cred;
    ViewDialog viewDialog;
    String currentPhotoPath;

    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.btn_cerrar)
    ImageView btn_cerrar;
    @BindView(R.id.et_full_name)
    EditText full_name;
    @BindView(R.id.et_email)
    EditText email;
    @BindView(R.id.et_documento)
    EditText documento;
    @BindView(R.id.edit_full_name)
    ImageView edit_full_name;
    @BindView(R.id.edit_email)
    ImageView edit_email;
    @BindView(R.id.edit_documento)
    ImageView edit_documento;
    @BindView(R.id.finish_full_name)
    ImageView finish_full_name;
    @BindView(R.id.finish_email)
    ImageView finish_email;
    @BindView(R.id.finish_documento)
    ImageView finish_documento;
    @BindView(R.id.linear_change_password)
    LinearLayout change_password;
    @BindView(R.id.linear_change_phone)
    LinearLayout change_phone;
    @BindView(R.id.linear_metodos_pagos)
    LinearLayout metodos_pago;

    String str_name = "";
    String str_email = "";
    String str_documento = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ctx = this;
        cred = new Credentials(ctx);
        viewDialog = new ViewDialog(this);
        viewDialog.showDialog();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        }, 500);

        btn_cerrar.setOnClickListener(this);
        edit_full_name.setOnClickListener(this);
        finish_full_name.setOnClickListener(this);
        edit_email.setOnClickListener(this);
        finish_email.setOnClickListener(this);
        edit_documento.setOnClickListener(this);
        finish_documento.setOnClickListener(this);
        image.setOnClickListener(this);
        metodos_pago.setOnClickListener(this);
        change_password.setOnClickListener(this);
        change_phone.setOnClickListener(this);
    }

    void loadData() {
        String str_full_name = cred.getData(Preference.FULLNAME);
        String str_email = cred.getData(Preference.EMAIL);
        String str_documento = cred.getData(Preference.DOCUMENTO);
        full_name.setText(str_full_name);
        email.setText(str_email);
        documento.setText(str_documento);
        if (!cred.getData(Preference.PROFILE_PHOTO).isEmpty()) {
            image.setImageBitmap(Image.convert(cred.getData(Preference.PROFILE_PHOTO)));
        }
        viewDialog.hideDialog(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                galleryAddPic();
                break;
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        try {
            String b64 = currentPhotoPath;
            String profile_photo = Image.convert(BitmapFactory.decodeFile(b64, options));
            cred.saveData(Preference.PROFILE_PHOTO, profile_photo);
            image.setImageBitmap(Image.convert(profile_photo));
            str_name = full_name.getText().toString();
            str_email = email.getText().toString();
            str_documento = documento.getText().toString();
            updateProfile(str_name, str_email, str_documento);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("galleryPic_error: " + e);
        }
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.image:
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                actionTakePhoto(intent);
                break;
            case R.id.edit_full_name:
                full_name.setEnabled(true);
                edit_full_name.setVisibility(View.GONE);
                finish_full_name.setVisibility(View.VISIBLE);
                break;
            case R.id.edit_email:
                email.setEnabled(true);
                edit_email.setVisibility(View.GONE);
                finish_email.setVisibility(View.VISIBLE);
                break;
            case R.id.edit_documento:
                documento.setEnabled(true);
                edit_documento.setVisibility(View.GONE);
                finish_documento.setVisibility(View.VISIBLE);
                break;
            case R.id.finish_full_name:
                full_name.setEnabled(false);
                finish_full_name.setVisibility(View.GONE);
                edit_full_name.setVisibility(View.VISIBLE);
                str_name = full_name.getText().toString();
                str_email = email.getText().toString();
                str_documento = documento.getText().toString();
                updateProfile(str_name, str_email, str_documento);
                break;
            case R.id.finish_email:
                email.setEnabled(false);
                finish_email.setVisibility(View.GONE);
                edit_email.setVisibility(View.VISIBLE);
                str_name = full_name.getText().toString();
                str_email = email.getText().toString();
                str_documento = documento.getText().toString();
                updateProfile(str_name, str_email, str_documento);
                break;
            case R.id.finish_documento:
                documento.setEnabled(false);
                finish_documento.setVisibility(View.GONE);
                edit_documento.setVisibility(View.VISIBLE);
                str_name = full_name.getText().toString();
                str_email = email.getText().toString();
                str_documento = documento.getText().toString();
                updateProfile(str_name, str_email, str_documento);
                break;
            case R.id.btn_cerrar:
                this.finish();
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                startActivity(new Intent(ProfileActivity.this, MenuActivity.class));
                break;
            case R.id.linear_efectivo:
                break;
            case R.id.linear_metodos_pagos:
                startActivity(new Intent(ctx, PaymentActivity.class));
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                ProfileActivity.this.finish();
                break;
            case R.id.linear_change_password:
                AlertDialog.Builder builder_password = new AlertDialog.Builder(ctx);
                View custom_view_password = View.inflate(ctx, R.layout.dialog_change_password, null);
                TextView et_pin = custom_view_password.findViewById(R.id.et_pin);
                builder_password.setTitle("¡Hola " + cred.getData("full_name") + "!");
                builder_password.setMessage("Por favor, ingresa tu nuevo PIN");
                builder_password.setView(custom_view_password);
                builder_password.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String str_pin = et_pin.getText().toString();
                        if (str_pin.isEmpty()) {
                            new Utils().createAlert(ctx, "PIN no puede estar vacío", 1);
                            return;
                        }
                        if (str_pin.length() < 4) {
                            new Utils().createAlert(ctx, "PIN debe contener 4 dígitos", 1);
                            return;
                        }
                        dialog.dismiss();
                        udpatePin(str_pin);
                    }
                });
                builder_password.setNeutralButton("Cerrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialogbuilder_password = builder_password.create();
                alertDialogbuilder_password.show();
                break;
            case R.id.linear_change_phone:
                AlertDialog.Builder builder_phone = new AlertDialog.Builder(ctx);
                View custom_view_phone = View.inflate(ctx, R.layout.dialog_change_phone, null);
                TextView et_phone = custom_view_phone.findViewById(R.id.et_phone);
                builder_phone.setTitle("¡Hola " + cred.getData("full_name") + "!");
                builder_phone.setMessage("Por favor, ingresa tu nuevo número de celular");
                builder_phone.setView(custom_view_phone);
                builder_phone.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String str_phone = et_phone.getText().toString();
                        if (str_phone.isEmpty()) {
                            new Utils().createAlert(ctx, "Celular no puede estar vacío", 1);
                            return;
                        }
                        if (str_phone.length() < 9) {
                            new Utils().createAlert(ctx, "Celular debe contener 9 dígitos", 1);
                            return;
                        }
                        dialog.dismiss();
                        udpatePin(str_phone);
                    }
                });
                builder_phone.setNeutralButton("Cerrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog_phone = builder_phone.create();
                alertDialog_phone.show();
                break;
        }
    }

    void updateProfile(String name, String email, String documento) {
        viewDialog.showDialog();
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.updateProfile);
        Log.i("updateProfile_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            RespuestaResponse updateProfile = new Gson().fromJson(response, RespuestaResponse.class);
                            if (updateProfile.getIde_error() == 0) {
                                viewDialog.hideDialog(0);
                                cred.saveData(Preference.FULLNAME, name);
                                cred.saveData(Preference.EMAIL, email);
                                cred.saveData(Preference.DOCUMENTO, documento);
                                new Utils().showToast(ctx, updateProfile.getMsg_error(), 1);
                                loadData();
                            } else {
                                viewDialog.hideDialog(0);
                                loadData();
                                new Utils().showToast(ctx, updateProfile.getMsg_error(), 2);
                                new Utils().createAlert(ctx, "No se pudo actualizar la información", 1);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            viewDialog.hideDialog(0);
                            new Utils().createAlert(ctx, e.getMessage(), 1);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("updateProfile_error: " + error.getMessage());
                viewDialog.hideDialog(0);
                new Utils().createAlert(ctx, error.getMessage(), 1);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                params.put("documento", documento);
                params.put("foto", cred.getData(Preference.PROFILE_PHOTO));
                params.put("id", cred.getData(Preference.USERID));
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    void actionTakePhoto(Intent in) {
        if (in.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(ctx, "Ocurrió  un error al guardar la foto", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(ctx,
                        getString(R.string.file_provider),
                        photoFile);
                in.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(in, 1);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        currentPhotoPath = "";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                "img_" + cred.getData("user_id") + "_",  /* prefix */
                ".png",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void udpatePin(String pin) {
        viewDialog.showDialog();
        String url = ctx.getApplicationContext().getString(R.string.base_url) + ctx.getApplicationContext().getString(R.string.updatePin);
        Log.i("udpatePin_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            RespuestaResponse login = new Gson().fromJson(response, RespuestaResponse.class);
                            if (login.getIde_error() == 0) {
                                viewDialog.hideDialog(0);
                                new Utils().createAlert(ctx, login.getMsg_error(), 2);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        cred.logout();
                                    }
                                }, 1500);
                            } else {
                                viewDialog.hideDialog(0);
                                new Utils().createAlert(ctx, login.getMsg_error(), 1);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            viewDialog.hideDialog(0);
                            new Utils().createAlert(ctx, e.getMessage(), 1);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("udpatePin_error: " + error.getMessage());
                viewDialog.hideDialog(0);
                new Utils().createAlert(ctx, error.getMessage(), 1);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("pin", pin);
                params.put("telefono", cred.getData("telefono"));
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}