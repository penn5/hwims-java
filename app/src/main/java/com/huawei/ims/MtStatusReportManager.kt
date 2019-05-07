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

object MtStatusReportConstants {
    const val FAIL_CAUSE_BASE = 0X8000

    enum class FAIL_CAUSES(val err: Int) {
        CANCEL_CALL_NOT_FOUND(0X8),
        DUPLICATE_INVITE_CANCELLED(0X4),
        DUPLICATE_INVITE_IDLE(0X1),
        DUPLICATE_INVITE_INVITED(0X2),
        DUPLICATE_INVITE_RANG(0X3),
        DUPLICATE_INVITE_UNKNOWN(0X5),
        HUNG_WHEN_MONITOR_EXPIRES(0X6),
        MT_FAIL_CALLER_KNOWN(0X0),
        MT_FAIL_CALLER_UNKNOWN(0XC),
        NO_RING_OR_CANCEL_ANMS(0XA),
        NO_RING_OR_CANCEL_CALL(0X9),
        RING_WHEN_MONITOR_EXPIRES(0X7),
        RING_WHEN_NO_SERVICE(0XB)
    }

    const val MISSED_CALL_BASE = 0X3E0

    enum class MISSED_CALLS(val code: Int) {
        NON_DIALABLE_CALL_EVENT(0XE),
        REMINDER_EVENT(0XA),
        REMINDER_GENERAL_EVENT(0XB),
        RING_TIMER_OUT_HUNG_EVENT(0XD),
        RING_TIMER_OUT_RING_EVENT(0XC)
    }
}