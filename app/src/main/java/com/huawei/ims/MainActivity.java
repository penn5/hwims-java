package com.huawei.ims;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
    Object x;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.x = new Object();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.x = null;
    }

    public void doNothing(View v) {
        assert this.x == null;
    }
}