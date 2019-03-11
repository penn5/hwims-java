package com.huawei.ims;

import android.hardware.radio.V1_0.CdmaCallWaiting;
import android.hardware.radio.V1_0.CdmaInformationRecords;
import android.hardware.radio.V1_0.CdmaSignalInfoRecord;
import android.hardware.radio.V1_0.CdmaSmsMessage;
import android.hardware.radio.V1_0.CellInfo;
import android.hardware.radio.V1_0.HardwareConfig;
import android.hardware.radio.V1_0.LceDataInfo;
import android.hardware.radio.V1_0.PcoDataInfo;
import android.hardware.radio.V1_0.RadioCapability;
import android.hardware.radio.V1_0.SetupDataCallResult;
import android.hardware.radio.V1_0.SignalStrength;
import android.hardware.radio.V1_0.SimRefreshResult;
import android.hardware.radio.V1_0.StkCcUnsolSsResult;
import android.hardware.radio.V1_0.SuppSvcNotification;
import android.util.Log;

import java.util.ArrayList;

import vendor.huawei.hardware.radio.V1_0.IRadioIndication;
import vendor.huawei.hardware.radio.V1_0.RILAPDsFlowInfoReport;
import vendor.huawei.hardware.radio.V1_0.RILImsCallModify;
import vendor.huawei.hardware.radio.V1_0.RILImsHandover;
import vendor.huawei.hardware.radio.V1_0.RILImsModifyEndCause;
import vendor.huawei.hardware.radio.V1_0.RILImsMtStatusReport;
import vendor.huawei.hardware.radio.V1_0.RILImsSrvstatusList;
import vendor.huawei.hardware.radio.V1_0.RILImsSuppSvcNotification;
import vendor.huawei.hardware.radio.V1_0.RILUnsolMsgPayload;
import vendor.huawei.hardware.radio.V1_0.RILVsimOtaSmsResponse;
import vendor.huawei.hardware.radio.V1_0.RILVtFlowInfoReport;

public class HwImsRadioIndication extends IRadioIndication.Stub {

    private static final String LOG_TAG = "HwImsRadioIndication";

    @Override
    public void UnsolMsg(int indicationType, int msgId, RILUnsolMsgPayload rilUnsolMsgPayload) {
        Log.e(LOG_TAG, "indicationType = " + indicationType + ", msgId = " + msgId);
        // Huawei
    }

    @Override
    public void apDsFlowInfoReport(int indicationType, RILAPDsFlowInfoReport rapDsFlowInfoReport) {
        // Huawei not needed
    }

    @Override
    public void dsFlowInfoReport(int indicationType, RILAPDsFlowInfoReport apDsFlowInfoReport) {
        // Huawei not needed
    }

    @Override
    public void imsCallModifyEndCauseInd(int type, RILImsModifyEndCause cause) {
        // Huawei
    }

    @Override
    public void imsCallModifyInd(int type, RILImsCallModify modify) {
        // Huawei
    }

    @Override
    public void imsCallMtStatusInd(int type, RILImsMtStatusReport imsCallMtStatus) {
        // Huawei
    }

    @Override
    public void imsHandoverInd(int indicationType, RILImsHandover imsHandover) {
        // Huawei
    }

    @Override
    public void imsSrvStatusInd(int type, RILImsSrvstatusList imsSrvStatus) {
        // Huawei
    }

    @Override
    public void imsSuppSrvInd(int type, RILImsSuppSvcNotification idsSuppSacNotification) {
        // Huawei
    }

    @Override
    public void imsaToVowifiMsg(int indicationType, ArrayList<Byte> msgs) {
        // Huawei
    }

    @Override
    public void vsimOtaSmsReport(int indicationType, RILVsimOtaSmsResponse vsimOtaSms) {
        // Huawei
    }

    @Override
    public void vtFlowInfoReport(int indicationType, RILVtFlowInfoReport rilVtFlowInfoReport) {
        // Huawei not needed
    }


