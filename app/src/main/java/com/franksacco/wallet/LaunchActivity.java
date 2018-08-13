package com.franksacco.wallet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class LaunchActivity extends AppCompatActivity {

    private static final String TAG = "LaunchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // todo check for new change rates

        Log.d(TAG, "launch routine finished");
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
