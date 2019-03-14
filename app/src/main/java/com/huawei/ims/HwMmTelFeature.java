package com.huawei.ims;

import android.annotation.NonNull;
import android.os.RemoteException;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.feature.CapabilityChangeRequest;
import android.telephony.ims.feature.MmTelFeature;
import android.telephony.ims.stub.ImsCallSessionImplBase;
import android.telephony.ims.stub.ImsRegistrationImplBase;
import android.util.Log;
import android.util.SparseArray;

public class HwMmTelFeature extends MmTelFeature {

    private static final HwMmTelFeature[] instances = {null, null, null};
    private final String LOG_TAG = "HwImsMmTelFeatureImpl";
    // Enabled Capabilities - not status
    private final SparseArray<MmTelCapabilities> mEnabledCapabilities = new SparseArray<>();
    private final int mSlotId;

    private HwMmTelFeature(int slotId) { // Use getInstance(slotId)
        mSlotId = slotId;
        mEnabledCapabilities.append(ImsRegistrationImplBase.REGISTRATION_TECH_LTE,
                new MmTelFeature.MmTelCapabilities(MmTelCapabilities.CAPABILITY_TYPE_VOICE));
        // One day... :hearteyes:
        mEnabledCapabilities.append(ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN,
                new MmTelCapabilities(0));
        setFeatureState(STATE_READY);
    }

    public static HwMmTelFeature getInstance(int slotId) {
        if (instances[slotId] == null) {
            instances[slotId] = new HwMmTelFeature(slotId);
        }
        return instances[slotId];
    }


    @Override
    public boolean queryCapabilityConfiguration(int capability, int radioTech) {
        return mEnabledCapabilities.get(radioTech).isCapable(capability);
    }

    @Override
    public void changeEnabledCapabilities(CapabilityChangeRequest request,
                                          CapabilityCallbackProxy c) {
        for (CapabilityChangeRequest.CapabilityPair pair : request.getCapabilitiesToEnable()) {
            mEnabledCapabilities.get(pair.getRadioTech()).addCapabilities(pair.getCapability());
        }
        for (CapabilityChangeRequest.CapabilityPair pair : request.getCapabilitiesToDisable()) {
            mEnabledCapabilities.get(pair.getRadioTech()).removeCapabilities(pair.getCapability());
        }
    }

    private void registerImsInner() {
        try {
            RilHolder.INSTANCE.getRadio(mSlotId).imsRegister(RilHolder.callback((radioResponseInfo, rspMsgPayload) -> {
                Log.e(LOG_TAG, "CALLBACK CALLED!!!" + radioResponseInfo + rspMsgPayload);
                if (radioResponseInfo.error != 0) {
                    Log.e(LOG_TAG, "radiorespinfo gives error " + radioResponseInfo.error);
                    HwImsService.getInstance().getRegistration(mSlotId).onDeregistered(new ImsReasonInfo(ImsReasonInfo.CODE_UNSPECIFIED, radioResponseInfo.error, radioResponseInfo.toString() + rspMsgPayload.toString()));
                    throw new RuntimeException();
                } else {
                    MmTelCapabilities capabilities = new MmTelCapabilities();
                    capabilities.addCapabilities(MmTelCapabilities.CAPABILITY_TYPE_VOICE);
                    notifyCapabilitiesStatusChanged(capabilities);
                    HwImsService.getInstance().getRegistration(mSlotId).onRegistered(HwImsRegistration.REGISTRATION_TECH_LTE);
                }
            }, mSlotId));
        } catch (RemoteException e) {
            HwImsService.getInstance().getRegistration(mSlotId).onDeregistered(new ImsReasonInfo());
            Log.e(LOG_TAG, "error registering ims", e);
        }
    }

    public void registerIms() {
        HwImsService.getInstance().getRegistration(mSlotId).onRegistering(HwImsRegistration.REGISTRATION_TECH_LTE);
        try {
            RilHolder.INSTANCE.getRadio(mSlotId).setImsSwitch(RilHolder.callback((radioResponseInfo, rspMsgPayload) -> {
                Log.e(LOG_TAG, "Got resp from setImsSwitch");
                if (radioResponseInfo.error != 0) {
                    HwImsService.getInstance().getRegistration(mSlotId).onDeregistered(new ImsReasonInfo());
                } else {
                    registerImsInner();
                }

            }, mSlotId), 1);
        } catch (RemoteException e) {
            // This happens when it's already set to 1. Continue to register.
            Log.w(LOG_TAG, "Failed to set ims switch. Continue");
            registerImsInner();
        }


    }

    public void unregisterIms() {
        try {
            RilHolder.INSTANCE.getRadio(mSlotId).setImsSwitch(RilHolder.callback((radioResponseInfo, rspMsgPayload) -> {
                Log.e(LOG_TAG, "Got resp from setImsSwitch");
                if (radioResponseInfo.error != 0) {
                    // What can we do?
                    Log.e(LOG_TAG, "Failed to unregister imsswitch");
                }

            }, mSlotId), 0);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Failed to setImsSwitch to unregister", e);
        }
    }

    @Override
    public ImsCallProfile createCallProfile(int callSessionType, int callType) {
        if (callSessionType == ImsCallProfile.SERVICE_TYPE_EMERGENCY) {
            return null;
        }
        if (callSessionType == ImsCallProfile.SERVICE_TYPE_NONE) {
            // Register IMS
            registerIms();
        }
        return new ImsCallProfile(callSessionType, callType);
        // Is this right?
    }

    @Override
    public synchronized ImsCallSessionImplBase createCallSession(@NonNull ImsCallProfile profile) {
        Log.e(LOG_TAG, "Creating HwImsCallSession");
        return new HwImsCallSession(mSlotId, profile);
    }

    @Override
    public void onFeatureRemoved() {
        super.onFeatureRemoved();
    }

    @Override
    public void onFeatureReady() {
        registerIms();
    }

}
