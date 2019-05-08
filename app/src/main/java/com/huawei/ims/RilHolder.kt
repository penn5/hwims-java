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

import android.annotation.NonNull
import android.annotation.Nullable
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.hardware.radio.V1_0.RadioResponseInfo
import android.os.RemoteException
import android.util.Log
import vendor.huawei.hardware.radio.V1_0.IRadio
import vendor.huawei.hardware.radio.V1_0.RspMsgPayload
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object RilHolder {

    private const val LOG_TAG = "HwImsRilHolder"
    private val serviceNames = arrayOf("rildi", "rildi2", "rildi3")
    private val responseCallbacks = arrayOfNulls<HwImsRadioResponse>(3)
    private val unsolCallbacks = arrayOfNulls<HwImsRadioIndication>(3)
    private val radioImpls = arrayOfNulls<IRadio>(3)
    private var nextSerial = -1
    private val serialToSlot = ConcurrentHashMap<Int, Int>()
    private val callbacks = ConcurrentHashMap<Int, (RadioResponseInfo, RspMsgPayload?) -> Unit>()
    private val blocks = ConcurrentHashMap<Int, BlockingCallback>()


    @Synchronized
    fun getRadio(slotId: Int): IRadio? {
        if (radioImpls[slotId] == null) {
            try {
                try {
                    radioImpls[slotId] = IRadio.getService(serviceNames[slotId])
                } catch (e: NoSuchElementException) {
                    Log.e(LOG_TAG, "Index oob in rilholder. Bail Out!!!", e)
                    val notificationManager = HwImsService.instance!!.getSystemService(NotificationManager::class.java)
                    val channel = NotificationChannel("HwIms", "HwIms", NotificationManager.IMPORTANCE_HIGH)
                    notificationManager.createNotificationChannel(channel)
                    notificationManager.cancelAll()
                    val n = Notification.Builder(HwImsService.instance, "HwIms")
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle("HwIms not supported")
                            .setContentText("Please uninstall HwIms application from settings ASAP! Caused by broken IRadio or SELinux, try permissive.")
                            .setAutoCancel(true)
                            .build()
                    notificationManager.notify(0, n)
                    android.os.Process.killProcess(android.os.Process.myPid())
                    // We're dead.
                }

                responseCallbacks[slotId] = HwImsRadioResponse(slotId)
                unsolCallbacks[slotId] = HwImsRadioIndication(slotId)
            } catch (e: RemoteException) {
                Log.e(LOG_TAG, "remoteexception getting serivce. will throw npe later ig.")
                throw RuntimeException("Failed to get service due to internal error")
            }

        }
        try {
            radioImpls[slotId]!!.setResponseFunctionsHuawei(responseCallbacks[slotId], unsolCallbacks[slotId])
            radioImpls[slotId]!!.setResponseFunctions(responseCallbacks[slotId], unsolCallbacks[slotId])
        } catch (e: RemoteException) {
            Log.e(LOG_TAG, "Failed to update resp functions!")
        }

        return radioImpls[slotId]!!
    }

    class BlockingCallback {
        private val lock = Object()
        private var done = false
        private var radioResponseInfo: RadioResponseInfo? = null

        fun run(radioResponseInfo: RadioResponseInfo, rspMsgPayload: RspMsgPayload?) {
            synchronized(lock) {
                if (done)
                    throw RuntimeException("May not call the callback twice for the same serial!")
                this.radioResponseInfo = radioResponseInfo
                done = true
                lock.notifyAll()
            }
        }

        fun get(): RadioResponseInfo {
            synchronized(lock) {
                while (!done) {
                    lock.wait()
                }
            }
            return radioResponseInfo!!
            // The lock ensures it's never null. An NPE here means something went really wrong.
        }
    }

    @Synchronized
    fun callback(cb: (RadioResponseInfo, RspMsgPayload?) -> Unit, slotId: Int): Int {
        val serial = getNextSerial()
        serialToSlot[serial] = slotId
        callbacks[serial] = cb
        Log.v(LOG_TAG, "Setting callback for serial $serial")
        return serial
    }

    @Synchronized
    fun getNextSerial(): Int {
        return ++nextSerial
    }

    fun triggerCB(serial: Int, radioResponseInfo: RadioResponseInfo, rspMsgPayload: RspMsgPayload?) {
        Log.e(LOG_TAG, "Incoming response for slot " + serialToSlot[serial] + ", serial " + serial + ", radioResponseInfo " + radioResponseInfo + ", rspMsgPayload " + rspMsgPayload)
        if (callbacks.containsKey(serial))
            callbacks[serial]!!(radioResponseInfo, rspMsgPayload)
    }

    fun prepareBlock(slotId: Int): Int {
        val cb = BlockingCallback()
        val serial = callback(cb::run, slotId)
        blocks[serial] = cb
        return serial
    }

    /*
 * It is safe to call this method multiple times, it will always return the same for the same serial.
 */
    fun blockUntilComplete(serial: Int): RadioResponseInfo {
        return blocks[serial]?.get()
                ?: throw RuntimeException("prepareBlock was not called for this request!")

    }

}

