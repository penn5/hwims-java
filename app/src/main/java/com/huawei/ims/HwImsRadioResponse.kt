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
import android.os.Bundle
import android.telephony.Rlog
import android.telephony.ims.ImsCallProfile
import android.util.Log
import com.android.ims.ImsManager
import vendor.huawei.hardware.radio.V1_0.*
import vendor.huawei.hardware.radio.V1_0.IRadioResponse
import java.util.*

class HwImsRadioResponse internal constructor(private val mSlotId: Int) : IRadioResponse.Stub() {

    override fun RspMsg(radioResponseInfo: RadioResponseInfo, msgType: Int, rspMsgPayload: RspMsgPayload?) {
        Log.d(tag, "rspmsg radioresponseinfo = $radioResponseInfo,msgtype=$msgType")
        Log.d(tag, "serial " + radioResponseInfo.serial)
        Log.d(tag, "type=" + RespCode.getName(msgType))
        /*switch (msgType) {
            case PASS_1:
            case PASS_2:
            case PASS_3:
                return;
            case IMS_DIAL_RESPONSE:
                imsDialResponse(radioResponseInfo);
                break;
            case SET_IMS_CALL_WAITING_RESPONSE:
                setImsCallWaitingResponse(radioResponseInfo);
                break;
            case GET_LTE_INFO_RESPONSE:
                getLteInfoResponse(radioResponseInfo);
                break;
            case ACCEPT_IMS_CALL_RESPONSE:
                acceptImsCallResponse(radioResponseInfo);

        }*/
        // Huawei
        RilHolder.triggerCB(radioResponseInfo.serial, radioResponseInfo, rspMsgPayload)
    }
    /*
    public static final int IMS_DIAL_RESPONSE = 0XDC;
    public static final int SET_IMS_CALL_WAITING_RESPONSE = 0X100;
    public static final int GET_LTE_INFO_RESPONSE = 0X136;
    public static final int ACCEPT_IMS_CALL_RESPONSE = 0XE7;
    public static final int SET_DMPCSCF_RESPONSE = 0X13C;
    public static final int SET_DMDYN_RESPONSE = 0X13D;
    public static final int SET_DMTIMER_RESPONSE = 0X13E;
    public static final int SET_DMSMS_RESPONSE = 0X13F;
    public static final int GET_DMPCSCF_RESPONSE = 0X140;
    public static final int GET_DMTIMER_RESPONSE = 0X141;
    public static final int GET_DMDYN_RESPONSE = 0X142;
    public static final int GET_DMSMS_RESPONSE = 0X143;
    public static final int GET_DMUSER_RESPONSE = 0X144;
    public static final int WIFI_EMERGENCY_AID = 0X151;
    public static final int SEND_BATTERY_STATUS_RESPONSE = 0X147;
    public static final int MODIFY_IMS_CALL_INITIATE_RESPONSE = 0X113;
    public static final int MODIFY_IMS_CALL_CONFIRM_RESPONSE = 0X114;
    public static final int GET_IMS_IMPU_RESPONSE = 0XF6;
    public static final int SET_IMS_VT_CAPABILITY_RESPONSE = 0X150;
    public static final int IMS_LAST_CALL_FAIL_REASON_INFO_RESPONSE = 0X14F;
    public static final int SWITCH_WAITING_OR_HOLDING_AND_ACTIVE_FOR_IMS_RESPONSE = 0X156;
    public static final int PASS_1 = 0XE3;
    public static final int PASS_2 = 0X35;
    public static final int PASS_3 = 0X36;*/

    override fun deactivateDataCallEmergencyResponse(radioResponseInfo: RadioResponseInfo) {
        // Huawei
    }

    override fun getAvailableCsgIdsResponse(radioResponseInfo: RadioResponseInfo, arrayList: ArrayList<CsgNetworkInfo>) {
        // Huawei
    }

    override fun getCellInfoListOtdoaResponse(radioResponseInfo: RadioResponseInfo, arrayList: ArrayList<CellInfo>) {
        //   Huawei
    }

