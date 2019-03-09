package com.hwims;

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

import java.util.ArrayList;

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
    @Override
    public void RspMsg(RadioResponseInfo radioResponseInfo, int i, RspMsgPayload rspMsgPayload) {
        // Huawei
    }

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
        // Huawei
    }

    @Override
    public void getCurrentImsCallsResponse(RadioResponseInfo radioResponseInfo, ArrayList<RILImsCall> arrayList) {
        // Huawei
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

    // END OF HUAWEI METHODS


}
