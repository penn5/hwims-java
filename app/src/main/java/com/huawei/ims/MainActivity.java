package com.huawei.ims;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;

public class MainActivity extends Activity {
    private Object x;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.x = new Object();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.x = null;
    }

    public void restartRILD0(View view) throws RemoteException {
        RilHolder.INSTANCE.getRadio(0).restartRILD(RilHolder.getNextSerial());
    }

    public void restartRILD1(View view) throws RemoteException {
        RilHolder.INSTANCE.getRadio(1).restartRILD(RilHolder.getNextSerial());
    }

    public void restartRILD2(View view) throws RemoteException {
        RilHolder.INSTANCE.getRadio(2).restartRILD(RilHolder.getNextSerial());
    }

    public void fakeReg0(View view) {
        HwImsService.getInstance().enableIms(0);
    }

    public void fakeReg1(View view) {
        HwImsService.getInstance().enableIms(1);
    }

    public void fakeReg2(View view) {
        HwImsService.getInstance().enableIms(2);
    }
}