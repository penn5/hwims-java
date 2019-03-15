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

import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

class HwModemCapability {

    private static final String TAG = "HwModemCapability";
    private static String MODEM_CAP = SystemProperties.get("persist.radio.modem.cap", "");

    public static boolean isCapabilitySupport(int capability) {
        boolean z = true;
        int bcdIndex = capability / 4;
        int bcdOffset = capability % 4;
        if (capability < 0 || capability >= 360) {
            return false;
        }
        if (TextUtils.isEmpty(MODEM_CAP)) {
            MODEM_CAP = SystemProperties.get("persist.radio.modem.cap", "");
        }
        try {
            int bcdValue = convertChar2Int(MODEM_CAP.charAt(bcdIndex));
            if (bcdValue != -1) {
                if (((1 << (3 - bcdOffset)) & bcdValue) <= 0) {
                    z = false;
                }
                return z;
            }
        } catch (IndexOutOfBoundsException ex) {
            Log.w(TAG, "isCapabilitySupport", ex);
        }
        return false;
    }

    private static int convertChar2Int(char origChar) {
        if (origChar >= '0' && origChar <= '9') {
            return origChar - 48;
        }
        if (origChar >= 'a' && origChar <= 'f') {
            return (origChar - 97) + 10;
        }
        if (origChar < 'A' || origChar > 'F') {
            return -1;
        }
        return (origChar - 65) + 10;
    }
}
