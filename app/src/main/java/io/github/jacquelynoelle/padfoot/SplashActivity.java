package io.github.jacquelynoelle.padfoot;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.FullScreen);
        setContentView(R.layout.activity_splash);

        final int SPLASH_DURATION = 1000;  // show splash for 1s
        showSplashScreen(SPLASH_DURATION);
    }

    private void showSplashScreen(int duration) {
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
            }, duration);
        }
    }