    override fun getCurrentImsCallsResponse(radioResponseInfo: RadioResponseInfo, arrayList: ArrayList<RILImsCall>) {
        // Huawei
        synchronized(HwImsCallSession.sCallsLock) {
            val calls = ArrayList<Int>(arrayList.size)
            for (call in arrayList) {
                Log.d(tag, "calls list contains " + redactCall(call))
                // RIL sometimes gives us the leading +, so first try with one, and if its null, try again without the +.
                var session = HwImsCallSession.awaitingIdFromRIL["+" + call.number]
                if (session == null)
                    session = HwImsCallSession.awaitingIdFromRIL[call.number]
                if (session != null) {
                    Rlog.d(tag, "giving call id from ril.")
                    session.addIdFromRIL(call)
                }
                session = HwImsCallSession.calls[call.index]
                if (session == null) {
                    if (call.isMT > 0) {
                        Log.d(tag, "Notifying MmTelFeature incoming call! " + redactCall(call))
                        // An incoming call that we have never seen before, tell the framework.
                    } else {
                        Log.e(tag, "Phantom Call!!!! " + redactCall(call))
                        HwImsCallSession.calls.forEach { s, hwImsCallSession -> Rlog.d(tag, "Phantom debugging got call in static calls " + redactCall(hwImsCallSession.rilImsCall!!) + " with number " + s) }
                        HwImsCallSession.awaitingIdFromRIL.forEach { s, hwImsCallSession -> Rlog.d(tag, "Phantom debugging got call in static awaiting " + hwImsCallSession.mCallee + " with number " + s) }
                        // Someone has been talking to AT... naughty.
                    }
                    val extras = Bundle()
                    val callSession = HwImsCallSession(mSlotId, ImsCallProfile(), call)
                    extras.putInt(ImsManager.EXTRA_PHONE_ID, mSlotId)
                    extras.putString(ImsManager.EXTRA_CALL_ID, callSession.callId)
                    extras.putBoolean(ImsManager.EXTRA_IS_UNKNOWN_CALL, call.isMT.toInt() == 0) // A new outgoing call should never happen. Someone is playing with AT commands or talking to the modem.
                    HwImsService.instance!!.createMmTelFeature(mSlotId)!!.notifyIncomingCall(callSession, extras)


                } else {
                    // Existing call, update it's data.
                    session.updateCall(call)
                }
                if (call.isMpty > 0 && call.state == 2) { // Dialing & Multiparty
                    // It is a new conference call being added.
                    for (confSession in HwImsCallSession.calls.values) {
                        if (confSession.isMultiparty) {
                            Rlog.d(tag, "adding call " + call.index + " to conference " + confSession.callId)
                            confSession.notifyConfDone(call)
                            break
                        }
                    }
                }
                calls.add(call.index)
            }
            for ((_, value) in HwImsCallSession.calls) {
                if (!calls.contains(value.rilImsCall!!.index)) {
                    try {
                        Rlog.d(tag, "notifying dead call " + redactCall(value.rilImsCall!!))
                        value.notifyEnded()
                    } catch (e: RuntimeException) {
                        Rlog.e(tag, "error notifying dead call!", e)
                    }

                }
            }
        }
    }

    private fun redactCall(call: RILImsCall): String {
        return "{.state = " + call.state + ", .index = " + call.index + ", .toa = " + call.toa + ", .isMpty = " + call.isMpty + ", .isMT = " + call.isMT + ", .als = " + call.als + ", .isVoice = " + call.isVoice + ", .isVoicePrivacy = " + call.isVoicePrivacy + ", .number = " + Rlog.pii(tag, call.number) + ", .numberPresentation = " + call.numberPresentation + ", .name = " + Rlog.pii(tag, call.name) + ", .namePresentation = " + call.namePresentation + ", .callDetails = " + call.callDetails.toString() + ", .isEConference = " + call.isECOnference + ", .peerVideoSupport = " + call.peerVideoSupport + "}"
    }

    override fun getDeviceVersionResponse(radioResponseInfo: RadioResponseInfo, rilDeviceVersionResponse: RILDeviceVersionResponse) {
        // Huawei
    }

    override fun getDsFlowInfoResponse(radioResponseInfo: RadioResponseInfo, rilDsFlowInfoResponse: RILDsFlowInfoResponse) {
        // Huawei
    }

    override fun getPolListResponse(radioResponseInfo: RadioResponseInfo, rilPreferredPLMNSelector: RILPreferredPLMNSelector) {
        // Huawei
    }

    override fun getSystemInfoExResponse(radioResponseInfo: RadioResponseInfo, rilradiosysinfo: RILRADIOSYSINFO) {
        // Huawei
    }

    override fun manualSelectionCsgIdResponse(radioResponseInfo: RadioResponseInfo) {
        // Huawei
    }

    override fun setupDataCallEmergencyResponse(radioResponseInfo: RadioResponseInfo, setupDataCallResult: SetupDataCallResult) {
        // Huawei
    }

    override fun uiccAuthResponse(radioResponseInfo: RadioResponseInfo, riluiccauthresponse: RILUICCAUTHRESPONSE) {
        // Huawei
    }

