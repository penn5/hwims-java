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

import android.hardware.radio.V1_0.*
import android.os.RemoteException
import android.telephony.Rlog
import android.util.Log
import vendor.huawei.hardware.radio.V1_0.*
import vendor.huawei.hardware.radio.V1_0.IRadioIndication

class HwImsRadioIndication internal constructor(private val mSlotId: Int) : IRadioIndication.Stub() {

    private val tag = "HwImsRadioIndication"

    override fun UnsolMsg(indicationType: Int, msgId: Int, rilUnsolMsgPayload: RILUnsolMsgPayload) {
        Log.v(tag, "indicationType = $indicationType, msgId = $msgId, msgPayload = $rilUnsolMsgPayload")
        // Huawei
        when (msgId) {
            1079 -> imsCallStateChanged(indicationType)
            1122 -> imsCallHeldChange(indicationType)
            else -> Log.w(tag, "Unknown indication type!")
        }
    }

    private fun imsCallStateChanged(indicationType: Int) {
        if (indicationType > 1) { // 1 is the normal one, 0 happens sometimes, 0 seems to mean "call terminated"
            // Weird...
            Rlog.w(tag, "unknown indicationType $indicationType")
        }
        try {
            RilHolder.getRadio(mSlotId)!!.getCurrentImsCalls(RilHolder.getNextSerial())
        } catch (e: RemoteException) {
            Rlog.e(tag, "Error getting current calls", e)
        }

    }

    private fun imsCallHeldChange(indicationType: Int) {
        imsCallStateChanged(indicationType)
        // We can probably optimise this somehow but I don't know how. CallSession checks the status
        // If its held, it will send the correct notifications.
    }

    override fun apDsFlowInfoReport(indicationType: Int, rapDsFlowInfoReport: RILAPDsFlowInfoReport) {
        // Huawei not needed
    }

    override fun dsFlowInfoReport(indicationType: Int, apDsFlowInfoReport: RILAPDsFlowInfoReport) {
        // Huawei not needed
    }

    override fun imsCallModifyEndCauseInd(type: Int, cause: RILImsModifyEndCause) {
        // Huawei
    }

    override fun imsCallModifyInd(type: Int, modify: RILImsCallModify) {
        try {
            RilHolder.getRadio(mSlotId)!!.getCurrentImsCalls(RilHolder.getNextSerial())
        } catch (e: RemoteException) {
            Rlog.e(tag, "Error getting current calls for handover", e)
        }
        // Huawei
    }

    override fun imsCallMtStatusInd(type: Int, imsCallMtStatus: RILImsMtStatusReport) {
        // TODO: MT status indications - Missed incoming call notifications
        Log.d(tag, "Received MT status indication: $type/$imsCallMtStatus")
        // Huawei
    }

    override fun imsHandoverInd(indicationType: Int, imsHandover: RILImsHandover) {
        // Huawei
    }

    override fun imsSrvStatusInd(type: Int, imsSrvStatus: RILImsSrvstatusList) {
        // Huawei
    }

    override fun imsSuppSrvInd(type: Int, idsSuppSacNotification: RILImsSuppSvcNotification) {
        // Huawei
    }

    override fun imsaToVowifiMsg(indicationType: Int, msgs: ArrayList<Byte>) {
        // Huawei
    }

    override fun vsimOtaSmsReport(indicationType: Int, vsimOtaSms: RILVsimOtaSmsResponse) {
        // Huawei
    }

    override fun vtFlowInfoReport(indicationType: Int, rilVtFlowInfoReport: RILVtFlowInfoReport) {
        // Huawei not needed
    }


    // END OF HUAWEI METHODS


    override fun callRing(i: Int, b: Boolean, cdmaSignalInfoRecord: CdmaSignalInfoRecord) {

    }

    override fun callStateChanged(i: Int) {

    }

    override fun cdmaCallWaiting(i: Int, cdmaCallWaiting: CdmaCallWaiting) {

    }

    override fun cdmaInfoRec(i: Int, cdmaInformationRecords: CdmaInformationRecords) {

    }

    override fun cdmaNewSms(i: Int, cdmaSmsMessage: CdmaSmsMessage) {

    }

    override fun cdmaOtaProvisionStatus(i: Int, i1: Int) {

    }

    override fun cdmaPrlChanged(i: Int, i1: Int) {

    }

    override fun cdmaRuimSmsStorageFull(i: Int) {

    }

    override fun cdmaSubscriptionSourceChanged(i: Int, i1: Int) {

    }

    override fun cellInfoList(i: Int, arrayList: ArrayList<CellInfo>) {

    }

    override fun currentSignalStrength(i: Int, signalStrength: SignalStrength) {

    }

    override fun dataCallListChanged(i: Int, arrayList: ArrayList<SetupDataCallResult>) {

    }

    override fun enterEmergencyCallbackMode(i: Int) {

    }

    override fun exitEmergencyCallbackMode(i: Int) {

    }

    override fun hardwareConfigChanged(i: Int, arrayList: ArrayList<HardwareConfig>) {

    }

    override fun imsNetworkStateChanged(i: Int) {
        // AOSP IMS
    }

    override fun indicateRingbackTone(i: Int, b: Boolean) {

    }

    override fun lceData(i: Int, lceDataInfo: LceDataInfo) {

    }

    override fun modemReset(i: Int, s: String) {

    }

    override fun networkStateChanged(i: Int) {

    }

    override fun newBroadcastSms(i: Int, arrayList: ArrayList<Byte>) {

    }

    override fun newSms(i: Int, arrayList: ArrayList<Byte>) {

    }

    override fun newSmsOnSim(i: Int, i1: Int) {

    }

    override fun newSmsStatusReport(i: Int, arrayList: ArrayList<Byte>) {

    }

    override fun nitzTimeReceived(i: Int, s: String, l: Long) {

    }

    override fun onSupplementaryServiceIndication(i: Int, stkCcUnsolSsResult: StkCcUnsolSsResult) {

    }

    override fun onUssd(i: Int, i1: Int, s: String) {

    }

    override fun pcoData(i: Int, pcoDataInfo: PcoDataInfo) {

    }

    override fun radioCapabilityIndication(i: Int, radioCapability: RadioCapability) {

    }

    override fun radioStateChanged(i: Int, i1: Int) {

    }

    override fun resendIncallMute(i: Int) {

    }

    override fun restrictedStateChanged(i: Int, i1: Int) {

    }

    override fun rilConnected(i: Int) {

    }

    override fun simRefresh(i: Int, simRefreshResult: SimRefreshResult) {

    }

    override fun simSmsStorageFull(i: Int) {

    }

    override fun simStatusChanged(i: Int) {

    }

    override fun srvccStateNotify(i: Int, i1: Int) {

    }

    override fun stkCallControlAlphaNotify(i: Int, s: String) {

    }

    override fun stkCallSetup(i: Int, l: Long) {

    }

    override fun stkEventNotify(i: Int, s: String) {

    }

    override fun stkProactiveCommand(i: Int, s: String) {

    }

    override fun stkSessionEnd(i: Int) {

    }

    override fun subscriptionStatusChanged(i: Int, b: Boolean) {

    }

    override fun suppSvcNotify(i: Int, suppSvcNotification: SuppSvcNotification) {

    }

    override fun voiceRadioTechChanged(i: Int, i1: Int) {

    }

}
