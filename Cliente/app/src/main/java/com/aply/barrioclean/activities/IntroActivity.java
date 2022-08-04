package com.aply.barrioclean.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.aply.barrioclean.R;
import com.aply.barrioclean.utils.Credentials;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IntroActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.carousel_on_boarding)
    CarouselView carousel_on_boarding;
    @BindView(R.id.btn_continuar)
    Button btn_continuar;

    Context ctx;
    Credentials cred;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        ctx = this;
        cred = new Credentials(ctx);
        btn_continuar.setOnClickListener(this);

        carousel_on_boarding.setIndicatorVisibility(View.GONE);
        carousel_on_boarding.setImageListener(viewOnBoardingListener);
        carousel_on_boarding.setPageCount(4);
        carousel_on_boarding.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 3) {
                    btn_continuar.setVisibility(View.VISIBLE);
                } else {
                    btn_continuar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 3) {
                    btn_continuar.setVisibility(View.VISIBLE);
                } else {
                    btn_continuar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.e("state", state + "");
            }
        });
    }

    ImageListener viewOnBoardingListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            switch (position) {
                case 0:
                    imageView.setImageResource(R.drawable.on_boarding_01);
                    break;
                case 1:
                    imageView.setImageResource(R.drawable.on_boarding_02);
                    break;
                case 2:
                    imageView.setImageResource(R.drawable.on_boarding_03);
                    break;
                case 3:
                    imageView.setImageResource(R.drawable.on_boarding_04);
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_continuar:
                cred.saveData("on_boarding", "1");
                String login = cred.getData("login");
                startActivity(new Intent(ctx, LoginActivity.class));
                break;
        }
    }
}