    // END OF HUAWEI METHODS


    @Override
    public void callRing(int i, boolean b, CdmaSignalInfoRecord cdmaSignalInfoRecord) {

    }

    @Override
    public void callStateChanged(int i) {

    }

    @Override
    public void cdmaCallWaiting(int i, CdmaCallWaiting cdmaCallWaiting) {

    }

    @Override
    public void cdmaInfoRec(int i, CdmaInformationRecords cdmaInformationRecords) {

    }

    @Override
    public void cdmaNewSms(int i, CdmaSmsMessage cdmaSmsMessage) {

    }

    @Override
    public void cdmaOtaProvisionStatus(int i, int i1) {

    }

    @Override
    public void cdmaPrlChanged(int i, int i1) {

    }

    @Override
    public void cdmaRuimSmsStorageFull(int i) {

    }

    @Override
    public void cdmaSubscriptionSourceChanged(int i, int i1) {

    }

    @Override
    public void cellInfoList(int i, ArrayList<CellInfo> arrayList) {

    }

    @Override
    public void currentSignalStrength(int i, SignalStrength signalStrength) {

    }

    @Override
    public void dataCallListChanged(int i, ArrayList<SetupDataCallResult> arrayList) {

    }

    @Override
    public void enterEmergencyCallbackMode(int i) {

    }

    @Override
    public void exitEmergencyCallbackMode(int i) {

    }

    @Override
    public void hardwareConfigChanged(int i, ArrayList<HardwareConfig> arrayList) {

    }

    @Override
    public void imsNetworkStateChanged(int i) {
        // AOSP IMS
    }

    @Override
    public void indicateRingbackTone(int i, boolean b) {

    }

    @Override
    public void lceData(int i, LceDataInfo lceDataInfo) {

    }

    @Override
    public void modemReset(int i, String s) {

    }

    @Override
    public void networkStateChanged(int i) {

    }

    @Override
    public void newBroadcastSms(int i, ArrayList<Byte> arrayList) {

    }

    @Override
    public void newSms(int i, ArrayList<Byte> arrayList) {

    }

    @Override
    public void newSmsOnSim(int i, int i1) {

    }

    @Override
    public void newSmsStatusReport(int i, ArrayList<Byte> arrayList) {

    }

    @Override
    public void nitzTimeReceived(int i, String s, long l) {

    }

    @Override
    public void onSupplementaryServiceIndication(int i, StkCcUnsolSsResult stkCcUnsolSsResult) {

    }

    @Override
    public void onUssd(int i, int i1, String s) {

    }

    @Override
    public void pcoData(int i, PcoDataInfo pcoDataInfo) {

    }

    @Override
    public void radioCapabilityIndication(int i, RadioCapability radioCapability) {

    }

    @Override
    public void radioStateChanged(int i, int i1) {

    }

    @Override
    public void resendIncallMute(int i) {

    }

    @Override
    public void restrictedStateChanged(int i, int i1) {

    }

    @Override
    public void rilConnected(int i) {

    }

    @Override
    public void simRefresh(int i, SimRefreshResult simRefreshResult) {

    }

    @Override
    public void simSmsStorageFull(int i) {

    }

    @Override
    public void simStatusChanged(int i) {

    }

    @Override
    public void srvccStateNotify(int i, int i1) {

    }

    @Override
    public void stkCallControlAlphaNotify(int i, String s) {

    }

    @Override
    public void stkCallSetup(int i, long l) {

    }

    @Override
    public void stkEventNotify(int i, String s) {

    }

    @Override
    public void stkProactiveCommand(int i, String s) {

    }

    @Override
    public void stkSessionEnd(int i) {

    }

    @Override
    public void subscriptionStatusChanged(int i, boolean b) {

    }

    @Override
    public void suppSvcNotify(int i, SuppSvcNotification suppSvcNotification) {

    }

    @Override
    public void voiceRadioTechChanged(int i, int i1) {

    }
}