    // END OF HUAWEI METHODS

    override fun acceptCallResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun acknowledgeIncomingGsmSmsWithPduResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun acknowledgeLastIncomingCdmaSmsResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun acknowledgeLastIncomingGsmSmsResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun acknowledgeRequest(i: Int) {

    }

    override fun cancelPendingUssdResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun changeIccPin2ForAppResponse(radioResponseInfo: RadioResponseInfo, i: Int) {

    }

    override fun changeIccPinForAppResponse(radioResponseInfo: RadioResponseInfo, i: Int) {

    }

    override fun conferenceResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun deactivateDataCallResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun deleteSmsOnRuimResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun deleteSmsOnSimResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun dialResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun exitEmergencyCallbackModeResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun explicitCallTransferResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun getAllowedCarriersResponse(radioResponseInfo: RadioResponseInfo, b: Boolean, carrierRestrictions: CarrierRestrictions) {

    }

    override fun getAvailableBandModesResponse(radioResponseInfo: RadioResponseInfo, arrayList: ArrayList<Int>) {

    }

    override fun getAvailableNetworksResponse(radioResponseInfo: RadioResponseInfo, arrayList: ArrayList<OperatorInfo>) {

    }

    override fun getBasebandVersionResponse(radioResponseInfo: RadioResponseInfo, s: String) {

    }

    override fun getCDMASubscriptionResponse(radioResponseInfo: RadioResponseInfo, s: String, s1: String, s2: String, s3: String, s4: String) {

    }

    override fun getCallForwardStatusResponse(radioResponseInfo: RadioResponseInfo, arrayList: ArrayList<CallForwardInfo>) {

    }

    override fun getCallWaitingResponse(radioResponseInfo: RadioResponseInfo, b: Boolean, i: Int) {

    }

    override fun getCdmaBroadcastConfigResponse(radioResponseInfo: RadioResponseInfo, arrayList: ArrayList<CdmaBroadcastSmsConfigInfo>) {

    }

    override fun getCdmaRoamingPreferenceResponse(radioResponseInfo: RadioResponseInfo, i: Int) {

    }

    override fun getCdmaSubscriptionSourceResponse(radioResponseInfo: RadioResponseInfo, i: Int) {

    }

    override fun getCellInfoListResponse(radioResponseInfo: RadioResponseInfo, arrayList: ArrayList<CellInfo>) {

    }

    override fun getClipResponse(radioResponseInfo: RadioResponseInfo, i: Int) {

    }

    override fun getClirResponse(radioResponseInfo: RadioResponseInfo, i: Int, i1: Int) {

    }

    override fun getCurrentCallsResponse(radioResponseInfo: RadioResponseInfo, arrayList: ArrayList<Call>) {

    }

    override fun getDataCallListResponse(radioResponseInfo: RadioResponseInfo, arrayList: ArrayList<SetupDataCallResult>) {

    }

    override fun getDataRegistrationStateResponse(radioResponseInfo: RadioResponseInfo, dataRegStateResult: DataRegStateResult) {

    }

    override fun getDeviceIdentityResponse(radioResponseInfo: RadioResponseInfo, s: String, s1: String, s2: String, s3: String) {

    }

    override fun getFacilityLockForAppResponse(radioResponseInfo: RadioResponseInfo, i: Int) {

    }

    override fun getGsmBroadcastConfigResponse(radioResponseInfo: RadioResponseInfo, arrayList: ArrayList<GsmBroadcastSmsConfigInfo>) {

    }

    override fun getHardwareConfigResponse(radioResponseInfo: RadioResponseInfo, arrayList: ArrayList<HardwareConfig>) {

    }

    override fun getIMSIForAppResponse(radioResponseInfo: RadioResponseInfo, s: String) {

    }

    override fun getIccCardStatusResponse(radioResponseInfo: RadioResponseInfo, cardStatus: CardStatus) {

    }

    override fun getImsRegistrationStateResponse(radioResponseInfo: RadioResponseInfo, b: Boolean, i: Int) {

    }

    override fun getLastCallFailCauseResponse(radioResponseInfo: RadioResponseInfo, lastCallFailCauseInfo: LastCallFailCauseInfo) {

    }

    override fun getModemActivityInfoResponse(radioResponseInfo: RadioResponseInfo, activityStatsInfo: ActivityStatsInfo) {

    }

    override fun getMuteResponse(radioResponseInfo: RadioResponseInfo, b: Boolean) {

    }

