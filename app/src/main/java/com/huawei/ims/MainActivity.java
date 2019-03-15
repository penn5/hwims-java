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