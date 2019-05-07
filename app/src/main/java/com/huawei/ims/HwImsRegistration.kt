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

import android.telephony.ims.ImsReasonInfo
import android.telephony.ims.stub.ImsRegistrationImplBase

class HwImsRegistration(private val mSlotId: Int) : ImsRegistrationImplBase() {

    // BEWARE FUTURE ME: https://android-review.googlesource.com/c/platform/frameworks/base/+/809459 is changing this big-time in AOSP Q

    fun notifyRegistered(@ImsRegistrationTech imsRadioTech: Int) {
        this.onRegistered(imsRadioTech)
    }

    fun notifyRegistering(@ImsRegistrationTech imsRadioTech: Int) {
        this.onRegistering(imsRadioTech)
    }

    fun notifyDeregistered(info: ImsReasonInfo, @ImsRegistrationTech imsRadioTech: Int) {
        this.onDeregistered(info)
    }
}
