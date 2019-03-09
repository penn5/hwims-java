package com.hwims;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        refresh(null);
    }

    public void refresh(View v) {
        ((TextView) findViewById(R.id.serviceConnected0)).setText(HwImsService.getInstance().mmTelFeatures[0] == null ? "0 is null" : "0 is valid");
        ((TextView) findViewById(R.id.serviceConnected1)).setText(HwImsService.getInstance().mmTelFeatures[0] == null ? "0 is null" : "0 is valid");
        ((TextView) findViewById(R.id.serviceConnected2)).setText(HwImsService.getInstance().mmTelFeatures[0] == null ? "0 is null" : "0 is valid");
    }
}
