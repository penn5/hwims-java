/*
 * This file is part of HwIms
 * Copyright (C) 2019 Penn Mackintosh
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.huawei.ims;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Switch;

public class MainActivity extends Activity {
    private Object x;
    private SharedPreferences prefs;
    private boolean ims0state;
    private boolean ims1state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.x = new Object();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.x = null;
        prefs = getSharedPreferences("config", MODE_PRIVATE);
        ims0state = prefs.getBoolean("ims0", true);
        ims1state = prefs.getBoolean("ims1", false);
        ((Switch) findViewById(R.id.ims0)).setChecked(ims0state);
        ((Switch) findViewById(R.id.ims1)).setChecked(ims1state);
        if (!HwImsService.supportsDualIms(this)) {
            ((Switch) findViewById(R.id.ims1)).setChecked(false);
            findViewById(R.id.ims1).setEnabled(false);
        } else {
            findViewById(R.id.ims1).setEnabled(true);
        }
    }

    public void restartRILD0(View view) throws RemoteException {
        try {
            RilHolder.INSTANCE.getRadio(0).restartRILD(RilHolder.getNextSerial());
        } catch (NullPointerException ignored) {
        }
    }

    public void restartRILD1(View view) throws RemoteException {
        try {
            RilHolder.INSTANCE.getRadio(1).restartRILD(RilHolder.getNextSerial());
        } catch (NullPointerException ignored) {
        }
    }

    public void restartRILD2(View view) throws RemoteException {
        try {
            RilHolder.INSTANCE.getRadio(2).restartRILD(RilHolder.getNextSerial());
        } catch (NullPointerException ignored) {
        }
    }

    @SuppressLint("ApplySharedPref")
    public synchronized void ims0(View view) {
        if (view.isEnabled() && ((Switch) view).isChecked() != ims0state) {
            if (ims0state) {
                // Uncheck
                HwImsService.getInstance().disableIms(0);
            } else {
                // Check
            }
            ims0state = ((Switch) view).isChecked();
            prefs.edit().putBoolean("ims0", ims0state).commit();
        }
    }

    @SuppressLint("ApplySharedPref")
    public synchronized void ims1(View view) {
        if (view.isEnabled() && ((Switch) view).isChecked() != ims1state) {
            if (ims1state) {
                // Uncheck
                HwImsService.getInstance().disableIms(1);
            } else {
                // Check
            }
            ims1state = ((Switch) view).isChecked();
            prefs.edit().putBoolean("ims1", ims1state).commit();
        }
    }
}