    override fun getNeighboringCidsResponse(radioResponseInfo: RadioResponseInfo, arrayList: ArrayList<NeighboringCell>) {

    }

    override fun getNetworkSelectionModeResponse(radioResponseInfo: RadioResponseInfo, b: Boolean) {

    }

    override fun getOperatorResponse(radioResponseInfo: RadioResponseInfo, s: String, s1: String, s2: String) {

    }

    override fun getPreferredNetworkTypeResponse(radioResponseInfo: RadioResponseInfo, i: Int) {

    }

    override fun getPreferredVoicePrivacyResponse(radioResponseInfo: RadioResponseInfo, b: Boolean) {

    }

    override fun getRadioCapabilityResponse(radioResponseInfo: RadioResponseInfo, radioCapability: RadioCapability) {

    }

    override fun getSignalStrengthResponse(radioResponseInfo: RadioResponseInfo, signalStrength: SignalStrength) {

    }

    override fun getSmscAddressResponse(radioResponseInfo: RadioResponseInfo, s: String) {

    }

    override fun getTTYModeResponse(radioResponseInfo: RadioResponseInfo, i: Int) {

    }

    override fun getVoiceRadioTechnologyResponse(radioResponseInfo: RadioResponseInfo, i: Int) {

    }

    override fun getVoiceRegistrationStateResponse(radioResponseInfo: RadioResponseInfo, voiceRegStateResult: VoiceRegStateResult) {

    }

    override fun handleStkCallSetupRequestFromSimResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun hangupConnectionResponse(radioResponseInfo: RadioResponseInfo) {
        RspMsg(radioResponseInfo, -1, null)
    }

    override fun hangupForegroundResumeBackgroundResponse(radioResponseInfo: RadioResponseInfo) {
        RspMsg(radioResponseInfo, -1, null)
    }

    override fun hangupWaitingOrBackgroundResponse(radioResponseInfo: RadioResponseInfo) {
        RspMsg(radioResponseInfo, -1, null)
    }

    override fun iccCloseLogicalChannelResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun iccIOForAppResponse(radioResponseInfo: RadioResponseInfo, iccIoResult: IccIoResult) {

    }

    override fun iccOpenLogicalChannelResponse(radioResponseInfo: RadioResponseInfo, i: Int, arrayList: ArrayList<Byte>) {

    }

    override fun iccTransmitApduBasicChannelResponse(radioResponseInfo: RadioResponseInfo, iccIoResult: IccIoResult) {

    }

    override fun iccTransmitApduLogicalChannelResponse(radioResponseInfo: RadioResponseInfo, iccIoResult: IccIoResult) {

    }

    override fun nvReadItemResponse(radioResponseInfo: RadioResponseInfo, s: String) {

    }

    override fun nvResetConfigResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun nvWriteCdmaPrlResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun nvWriteItemResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun pullLceDataResponse(radioResponseInfo: RadioResponseInfo, lceDataInfo: LceDataInfo) {

    }

    override fun rejectCallResponse(radioResponseInfo: RadioResponseInfo) {
        RspMsg(radioResponseInfo, -1, null)
    }

    override fun reportSmsMemoryStatusResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun reportStkServiceIsRunningResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun requestIccSimAuthenticationResponse(radioResponseInfo: RadioResponseInfo, iccIoResult: IccIoResult) {

    }

    override fun requestIsimAuthenticationResponse(radioResponseInfo: RadioResponseInfo, s: String) {

    }

    override fun requestShutdownResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun sendBurstDtmfResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun sendCDMAFeatureCodeResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun sendCdmaSmsResponse(radioResponseInfo: RadioResponseInfo, sendSmsResult: SendSmsResult) {

    }

    override fun sendDeviceStateResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun sendDtmfResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun sendEnvelopeResponse(radioResponseInfo: RadioResponseInfo, s: String) {

    }

    override fun sendEnvelopeWithStatusResponse(radioResponseInfo: RadioResponseInfo, iccIoResult: IccIoResult) {

    }

    override fun sendImsSmsResponse(radioResponseInfo: RadioResponseInfo, sendSmsResult: SendSmsResult) {

    }

    override fun sendSMSExpectMoreResponse(radioResponseInfo: RadioResponseInfo, sendSmsResult: SendSmsResult) {

    }

    override fun sendSmsResponse(radioResponseInfo: RadioResponseInfo, sendSmsResult: SendSmsResult) {

    }

    override fun sendTerminalResponseToSimResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun sendUssdResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun separateConnectionResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setAllowedCarriersResponse(radioResponseInfo: RadioResponseInfo, i: Int) {

    }

