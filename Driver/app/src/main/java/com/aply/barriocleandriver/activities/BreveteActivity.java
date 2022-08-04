package com.aply.barriocleandriver.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.aply.barriocleandriver.R;
import com.aply.barriocleandriver.utils.Credentials;
import com.aply.barriocleandriver.utils.Image;
import com.aply.barriocleandriver.utils.Preference;
import com.aply.barriocleandriver.utils.RespuestaResponse;
import com.aply.barriocleandriver.utils.Utils;
import com.aply.barriocleandriver.utils.ViewDialog;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BreveteActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.btn_cerrar)
    ImageView btn_cerrar;
    @BindView(R.id.img_brevete)
    ImageView img_brevete;
    @BindView(R.id.btn_listo)
    Button btn_listo;

    Context ctx;
    Credentials cred;
    ViewDialog viewDialog;
    String currentPhotoPath;
    boolean fromMain = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brevete);
        ButterKnife.bind(this);
        ctx = this;
        cred = new Credentials(ctx);
        viewDialog = new ViewDialog(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        btn_cerrar.setOnClickListener(this);
        img_brevete.setOnClickListener(this);
        btn_listo.setOnClickListener(this);
        if (!cred.getData(Preference.BREVETE).isEmpty()) {
            img_brevete.setImageBitmap(Image.convert(cred.getData(Preference.BREVETE)));
        }
        if (getIntent() != null) {
            fromMain = getIntent().getBooleanExtra("fromMain", false);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_cerrar:
                this.finish();
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                startActivity(new Intent(BreveteActivity.this, DocumentActivity.class));
                break;
            case R.id.img_brevete:
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                actionTakePhoto(intent);
                break;
            case R.id.btn_listo:
                viewDialog.showDialog();
                updateBrevete();
                break;
        }
    }

    void actionTakePhoto(Intent in) {

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
                    getString(R.string.authority),
                    photoFile);
            in.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(in, 1);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        currentPhotoPath = "";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                "img_" + cred.getData("user_id") + "_",  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        try {
            String b64 = currentPhotoPath;
            String brevete = Image.convert(BitmapFactory.decodeFile(b64, options));
            cred.saveData(Preference.BREVETE, brevete);
            img_brevete.setImageBitmap(Image.convert(brevete));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("galleryPic_error: " + e);
        }
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
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

    void updateBrevete() {
        String url = ctx.getString(R.string.base_url) + ctx.getString(R.string.updateBrevete);
        Log.i("updateBrevete_url", url);
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        RespuestaResponse resend = new Gson().fromJson(response, RespuestaResponse.class);
                        if (resend.getIde_error() == 0) {
                            viewDialog.hideDialog(0);
                            new Utils().createAlert(ctx, resend.getMsg_error(), 2);
                            if (fromMain) {
                                startActivity(new Intent(ctx, PrincipalActivity.class));
                                BreveteActivity.this.finish();
                            } else {
                                new Utils().finishActivity(BreveteActivity.this);
                            }
                        } else {
                            viewDialog.hideDialog(0);
                            new Utils().createAlert(ctx, resend.getMsg_error(), 10);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        viewDialog.hideDialog(0);
                        new Utils().createAlert(ctx, e.getMessage(), 1);
                    }
                }, error -> {
            System.out.println("updateBrevete_error: " + error.getMessage());
            viewDialog.hideDialog(0);
            new Utils().createAlert(ctx, error.getMessage(), 1);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", cred.getData(Preference.USERID));
                params.put("brevete", cred.getData(Preference.BREVETE));
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}