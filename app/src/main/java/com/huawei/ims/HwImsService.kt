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

import android.content.Context
import android.content.SharedPreferences
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.telephony.ims.ImsService
import android.telephony.ims.feature.ImsFeature
import android.telephony.ims.stub.ImsConfigImplBase
import android.telephony.ims.stub.ImsFeatureConfiguration
import android.util.Log

class HwImsService : ImsService() {
    private val mmTelFeatures = arrayOfNulls<HwMmTelFeature>(3)
    private val registrations = arrayOfNulls<HwImsRegistration>(3)
    private val configs = arrayOfNulls<HwImsConfig>(3)
    private var prefs: SharedPreferences? = null
    internal lateinit var subscriptionManager: SubscriptionManager
    internal lateinit var telephonyManager: TelephonyManager

    override fun onCreate() {
        Log.v(LOG_TAG, "HwImsService version " + BuildConfig.GIT_HASH + " created!")
        subscriptionManager = getSystemService(SubscriptionManager::class.java)
        telephonyManager = getSystemService(TelephonyManager::class.java)
        prefs = createDeviceProtectedStorageContext().getSharedPreferences("config", Context.MODE_PRIVATE)
        MapconController.getInstance().init(this)
    }

    override fun onDestroy() {
        Log.v(LOG_TAG, "Shutting down HwImsService...")
        instance = null
    }

    override fun enableIms(slotId: Int) {
        (createMmTelFeature(slotId) as HwMmTelFeature).registerIms()
    }

    override fun disableIms(slotId: Int) {
        (createMmTelFeature(slotId) as HwMmTelFeature).unregisterIms()
    }

    override fun readyForFeatureCreation() {
        if (instance != null && instance !== this) {
            throw RuntimeException()
        }
        instance = this
    }

    override fun querySupportedImsFeatures(): ImsFeatureConfiguration {
        val builder = ImsFeatureConfiguration.Builder()
        if (prefs!!.getBoolean("ims0", true)) {
            builder.addFeature(0, ImsFeature.FEATURE_MMTEL)
        }
        if (supportsDualIms(this) && prefs!!.getBoolean("ims1", false)) {
            builder.addFeature(1, ImsFeature.FEATURE_MMTEL)
        }
        return builder.build()
    }

    override fun createMmTelFeature(slotId: Int): HwMmTelFeature? {
        if (slotId > 0 && !supportsDualIms(this)) {
            return null
        }
        if (mmTelFeatures[slotId] == null) {
            mmTelFeatures[slotId] = HwMmTelFeature.getInstance(slotId)
        }
        return mmTelFeatures[slotId]
    }

    override fun getConfig(slotId: Int): ImsConfigImplBase? {
        if (slotId > 0 && !supportsDualIms(this)) {
            return null
        }
        if (configs[slotId] == null) {
            configs[slotId] = HwImsConfig()
        }
        return configs[slotId]
    }

    override fun getRegistration(slotId: Int): HwImsRegistration? {
        if (slotId > 0 && !supportsDualIms(this)) {
            return null
        }
        if (this.registrations[slotId] == null) {
            registrations[slotId] = HwImsRegistration(slotId)
        }
        return this.registrations[slotId]
    }

    companion object {
        private const val LOG_TAG = "HwImsService"
        var instance: HwImsService? = null

        fun supportsDualIms(context: Context): Boolean {
            return HwModemCapability.isCapabilitySupport(21) && context.getSystemService(TelephonyManager::class.java).phoneCount > 1
        }
    }

}
