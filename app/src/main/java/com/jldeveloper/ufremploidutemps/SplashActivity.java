package com.jldeveloper.ufremploidutemps;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Start the main Activity

        Intent startIntent=new Intent(this,MainActivity.class);

        startActivity(startIntent);
        finish();
    }
}
