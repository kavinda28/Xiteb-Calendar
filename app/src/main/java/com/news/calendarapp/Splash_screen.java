package com.news.calendarapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ActionMode;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class Splash_screen extends AppCompatActivity {
    TextView welcome, brandName;
    ImageView brand;
    private static int Splash_timeout = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //full screen window open

        welcome = findViewById(R.id.welcome);
        brandName = findViewById(R.id.brandName);
        brand = findViewById(R.id.brand_logo);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent splashintent = new Intent(Splash_screen.this, MainActivity.class);
                startActivity(splashintent);
                finish();

            }
        }, Splash_timeout);
        Animation myAnimation1 = AnimationUtils.loadAnimation(Splash_screen.this, R.anim.animation2);
        welcome.startAnimation(myAnimation1);

        Animation myAnimation2 = AnimationUtils.loadAnimation(Splash_screen.this, R.anim.animation1);
        brandName.startAnimation(myAnimation2);

        Animation myAnimation3 = AnimationUtils.loadAnimation(Splash_screen.this, R.anim.animation1);
        brand.startAnimation(myAnimation3);


    }
}
