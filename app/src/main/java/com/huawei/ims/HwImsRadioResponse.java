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

import android.hardware.radio.V1_0.ActivityStatsInfo;
import android.hardware.radio.V1_0.Call;
import android.hardware.radio.V1_0.CallForwardInfo;
import android.hardware.radio.V1_0.CardStatus;
import android.hardware.radio.V1_0.CarrierRestrictions;
import android.hardware.radio.V1_0.CdmaBroadcastSmsConfigInfo;
import android.hardware.radio.V1_0.CellInfo;
import android.hardware.radio.V1_0.DataRegStateResult;
import android.hardware.radio.V1_0.GsmBroadcastSmsConfigInfo;
import android.hardware.radio.V1_0.HardwareConfig;
import android.hardware.radio.V1_0.IccIoResult;
import android.hardware.radio.V1_0.LastCallFailCauseInfo;
import android.hardware.radio.V1_0.LceDataInfo;
import android.hardware.radio.V1_0.LceStatusInfo;
import android.hardware.radio.V1_0.NeighboringCell;
import android.hardware.radio.V1_0.OperatorInfo;
import android.hardware.radio.V1_0.RadioCapability;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.hardware.radio.V1_0.SendSmsResult;
import android.hardware.radio.V1_0.SetupDataCallResult;
import android.hardware.radio.V1_0.SignalStrength;
import android.hardware.radio.V1_0.VoiceRegStateResult;
import android.os.Bundle;
import android.telephony.Rlog;
import android.telephony.ims.ImsCallProfile;
import android.util.Log;

import com.android.ims.ImsManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import vendor.huawei.hardware.radio.V1_0.CsgNetworkInfo;
import vendor.huawei.hardware.radio.V1_0.IRadioResponse;
import vendor.huawei.hardware.radio.V1_0.RILDeviceVersionResponse;
import vendor.huawei.hardware.radio.V1_0.RILDsFlowInfoResponse;
import vendor.huawei.hardware.radio.V1_0.RILImsCall;
import vendor.huawei.hardware.radio.V1_0.RILPreferredPLMNSelector;
import vendor.huawei.hardware.radio.V1_0.RILRADIOSYSINFO;
import vendor.huawei.hardware.radio.V1_0.RILUICCAUTHRESPONSE;
import vendor.huawei.hardware.radio.V1_0.RspMsgPayload;

public class HwImsRadioResponse extends IRadioResponse.Stub {
    private static final String LOG_TAG = "HwImsRadioResponse";
    private final int mSlotId;

    public HwImsRadioResponse(int slotId) {
        mSlotId = slotId;
    }

