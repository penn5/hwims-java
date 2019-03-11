package com.huawei.ims;

import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.Rlog;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsCallSessionListener;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.stub.ImsCallSessionImplBase;
import android.util.Log;

import java.util.Objects;

import vendor.huawei.hardware.radio.V1_0.RILImsCallType;
import vendor.huawei.hardware.radio.V1_0.RILImsDial;

import static android.telephony.ims.ImsCallProfile.CALL_TYPE_VIDEO_N_VOICE;
import static android.telephony.ims.ImsCallProfile.CALL_TYPE_VOICE;
import static android.telephony.ims.ImsCallProfile.CALL_TYPE_VOICE_N_VIDEO;
import static android.telephony.ims.ImsCallProfile.CALL_TYPE_VT;
import static android.telephony.ims.ImsCallProfile.CALL_TYPE_VT_NODIR;
import static android.telephony.ims.ImsCallProfile.CALL_TYPE_VT_RX;
import static android.telephony.ims.ImsCallProfile.CALL_TYPE_VT_TX;

public class HwImsCallSession extends ImsCallSessionImplBase {
    private static final String LOG_TAG = "HwImsCallSession";
    private static int instanceCount = 0;
    private int mSlotId;
    private ImsCallProfile mProfile;
    private ImsCallProfile mLocalProfile;
    private ImsCallProfile mRemoteProfile;
    private ImsCallSessionListener listener;
    private int instance;

    private int mState = State.INVALID;
    private boolean mInCall = false;

    public HwImsCallSession(int slotId, ImsCallProfile profile) {
        this.mSlotId = slotId;
        this.mProfile = new ImsCallProfile();
        this.mLocalProfile = new ImsCallProfile(profile.getServiceType(), profile.getCallType());
        this.mRemoteProfile = new ImsCallProfile(profile.getServiceType(), profile.getCallType());
        this.instance = instanceCount++;
    }

    @Override
    public void setListener(ImsCallSessionListener listener) {
        this.listener = listener;
    }

    @Override
    public String getCallId() {
        return "slot" + mSlotId + "instance" + instance;
    }

    @Override
    public ImsCallProfile getCallProfile() {
        return mProfile;
    }

    @Override
    public ImsCallProfile getRemoteCallProfile() {
        return mRemoteProfile;
    }

    @Override
    public ImsCallProfile getLocalCallProfile() {
        return mLocalProfile;
    }

    @Override
    public String getProperty(String name) {
        return null; // Right now there are no "properties" what are they even for?
    }

    @Override
    public int getState() {
        return mState;
    }

    @Override
    public boolean isInCall() {
        return mInCall;
    }

    @Override
    public void setMute(boolean muted) {
        try {
            int serial = RilHolder.prepareBlock(mSlotId);
            RilHolder.INSTANCE.getRadio(mSlotId).setMute(serial, muted);
            if (Objects.requireNonNull(RilHolder.blockUntilComplete(serial), "internal programing error in blockUntilComplete").error != 0) {
                Rlog.e(LOG_TAG, "Failed to setMute! " + RilHolder.blockUntilComplete(serial));
            }
        } catch (RemoteException e) {
            Rlog.e(LOG_TAG, "Error sending setMute request!", e);
        }
    }

    @Override
    public void start(String callee, ImsCallProfile profile) {
        RILImsDial callInfo = new RILImsDial();
        callInfo.address = callee;
        callInfo.clir = profile.getCallExtraInt("oir"); // Huawei do this so it **must** be right... Oh wait...
        Bundle extras = profile.mCallExtras.getBundle("OemCallExtras");
        if (extras != null) {
            Rlog.e(LOG_TAG, "NI reading oemcallextras, it is " + extras.toString());
        }
        int callType;
        switch (profile.getCallType()) {
            case CALL_TYPE_VOICE_N_VIDEO:
                callType = 0; //TODO: FIXME: ITS NOT FINSIHED AND WONT WORK!!!
                break;
            case CALL_TYPE_VOICE:
                callType = RILImsCallType.CALL_TYPE_VOICE;
                break;
            case CALL_TYPE_VIDEO_N_VOICE:
                Rlog.e(LOG_TAG, "Unsupported calltype!!!");
                listener.callSessionInitiatedFailed(new ImsReasonInfo(ImsReasonInfo.CODE_LOCAL_INTERNAL_ERROR, ImsReasonInfo.CODE_UNSPECIFIED, "Unsupported CallType 3"));
            case CALL_TYPE_VT:
                callType = 0x3;
                break;
            case CALL_TYPE_VT_TX:
                callType = 0x1;
                break;
            case CALL_TYPE_VT_RX:
                callType = 0x2;
                break;
            case CALL_TYPE_VT_NODIR:
                callType = 0x4;
            default:
                throw new RuntimeException(); //TODO
        }
        callInfo.callDetails.callType = callType;
        callInfo.callDetails.callDomain = 3; // From HwIms.
        try {
            RilHolder.INSTANCE.getRadio(mSlotId).imsDial(RilHolder.callback((radioResponseInfo, rspMsgPayload) -> {
                if (radioResponseInfo.error == 0) {
                    Rlog.e(LOG_TAG, "MADE AN IMS CALL OMG WOW");
                    Log.e(LOG_TAG, "MADE AN IMS CALL OMG WOW");
                }
            }, mSlotId), callInfo);
        } catch (RemoteException e) {
            Rlog.e(LOG_TAG, "Sending imsDial failed with exception", e);
        }
    }


}
