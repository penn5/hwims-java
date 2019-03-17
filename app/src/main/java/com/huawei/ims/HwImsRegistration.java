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

import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.stub.ImsRegistrationImplBase;

import com.android.ims.ImsConfig;

public class HwImsRegistration extends ImsRegistrationImplBase {
    private int mSlotId;

    public HwImsRegistration(int slotId) {
        mSlotId = slotId;
    }

    // BEWARE FUTURE ME: https://android-review.googlesource.com/c/platform/frameworks/base/+/809459 is changing this big-time in AOSP Q

    public void notifyRegistered(@ImsRegistrationTech int imsRadioTech) {
        this.onRegistered(imsRadioTech);
        HwImsService.getInstance().getConfig(mSlotId).setConfig(ImsConfig.ConfigConstants.VLT_SETTING_ENABLED, ImsConfig.FeatureValueConstants.ON);
    }

    public void notifyRegistering(@ImsRegistrationTech int imsRadioTech) {
        this.onRegistering(imsRadioTech);
    }

    public void notifyDeregistered(ImsReasonInfo info) {
        HwImsService.getInstance().getConfig(mSlotId).setConfig(ImsConfig.ConfigConstants.VLT_SETTING_ENABLED, ImsConfig.FeatureValueConstants.OFF);
        this.onDeregistered(info);
    }
}
