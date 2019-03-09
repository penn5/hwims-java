package com.hwims;

import android.os.RemoteException;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.feature.CapabilityChangeRequest;
import android.telephony.ims.feature.MmTelFeature;
import android.telephony.ims.stub.ImsRegistrationImplBase;
import android.util.Log;
import android.util.SparseArray;

public class HwMmTelFeature extends MmTelFeature {

    private static HwMmTelFeature[] instances = {null, null, null};
    private int mSlotId;
    private boolean mIsReady = false;
    // Enabled Capabilities - not status
    private SparseArray<MmTelCapabilities> mEnabledCapabilities = new SparseArray<>();
    private final String LOG_TAG = "HwImsMmTelFeatureImpl";





    private HwMmTelFeature() {} // Use getInstance.
    private HwMmTelFeature(int slotId) {
        mEnabledCapabilities.append(ImsRegistrationImplBase.REGISTRATION_TECH_LTE,
                new MmTelCapabilities());
        // One day... :hearteyes:
        //mEnabledCapabilities.append(ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN,
        //        new MmTelCapabilities());
        setFeatureState(STATE_INITIALIZING);
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

    @Override
    public ImsCallProfile createCallProfile(int callSessionType, int callType) {
        if (callSessionType == ImsCallProfile.SERVICE_TYPE_EMERGENCY) {
            return null;
        }
        if (callSessionType == ImsCallProfile.SERVICE_TYPE_NONE) {
            // Register IMS
            try {
                RilHolder.INSTANCE.getRadio(mSlotId).imsRegister(RilHolder.callback(() -> Log.e(LOG_TAG, "CALLBACK CALLED!!!")));
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "error registering ims", e);
                return null;
            }
        }
        return new ImsCallProfile(callSessionType, callType);
        // Is this right?
    }

    @Override
    public void onFeatureRemoved() {
	mIsReady = false;
        super.onFeatureRemoved();
    }

    @Override
    public void onFeatureReady() {
        mIsReady = true;
    }

}
