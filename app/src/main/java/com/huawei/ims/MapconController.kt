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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.telephony.ims.ImsReasonInfo
import android.telephony.ims.stub.ImsRegistrationImplBase
import android.util.Log
import com.hisi.mapcon.IMapconService

class MapconController : ServiceConnection {

    private var service: IMapconService? = null

    override fun onServiceDisconnected(name: ComponentName?) {
        Log.e(tag, "Mapcon Disconnected")
        this.service = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Log.d(tag, "Mapcon Connected!")
        this.service = service as? IMapconService
    }

    override fun onBindingDied(name: ComponentName?) {
        Log.e(tag, "Binding died!")
    }

    override fun onNullBinding(name: ComponentName?) {
        Log.e(tag, "Null Binding!")
    }

    fun turnVowifiOn(phoneId: Int) {
        Log.d(tag, "turnVowifiOn($phoneId)")
        service?.setVoWifiOn(phoneId)
        HwImsService.instance!!.getRegistration(phoneId)?.notifyRegistered(ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN)
    }

    fun turnVowifiOff(phoneId: Int) {
        Log.d(tag, "turnVowifiOff($phoneId)")
        service?.setVoWifiOff(phoneId)
        HwImsService.instance!!.getRegistration(phoneId)?.notifyDeregistered(ImsReasonInfo(), ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN)
    }

    fun setDomain(phoneId: Int, domain: Int) {
        Log.d(tag, "setDomain($phoneId, $domain)")
        service?.setDomain(phoneId, domain)
    }

    fun notifyRoaming(phoneId: Int) {
        Log.d(tag, "notifyRoaming($phoneId)")
        service?.notifyRoaming(phoneId)
    }

    fun init(context: Context) {
        val i = Intent()
        i.setClassName("com.hisi.mapcon", "com.hisi.mapcon.MapconService")
        // Mapcon doesn't work, and it seems to be more trouble than it's worth.
        // I'm leaving this here in case I ever try again.
        //context.startService(i)
        //context.bindService(i, this, Context.BIND_IMPORTANT or Context.BIND_ABOVE_CLIENT)
        Log.d(tag, "Requesting bind for Mapcon.")
    }

    companion object {
        private var INSTANCE: MapconController? = null
        fun getInstance(): MapconController {
            return INSTANCE ?: { INSTANCE = MapconController(); INSTANCE!! }()
        }

        private const val tag = "MapconController"
    }
}