    @Override
    public void RspMsg(RadioResponseInfo radioResponseInfo, int msgType, RspMsgPayload rspMsgPayload) {
        Log.d(LOG_TAG, "rspmsg radioresponseinfo = " + radioResponseInfo + ",msgtype=" + msgType);
        Log.d(LOG_TAG, "serial " + radioResponseInfo.serial);
        Log.d(LOG_TAG, "type=" + RESP_CODE.getName(msgType));
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
        RilHolder.triggerCB(radioResponseInfo.serial, radioResponseInfo, rspMsgPayload);
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

    @Override
    public void deactivateDataCallEmergencyResponse(RadioResponseInfo radioResponseInfo) {
        // Huawei
    }

    @Override
    public void getAvailableCsgIdsResponse(RadioResponseInfo radioResponseInfo, ArrayList<CsgNetworkInfo> arrayList) {
        // Huawei
    }

    @Override
    public void getCellInfoListOtdoaResponse(RadioResponseInfo radioResponseInfo, ArrayList<CellInfo> arrayList) {
        //   Huawei
    }

    @Override
    public void getCurrentImsCallsResponse(RadioResponseInfo radioResponseInfo, ArrayList<RILImsCall> arrayList) {
        // Huawei
        ArrayList<String> calls = new ArrayList<>(arrayList.size());
        for (RILImsCall call : arrayList) {
            Log.d(LOG_TAG, "calls list contains " + redactCall(call));
            HwImsCallSession session = HwImsCallSession.awaitingIdFromRIL.get(call.number);
            if (session != null) {
                session.addIdFromRIL(call, call.number);
            }
            session = HwImsCallSession.calls.get(call.number);
            if (session == null) {
                if (call.isMT > 0) {
                    Log.d(LOG_TAG, "Notifying MmTelFeature incoming call! " + redactCall(call));
                    // An incoming call that we have never seen before, tell the framework.
                } else {
                    Log.e(LOG_TAG, "Phantom Call!!!! " + redactCall(call));
                    HwImsCallSession.calls.forEach((s, hwImsCallSession) -> Rlog.d(LOG_TAG, "Phantom debugging got call in static calls " + redactCall(hwImsCallSession.rilImsCall) + " with number " + s));
                    HwImsCallSession.awaitingIdFromRIL.forEach((s, hwImsCallSession) -> Rlog.d(LOG_TAG, "Phantom debugging got call in static awaiting " + redactCall(hwImsCallSession.rilImsCall) + " with number " + s));
                    // Someone has been talking to AT... naughty.
                }
                Bundle extras = new Bundle();
                HwImsCallSession callSession = new HwImsCallSession(mSlotId, new ImsCallProfile(), call);
                extras.putInt(ImsManager.EXTRA_PHONE_ID, mSlotId);
                extras.putString(ImsManager.EXTRA_CALL_ID, callSession.getCallId());
                extras.putBoolean(ImsManager.EXTRA_IS_UNKNOWN_CALL, call.isMT == 0); // A new outgoing call should never happen. Someone is playing with AT commands or talking to the modem.
                HwImsService.getInstance().createMmTelFeature(mSlotId).notifyIncomingCall(callSession, extras);


            } else {
                // Existing call, update it's data.
                session.updateCall(call);
            }
            if (call.isMpty > 0 && call.state == 2) { // Dialing & Multiparty
                // It is a new conference call being added.
                for (HwImsCallSession confSession : HwImsCallSession.calls.values()) {
                    if (confSession.confInProgress) {
                        Rlog.d(LOG_TAG, "adding call " + call.index + " to conference " + confSession.getCallId());
                        confSession.notifyConfDone(call);
                        break;
                    }
                }
            }
            calls.add(call.number);
        }
        Rlog.v(LOG_TAG, "active calls is " + calls.toString());
        for (Map.Entry<String, HwImsCallSession> call : HwImsCallSession.calls.entrySet()) {
            if (!calls.contains(call.getKey())) {
                try {
                    Rlog.d(LOG_TAG, "notifying dead call " + redactCall(call.getValue().rilImsCall));
                    call.getValue().notifyEnded();
                } catch (RuntimeException e) {
                    Rlog.e(LOG_TAG, "error notifying dead call!", e);
                }
            }
        }
    }

    private String redactCall(RILImsCall call) {
        return "{.state = " + call.state + ", .index = " + call.index + ", .toa = " + call.toa + ", .isMpty = " + call.isMpty + ", .isMT = " + call.isMT + ", .als = " + call.als + ", .isVoice = " + call.isVoice + ", .isVoicePrivacy = " + call.isVoicePrivacy + ", .number = " + call.number + ", .numberPresentation = " + call.numberPresentation + ", .name = " + call.name + ", .namePresentation = " + call.namePresentation + ", .callDetails = " + call.callDetails.toString() + ", .isEConference = " + call.isECOnference + ", .peerVideoSupport = " + call.peerVideoSupport + "}";
    }

    @Override
    public void getDeviceVersionResponse(RadioResponseInfo radioResponseInfo, RILDeviceVersionResponse rilDeviceVersionResponse) {
        // Huawei
    }

    @Override
    public void getDsFlowInfoResponse(RadioResponseInfo radioResponseInfo, RILDsFlowInfoResponse rilDsFlowInfoResponse) {
        // Huawei
    }

    @Override
    public void getPolListResponse(RadioResponseInfo radioResponseInfo, RILPreferredPLMNSelector rilPreferredPLMNSelector) {
        // Huawei
    }

    @Override
    public void getSystemInfoExResponse(RadioResponseInfo radioResponseInfo, RILRADIOSYSINFO rilradiosysinfo) {
        // Huawei
    }

    @Override
    public void manualSelectionCsgIdResponse(RadioResponseInfo radioResponseInfo) {
        // Huawei
    }

    @Override
    public void setupDataCallEmergencyResponse(RadioResponseInfo radioResponseInfo, SetupDataCallResult setupDataCallResult) {
        // Huawei
    }

    @Override
    public void uiccAuthResponse(RadioResponseInfo radioResponseInfo, RILUICCAUTHRESPONSE riluiccauthresponse) {
        // Huawei
    }

    // END OF HUAWEI METHODS

    @Override
    public void acceptCallResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void acknowledgeIncomingGsmSmsWithPduResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void acknowledgeLastIncomingCdmaSmsResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void acknowledgeLastIncomingGsmSmsResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void acknowledgeRequest(int i) {

    }

    @Override
    public void cancelPendingUssdResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void changeIccPin2ForAppResponse(RadioResponseInfo radioResponseInfo, int i) {

    }

    @Override
    public void changeIccPinForAppResponse(RadioResponseInfo radioResponseInfo, int i) {

    }

    @Override
    public void conferenceResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void deactivateDataCallResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void deleteSmsOnRuimResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void deleteSmsOnSimResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void dialResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void exitEmergencyCallbackModeResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void explicitCallTransferResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void getAllowedCarriersResponse(RadioResponseInfo radioResponseInfo, boolean b, CarrierRestrictions carrierRestrictions) {

    }

    @Override
    public void getAvailableBandModesResponse(RadioResponseInfo radioResponseInfo, ArrayList<Integer> arrayList) {

    }

    @Override
    public void getAvailableNetworksResponse(RadioResponseInfo radioResponseInfo, ArrayList<OperatorInfo> arrayList) {

    }

    @Override
    public void getBasebandVersionResponse(RadioResponseInfo radioResponseInfo, String s) {

    }

    @Override
    public void getCDMASubscriptionResponse(RadioResponseInfo radioResponseInfo, String s, String s1, String s2, String s3, String s4) {

    }

    @Override
    public void getCallForwardStatusResponse(RadioResponseInfo radioResponseInfo, ArrayList<CallForwardInfo> arrayList) {

    }

    @Override
    public void getCallWaitingResponse(RadioResponseInfo radioResponseInfo, boolean b, int i) {

    }

    @Override
    public void getCdmaBroadcastConfigResponse(RadioResponseInfo radioResponseInfo, ArrayList<CdmaBroadcastSmsConfigInfo> arrayList) {

    }

    @Override
    public void getCdmaRoamingPreferenceResponse(RadioResponseInfo radioResponseInfo, int i) {

    }

    @Override
    public void getCdmaSubscriptionSourceResponse(RadioResponseInfo radioResponseInfo, int i) {

    }

    @Override
    public void getCellInfoListResponse(RadioResponseInfo radioResponseInfo, ArrayList<CellInfo> arrayList) {

    }

    @Override
    public void getClipResponse(RadioResponseInfo radioResponseInfo, int i) {

    }

    @Override
    public void getClirResponse(RadioResponseInfo radioResponseInfo, int i, int i1) {

    }

    @Override
    public void getCurrentCallsResponse(RadioResponseInfo radioResponseInfo, ArrayList<Call> arrayList) {

    }

    @Override
    public void getDataCallListResponse(RadioResponseInfo radioResponseInfo, ArrayList<SetupDataCallResult> arrayList) {

    }

    @Override
    public void getDataRegistrationStateResponse(RadioResponseInfo radioResponseInfo, DataRegStateResult dataRegStateResult) {

    }

    @Override
    public void getDeviceIdentityResponse(RadioResponseInfo radioResponseInfo, String s, String s1, String s2, String s3) {

    }

    @Override
    public void getFacilityLockForAppResponse(RadioResponseInfo radioResponseInfo, int i) {

    }

    @Override
    public void getGsmBroadcastConfigResponse(RadioResponseInfo radioResponseInfo, ArrayList<GsmBroadcastSmsConfigInfo> arrayList) {

    }

    @Override
    public void getHardwareConfigResponse(RadioResponseInfo radioResponseInfo, ArrayList<HardwareConfig> arrayList) {

    }

    @Override
    public void getIMSIForAppResponse(RadioResponseInfo radioResponseInfo, String s) {

    }

    @Override
    public void getIccCardStatusResponse(RadioResponseInfo radioResponseInfo, CardStatus cardStatus) {

    }

    @Override
    public void getImsRegistrationStateResponse(RadioResponseInfo radioResponseInfo, boolean b, int i) {

    }

    @Override
    public void getLastCallFailCauseResponse(RadioResponseInfo radioResponseInfo, LastCallFailCauseInfo lastCallFailCauseInfo) {

    }

    @Override
    public void getModemActivityInfoResponse(RadioResponseInfo radioResponseInfo, ActivityStatsInfo activityStatsInfo) {

    }

    @Override
    public void getMuteResponse(RadioResponseInfo radioResponseInfo, boolean b) {

    }

    @Override
    public void getNeighboringCidsResponse(RadioResponseInfo radioResponseInfo, ArrayList<NeighboringCell> arrayList) {

    }

    @Override
    public void getNetworkSelectionModeResponse(RadioResponseInfo radioResponseInfo, boolean b) {

    }

    @Override
    public void getOperatorResponse(RadioResponseInfo radioResponseInfo, String s, String s1, String s2) {

    }

    @Override
    public void getPreferredNetworkTypeResponse(RadioResponseInfo radioResponseInfo, int i) {

    }

    @Override
    public void getPreferredVoicePrivacyResponse(RadioResponseInfo radioResponseInfo, boolean b) {

    }

    @Override
    public void getRadioCapabilityResponse(RadioResponseInfo radioResponseInfo, RadioCapability radioCapability) {

    }

    @Override
    public void getSignalStrengthResponse(RadioResponseInfo radioResponseInfo, SignalStrength signalStrength) {

    }

    @Override
    public void getSmscAddressResponse(RadioResponseInfo radioResponseInfo, String s) {

    }

    @Override
    public void getTTYModeResponse(RadioResponseInfo radioResponseInfo, int i) {

    }

    @Override
    public void getVoiceRadioTechnologyResponse(RadioResponseInfo radioResponseInfo, int i) {

    }

    @Override
    public void getVoiceRegistrationStateResponse(RadioResponseInfo radioResponseInfo, VoiceRegStateResult voiceRegStateResult) {

    }

    @Override
    public void handleStkCallSetupRequestFromSimResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void hangupConnectionResponse(RadioResponseInfo radioResponseInfo) {
        RspMsg(radioResponseInfo, -1, null);
    }

    @Override
    public void hangupForegroundResumeBackgroundResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void hangupWaitingOrBackgroundResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void iccCloseLogicalChannelResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void iccIOForAppResponse(RadioResponseInfo radioResponseInfo, IccIoResult iccIoResult) {

    }

    @Override
    public void iccOpenLogicalChannelResponse(RadioResponseInfo radioResponseInfo, int i, ArrayList<Byte> arrayList) {

    }

    @Override
    public void iccTransmitApduBasicChannelResponse(RadioResponseInfo radioResponseInfo, IccIoResult iccIoResult) {

    }

    @Override
    public void iccTransmitApduLogicalChannelResponse(RadioResponseInfo radioResponseInfo, IccIoResult iccIoResult) {

    }

    @Override
    public void nvReadItemResponse(RadioResponseInfo radioResponseInfo, String s) {

    }

    @Override
    public void nvResetConfigResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void nvWriteCdmaPrlResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void nvWriteItemResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void pullLceDataResponse(RadioResponseInfo radioResponseInfo, LceDataInfo lceDataInfo) {

    }

    @Override
    public void rejectCallResponse(RadioResponseInfo radioResponseInfo) {
        RspMsg(radioResponseInfo, -1, null);
    }

    @Override
    public void reportSmsMemoryStatusResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void reportStkServiceIsRunningResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void requestIccSimAuthenticationResponse(RadioResponseInfo radioResponseInfo, IccIoResult iccIoResult) {

    }

    @Override
    public void requestIsimAuthenticationResponse(RadioResponseInfo radioResponseInfo, String s) {

    }

    @Override
    public void requestShutdownResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void sendBurstDtmfResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void sendCDMAFeatureCodeResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void sendCdmaSmsResponse(RadioResponseInfo radioResponseInfo, SendSmsResult sendSmsResult) {

    }

    @Override
    public void sendDeviceStateResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void sendDtmfResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void sendEnvelopeResponse(RadioResponseInfo radioResponseInfo, String s) {

    }

    @Override
    public void sendEnvelopeWithStatusResponse(RadioResponseInfo radioResponseInfo, IccIoResult iccIoResult) {

    }

    @Override
    public void sendImsSmsResponse(RadioResponseInfo radioResponseInfo, SendSmsResult sendSmsResult) {

    }

    @Override
    public void sendSMSExpectMoreResponse(RadioResponseInfo radioResponseInfo, SendSmsResult sendSmsResult) {

    }

    @Override
    public void sendSmsResponse(RadioResponseInfo radioResponseInfo, SendSmsResult sendSmsResult) {

    }

    @Override
    public void sendTerminalResponseToSimResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void sendUssdResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void separateConnectionResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setAllowedCarriersResponse(RadioResponseInfo radioResponseInfo, int i) {

    }

    @Override
    public void setBandModeResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setBarringPasswordResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setCallForwardResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setCallWaitingResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setCdmaBroadcastActivationResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setCdmaBroadcastConfigResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setCdmaRoamingPreferenceResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setCdmaSubscriptionSourceResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setCellInfoListRateResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setClirResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setDataAllowedResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setDataProfileResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setFacilityLockForAppResponse(RadioResponseInfo radioResponseInfo, int i) {

    }

    @Override
    public void setGsmBroadcastActivationResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setGsmBroadcastConfigResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setIndicationFilterResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setInitialAttachApnResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setLocationUpdatesResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setMuteResponse(RadioResponseInfo radioResponseInfo) {
        RspMsg(radioResponseInfo, -1, null);
    }

    @Override
    public void setNetworkSelectionModeAutomaticResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setNetworkSelectionModeManualResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setPreferredNetworkTypeResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setPreferredVoicePrivacyResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setRadioCapabilityResponse(RadioResponseInfo radioResponseInfo, RadioCapability radioCapability) {

    }

    @Override
    public void setRadioPowerResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setSimCardPowerResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setSmscAddressResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setSuppServiceNotificationsResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setTTYModeResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setUiccSubscriptionResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void setupDataCallResponse(RadioResponseInfo radioResponseInfo, SetupDataCallResult setupDataCallResult) {

    }

    @Override
    public void startDtmfResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void startLceServiceResponse(RadioResponseInfo radioResponseInfo, LceStatusInfo lceStatusInfo) {

    }

    @Override
    public void stopDtmfResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void stopLceServiceResponse(RadioResponseInfo radioResponseInfo, LceStatusInfo lceStatusInfo) {

    }

    @Override
    public void supplyIccPin2ForAppResponse(RadioResponseInfo radioResponseInfo, int i) {

    }

    @Override
    public void supplyIccPinForAppResponse(RadioResponseInfo radioResponseInfo, int i) {

    }

    @Override
    public void supplyIccPuk2ForAppResponse(RadioResponseInfo radioResponseInfo, int i) {

    }

    @Override
    public void supplyIccPukForAppResponse(RadioResponseInfo radioResponseInfo, int i) {

    }

    @Override
    public void supplyNetworkDepersonalizationResponse(RadioResponseInfo radioResponseInfo, int i) {

    }

    @Override
    public void switchWaitingOrHoldingAndActiveResponse(RadioResponseInfo radioResponseInfo) {

    }

    @Override
    public void writeSmsToRuimResponse(RadioResponseInfo radioResponseInfo, int i) {

    }

    @Override
    public void writeSmsToSimResponse(RadioResponseInfo radioResponseInfo, int i) {

    }

    public enum RESP_CODE {
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
        public int value;

        RESP_CODE(int value) {
            this.value = value;
        }

        public static String getName(int code) {
            Optional<RESP_CODE> x = Arrays.stream(RESP_CODE.values()).filter(resp_code -> resp_code.value == code).findAny();
            if (x.isPresent()) {
                return x.get().name();
            } else {
                return Integer.toString(code);
            }
        }

    }


}
