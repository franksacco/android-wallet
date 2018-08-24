package com.franksacco.wallet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;


/**
 * Launch activity class
 */
public class LaunchActivity extends AppCompatActivity {

    private static final String TAG = "LaunchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "launch terminated");
        this.startActivity(new Intent(this.getApplicationContext(), MainActivity.class));
        this.finish();
    }

}