    override fun setBandModeResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setBarringPasswordResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setCallForwardResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setCallWaitingResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setCdmaBroadcastActivationResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setCdmaBroadcastConfigResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setCdmaRoamingPreferenceResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setCdmaSubscriptionSourceResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setCellInfoListRateResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setClirResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setDataAllowedResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setDataProfileResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setFacilityLockForAppResponse(radioResponseInfo: RadioResponseInfo, i: Int) {

    }

    override fun setGsmBroadcastActivationResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setGsmBroadcastConfigResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setIndicationFilterResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setInitialAttachApnResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setLocationUpdatesResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setMuteResponse(radioResponseInfo: RadioResponseInfo) {
        RspMsg(radioResponseInfo, -1, null)
    }

    override fun setNetworkSelectionModeAutomaticResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setNetworkSelectionModeManualResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setPreferredNetworkTypeResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setPreferredVoicePrivacyResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setRadioCapabilityResponse(radioResponseInfo: RadioResponseInfo, radioCapability: RadioCapability) {

    }

    override fun setRadioPowerResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setSimCardPowerResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setSmscAddressResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setSuppServiceNotificationsResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setTTYModeResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setUiccSubscriptionResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun setupDataCallResponse(radioResponseInfo: RadioResponseInfo, setupDataCallResult: SetupDataCallResult) {

    }

    override fun startDtmfResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun startLceServiceResponse(radioResponseInfo: RadioResponseInfo, lceStatusInfo: LceStatusInfo) {

    }

    override fun stopDtmfResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun stopLceServiceResponse(radioResponseInfo: RadioResponseInfo, lceStatusInfo: LceStatusInfo) {

    }

    override fun supplyIccPin2ForAppResponse(radioResponseInfo: RadioResponseInfo, i: Int) {

    }

    override fun supplyIccPinForAppResponse(radioResponseInfo: RadioResponseInfo, i: Int) {

    }

    override fun supplyIccPuk2ForAppResponse(radioResponseInfo: RadioResponseInfo, i: Int) {

    }

    override fun supplyIccPukForAppResponse(radioResponseInfo: RadioResponseInfo, i: Int) {

    }

    override fun supplyNetworkDepersonalizationResponse(radioResponseInfo: RadioResponseInfo, i: Int) {

    }

    override fun switchWaitingOrHoldingAndActiveResponse(radioResponseInfo: RadioResponseInfo) {

    }

    override fun writeSmsToRuimResponse(radioResponseInfo: RadioResponseInfo, i: Int) {

    }

    override fun writeSmsToSimResponse(radioResponseInfo: RadioResponseInfo, i: Int) {

    }

    enum class RespCode(var value: Int) {
        IMS_DIAL_RESPONSE(0xdc), SET_IMS_CALL_WAITING_RESPONSE(0x100),
        GET_LTE_INFO_RESPONSE(0x136), ACCEPT_IMS_CALL_RESPONSE(0xe7),
        SET_DMPCSCF_RESPONSE(0x13c), SET_DMDYN_RESPONSE(0x13d),
        SET_DMTIMER_RESPONSE(0x13e), SET_DMSMS_RESPONSE(0x13f),
        GET_DMPCSCF_RESPONSE(0x140), GET_DMTIMER_RESPONSE(0x141),
        GET_DMDYN_RESPONSE(0x142), GET_DMSMS_RESPONSE(0x143),
        GET_DMUSER_RESPONSE(0x144), WIFI_EMERGENCY_AID(0x151),
        SEND_BATTERY_STATUS_RESPONSE(0x147), MODIFY_IMS_CALL_INITIATE_RESPONSE(0x133),
        MODIFY_IMS_CALL_CONFIRM_RESPONSE(0x114), GET_IMS_IMPU_RESPONSE(0xf6),
        SET_IMS_VT_CAPABILITY_RESPONSE(0x150), IMS_LAST_CALL_FAIL_REASON_INFO_RESPONSE(0x14f),
        SWITCH_WAITING_OR_HOLDING_AND_ACTIVE_FOR_IMS_RESPONSE(0x156),
        PASS1(0xe3), PASS2(0x35), PASS3(0x36);


        companion object {

            fun getName(code: Int): String {
                val x = Arrays.stream(RespCode.values()).filter { resp_code -> resp_code.value == code }.findAny()
                return if (x.isPresent) {
                    x.get().name
                } else {
                    Integer.toString(code)
                }
            }
        }

    }

    companion object {
        private const val tag = "HwImsRadioResponse"
    }


}
