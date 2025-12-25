package com.example.capstone_swastik;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_splash);

        View revealView = findViewById(R.id.revealView);
        ImageView logo = findViewById(R.id.logo);

        revealView.post(() -> startReveal(revealView, logo));
    }

    private void startReveal(View revealView, ImageView logo) {

        int cx = revealView.getWidth() / 2;
        int cy = revealView.getHeight() / 2;
        float finalRadius = (float) Math.hypot(cx, cy);

        revealView.setVisibility(View.VISIBLE);

        Animator reveal = ViewAnimationUtils.createCircularReveal(
                revealView,
                cx,
                cy,
                0f,
                finalRadius
        );

        reveal.setDuration(900);
        reveal.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());

        reveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fadeInLogo(logo);
            }
        });

        reveal.start();
    }

    private void fadeInLogo(ImageView logo) {

        logo.animate()
                .alpha(1f)
                .setDuration(600)
                .withEndAction(() -> {
                    logo.postDelayed(() -> {
                        startActivity(new Intent(SplashActivity.this, login.class));
                        finish();
                    }, 800);
                })
                .start();
    }
}
