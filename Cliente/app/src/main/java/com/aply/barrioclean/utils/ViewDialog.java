package com.aply.barrioclean.utils;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.aply.barrioclean.R;
import com.bumptech.glide.Glide;

public class ViewDialog {

    Activity activity;
    Dialog dialog;
    Credentials cred;

    //..we need the context else we can not create the dialog so get context in constructor
    public ViewDialog(Activity activity) {
        this.activity = activity;
        cred = new Credentials(activity);
    }

    public void showDialog() {

        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
        View dialog_view = View.inflate(activity, R.layout.dialog_loader, null);
        TextView txt_loader = dialog_view.findViewById(R.id.txt_loader);
        String str_loader = cred.getData(Preference.TXT_LOADER);
        ImageView img_carga = dialog_view.findViewById(R.id.img_carga);
        if (!str_loader.isEmpty())
            txt_loader.setText(str_loader);
        Glide.with(activity)
                .asGif()
                .load(R.drawable.gif_carga)
                .placeholder(R.drawable.gif_carga)
                .into(img_carga);
        dialog.setContentView(dialog_view);
        if (dialog != null)
            dialog.show();
    }

    //..also create a method which will hide the dialog when some work is done
    public void hideDialog(@NonNull double timer) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (dialog != null)
                    dialog.dismiss();
            }
        }, Integer.parseInt(String.valueOf(timer * 1000).split("\\.")[0]));
    }

}