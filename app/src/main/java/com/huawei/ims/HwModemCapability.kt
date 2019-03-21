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

import android.os.SystemProperties
import android.text.TextUtils
import android.util.Log

object HwModemCapability {

    private const val tag = "HwModemCapability"
    private var MODEM_CAP = SystemProperties.get("persist.radio.modem.cap", "")

    fun isCapabilitySupport(capability: Int): Boolean {
        var z = true
        val bcdIndex = capability / 4
        val bcdOffset = capability % 4
        if (capability < 0 || capability >= 360) {
            return false
        }
        if (TextUtils.isEmpty(MODEM_CAP)) {
            MODEM_CAP = SystemProperties.get("persist.radio.modem.cap", "")
        }
        try {
            val bcdValue = convertChar2Int(MODEM_CAP[bcdIndex])
            if (bcdValue != -1) {
                if (1 shl 3 - bcdOffset and bcdValue <= 0) {
                    z = false
                }
                return z
            }
        } catch (ex: IndexOutOfBoundsException) {
            Log.w(tag, "exception", ex)
        }

        return false
    }

    private fun convertChar2Int(origChar: Char): Int {
        if (origChar in '0'..'9') {
            return origChar.toInt() - 48
        }
        if (origChar in 'a'..'f') {
            return origChar.toInt() - 97 + 10
        }
        return if (origChar < 'A' || origChar > 'F') {
            -1
        } else origChar.toInt() - 65 + 10
    }
}
