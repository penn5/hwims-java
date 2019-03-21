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

package com.huawei.ims

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Switch

class MainActivity : Activity() {
    private var prefs: SharedPreferences? = null
    private var ims0state: Boolean = false
    private var ims1state: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = createDeviceProtectedStorageContext().getSharedPreferences("config", Context.MODE_PRIVATE)
        ims0state = prefs!!.getBoolean("ims0", true)
        ims1state = prefs!!.getBoolean("ims1", false)
        (findViewById<View>(R.id.ims0) as Switch).isChecked = ims0state
        (findViewById<View>(R.id.ims1) as Switch).isChecked = ims1state
        if (!HwImsService.supportsDualIms(this)) {
            (findViewById<View>(R.id.ims1) as Switch).isChecked = false
            findViewById<View>(R.id.ims1).isEnabled = false
        } else {
            findViewById<View>(R.id.ims1).isEnabled = true
        }
    }

    fun restartRILD0(view: View) {
        try {
            RilHolder.getRadio(0)!!.restartRILD(RilHolder.getNextSerial())
        } catch (ignored: NullPointerException) {
        }

    }

    fun restartRILD1(view: View) {
        try {
            RilHolder.getRadio(1)!!.restartRILD(RilHolder.getNextSerial())
        } catch (ignored: NullPointerException) {
        }

    }

    fun restartRILD2(view: View) {
        try {
            RilHolder.getRadio(2)!!.restartRILD(RilHolder.getNextSerial())
        } catch (ignored: NullPointerException) {
        }

    }

    @SuppressLint("ApplySharedPref")
    @Synchronized
    fun ims0(view: View) {
        if (view.isEnabled && (view as Switch).isChecked != ims0state) {
            if (ims0state) {
                // Uncheck
                HwImsService.instance?.disableIms(0)
            } else {
                // Check
            }
            ims0state = view.isChecked
            prefs!!.edit().putBoolean("ims0", ims0state).commit()
        }
    }

    @SuppressLint("ApplySharedPref")
    @Synchronized
    fun ims1(view: View) {
        if (view.isEnabled && (view as Switch).isChecked != ims1state) {
            if (ims1state) {
                // Uncheck
                HwImsService.instance?.disableIms(1)
            } else {
                // Check
            }
            ims1state = view.isChecked
            prefs!!.edit().putBoolean("ims1", ims1state).commit()
        }
    }
}