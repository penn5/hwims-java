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

package com.huawei.ims.video

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log

// TODO: Huawei leak this too. Maybe we should use an instantiable class instead though?
@SuppressLint("StaticFieldLeak")
object ImsThinClient {

    const val tag = "HwImsThinClient"
    var mContext: Context? = null

    init {
        System.loadLibrary("vsc")
    }

    fun init(context: Context) {
        Log.v(tag, "Initializing VSC client!")
        mContext = context
        nativeInit("", /* Who knows what goes here. I put today's date. */ "2019-05-09", context)
    }

    fun deinit() {
        Log.v(tag, "De-initializing VSC client!")
        nativeDestroy()
    }

    fun startVideo(): Int {
        Log.v(tag, "Starting RMT video!")
        return startRmtVideo()
    }

    fun stopVideo(): Int {
        Log.v(tag, "Stopping RMT video!")
        return stopRmtVideo()
    }

    fun resumeVideo(): Int {
        Log.v(tag, "Resuming RMT video!")
        return resumeRmtVideo()
    }

    private external fun driveSdk(i1: Int): Int
    private external fun enableHmeLog(b1: Boolean)
    private external fun enableSetInitMaxBitRate(b1: Boolean)
    private external fun getImsLpdcpThreshold(): Int
    private external fun getParaInt(i1: Int): Int
    //getQosInfo([Lcom/huawei/vtproxy/QosInfo;)I
    private external fun nativeDestroy(): Int

    private external fun nativeInit(blank: String, date: String, context: Context): Int
    private external fun pauseRTPStream(i1: Int): Int
    private external fun pauseRmtVideo(): Int
    private external fun resumeRTPStream(i1: Int): Int
    private external fun resumeRmtVideo(): Int
    private external fun setCurrentSession(i1: Int): Int
    private external fun setImsBuffInfo(l1: Long, l2: Long, l3: Long, l4: Long): Int
    private external fun setImsRlQualInfo(i1: Int, i2: Int, i3: Int, i4: Int): Int
    private external fun setParaInt(i1: Int, i2: Int): Int
    private external fun setRemoteView(@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN") o1: java.lang.Object): Int
    private external fun startRmtVideo(): Int
    private external fun startRotateLocalRmtVideo(i1: Int, b1: Boolean): Int
    private external fun stopRmtVideo(): Int
    private external fun zpandModDriveMsg()
    private external fun zpandTimerActive()
}