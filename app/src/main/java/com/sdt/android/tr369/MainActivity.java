package com.sdt.android.tr369;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.sdt.diagnose.common.GlobalContext;

/*
 * Main Activity class that loads {@link MainFragment}.
 */
public class MainActivity extends FragmentActivity {
    private final static String TAG = "MainActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, " ############ Outis ### MainActivity create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GlobalContext.setContext(getApplicationContext());

        startForegroundService(new Intent(getApplicationContext(), SdtTr369Service.class));
    }
}