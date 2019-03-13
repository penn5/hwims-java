package com.huawei.ims;

import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.Rlog;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsCallSessionListener;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsStreamMediaProfile;
import android.telephony.ims.stub.ImsCallSessionImplBase;
import android.util.Log;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import vendor.huawei.hardware.radio.V1_0.RILImsCall;
import vendor.huawei.hardware.radio.V1_0.RILImsCallDomain;
import vendor.huawei.hardware.radio.V1_0.RILImsCallType;
import vendor.huawei.hardware.radio.V1_0.RILImsDial;

import static android.telephony.ims.ImsCallProfile.CALL_TYPE_VIDEO_N_VOICE;
import static android.telephony.ims.ImsCallProfile.CALL_TYPE_VOICE;
import static android.telephony.ims.ImsCallProfile.CALL_TYPE_VOICE_N_VIDEO;
import static android.telephony.ims.ImsCallProfile.CALL_TYPE_VS;
import static android.telephony.ims.ImsCallProfile.CALL_TYPE_VS_RX;
import static android.telephony.ims.ImsCallProfile.CALL_TYPE_VS_TX;
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

    private int mState = State.INVALID;
    private boolean mInCall = false;

    public static ConcurrentHashMap<String, HwImsCallSession> awaitingIdFromRIL = new ConcurrentHashMap<String, HwImsCallSession>();
    public static ConcurrentHashMap<String, HwImsCallSession> calls = new ConcurrentHashMap<>();

    private final Object mCallIdLock = new Object();

    public boolean confInProgress = false;
    private RILImsCall rilImsCall;

    // For outgoing (MO) calls
    public HwImsCallSession(int slotId, ImsCallProfile profile) {
        this.mSlotId = slotId;
        this.mProfile = new ImsCallProfile();
        this.mLocalProfile = new ImsCallProfile(profile.getServiceType(), profile.getCallType());
        this.mRemoteProfile = new ImsCallProfile(profile.getServiceType(), profile.getCallType());
    }

    // For incoming (MT) calls
    public HwImsCallSession(int slotId, ImsCallProfile profile, RILImsCall call) {
        this(slotId, profile);
        this.rilImsCall = call;
        calls.put(call.number, this);
    }

    public void addIdFromRIL(RILImsCall call, String number) {
        if (awaitingIdFromRIL.remove(number, this)) {
            calls.put(number, this);
            synchronized (mCallIdLock) {
                rilImsCall = call;
                mCallIdLock.notify();
            }
        }
    }

    public void updateCall(RILImsCall call) {
        rilImsCall = call;
    }

    @Override
    public void setListener(ImsCallSessionListener listener) {
        this.listener = listener;
    }

    @Override
    public String getCallId() {
        return "slot" + mSlotId + "id" + rilImsCall.index;
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

    public int convertAospCallType(int callType) {
        switch (callType) {
            case CALL_TYPE_VOICE_N_VIDEO:
            case CALL_TYPE_VOICE:
                return RILImsCallType.CALL_TYPE_VOICE;
            case CALL_TYPE_VIDEO_N_VOICE:
            case CALL_TYPE_VT:
                return RILImsCallType.CALL_TYPE_VT;
            case CALL_TYPE_VT_TX:
                return RILImsCallType.CALL_TYPE_VT_TX;
            case CALL_TYPE_VT_RX:
                return RILImsCallType.CALL_TYPE_VT_RX;
            case CALL_TYPE_VT_NODIR:
                return RILImsCallType.CALL_TYPE_VT_NODIR;
            case CALL_TYPE_VS:
                throw new RuntimeException("NI VS!!");
            case CALL_TYPE_VS_TX:
                return RILImsCallType.CALL_TYPE_CS_VS_TX;
            case CALL_TYPE_VS_RX:
                return RILImsCallType.CALL_TYPE_CS_VS_RX;
            default:
                throw new RuntimeException("Unknown callType " + callType);
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
        try {
            callType = convertAospCallType(profile.getCallType());
        } catch (RuntimeException e) {
            listener.callSessionInitiatedFailed(new ImsReasonInfo(ImsReasonInfo.CODE_LOCAL_INTERNAL_ERROR, ImsReasonInfo.CODE_UNSPECIFIED, e.getMessage()));
            throw e;
        }
        callInfo.callDetails.callType = callType;
        callInfo.callDetails.callDomain = RILImsCallDomain.CALL_DOMAIN_AUTOMATIC;
        try {
            RilHolder.INSTANCE.getRadio(mSlotId).imsDial(RilHolder.callback((radioResponseInfo, rspMsgPayload) -> {
                if (radioResponseInfo.error == 0) {
                    Rlog.e(LOG_TAG, "MADE AN IMS CALL OMG WOW");
                    Log.e(LOG_TAG, "MADE AN IMS CALL OMG WOW");
                    awaitingIdFromRIL.put(callee, this);
                    mInCall = true;
                    mState = State.ESTABLISHED;
                    listener.callSessionInitiated(profile);
                } else {
                    Rlog.e(LOG_TAG, "Failed to make ims call :(");
                    Log.e(LOG_TAG, "failed to make ims call :(");
                    mState = State.TERMINATED;
                    listener.callSessionInitiatedFailed(new ImsReasonInfo());
                }
            }, mSlotId), callInfo);
        } catch (RemoteException e) {
            listener.callSessionInitiatedFailed(new ImsReasonInfo());
            Rlog.e(LOG_TAG, "Sending imsDial failed with exception", e);
        }
    }

    @Override
    public void startConference(String[] members, ImsCallProfile profile) {
        // This method is to initiate the conference call, not to add all the members.
        start(members[0], profile);
        //TODO is this right?
    }

    @Override
    public void accept(int callType, ImsStreamMediaProfile profile) {
        mState = State.ESTABLISHING;
        try {
            RilHolder.INSTANCE.getRadio(mSlotId).acceptImsCall(RilHolder.callback((radioResponseInfo, rspMsgPayload) -> {
                if (radioResponseInfo.error != 0) {
                    listener.callSessionInitiatedFailed(new ImsReasonInfo());
                    Rlog.e(LOG_TAG, "error accepting ims call");
                } else {
                    listener.callSessionInitiated(new ImsCallProfile());
                    mState = State.ESTABLISHED;
                    mInCall = true;
                }
            }, mSlotId), callType);
        } catch (RemoteException e) {
            listener.callSessionInitiatedFailed(new ImsReasonInfo());
            Rlog.e(LOG_TAG, "failed to accept ims call");
        }
    }

    @Override
    public void deflect(String destination) {
        // Huawei shim this, we can do the same.
    }

    @Override
    public void reject(int reason) {
        /*
        try {
            getRilCallId();
            RilHolder.INSTANCE.getRadio(mSlotId).rejectCallWithReason(RilHolder.callback((radioResponseInfo, rspMsgPayload) -> {
                if (radioResponseInfo.error == 0) {
                    Rlog.d(LOG_TAG, "Rejected incoming call.");
                } else {
                    Rlog.e(LOG_TAG, "Failed to reject incoming call!");
                }
            }, mSlotId), rilImsCall.index, reason);
        } catch (RemoteException e) {
            //and here too
            Rlog.e(LOG_TAG, "Error listing ims calls!");
        }
        */
        // The above doesn't work. So, we do it the huawei way, which is to hangup the call.
        mState = State.TERMINATING;
        try {
            getRilCallId();
            RilHolder.INSTANCE.getRadio(mSlotId).hangup(RilHolder.callback((radioResponseInfo, rspMsgPayload) -> {
                Rlog.d(LOG_TAG, "got cb for hangup!");
                if (radioResponseInfo.error != 0) {
                    mState = State.INVALID;
                    Rlog.e(LOG_TAG, "Error hanging up!");
                } else {
                    mState = State.TERMINATED;
                }
            }, mSlotId), rilImsCall.index);
        } catch (RemoteException e) {
            Rlog.e(LOG_TAG, "error hanging up", e);
        }

    }

    private void getRilCallId() {
        synchronized (mCallIdLock) {
            while (rilImsCall == null) {
                    try {
                        mCallIdLock.wait();
                    } catch (InterruptedException ignored) {
                }
            }
        }
    }

    @Override
    public void terminate(int reason) {
        mState = State.TERMINATING;
        try {
            getRilCallId();
            Rlog.d(LOG_TAG, "terminating call...");
            RilHolder.INSTANCE.getRadio(mSlotId).hangup(RilHolder.callback((radioResponseInfo, rspMsgPayload) -> {
                Rlog.d(LOG_TAG, "got cb for hangup!");
                if (radioResponseInfo.error != 0) {
                    mState = State.INVALID;
                    Rlog.e(LOG_TAG, "Error hanging up!");
                } else {
                    mState = State.TERMINATED;
                }
            }, mSlotId), rilImsCall.index);
        } catch (RemoteException e) {
            Rlog.e(LOG_TAG, "error hanging up", e);
        }
    }

    public void notifyDead() {
        mState = State.TERMINATED;
        mInCall = false;
        listener.callSessionTerminated(new ImsReasonInfo());
    }

    @Override
    public void hold(ImsStreamMediaProfile profile) {
        try {
            RilHolder.INSTANCE.getRadio(mSlotId).switchWaitingOrHoldingAndActive(RilHolder.callback((radioResponseInfo, rspMsgPayload) -> {
                if (radioResponseInfo.error == 0) {
                    listener.callSessionHeld(mProfile);
                } else {
                    listener.callSessionHoldFailed(new ImsReasonInfo());
                }
            }, mSlotId));
        } catch (RemoteException e) {
            Rlog.e(LOG_TAG, "Error holding", e);
        }
    }

    @Override
    public void resume(ImsStreamMediaProfile profile) {
        try {
            RilHolder.INSTANCE.getRadio(mSlotId).switchWaitingOrHoldingAndActive(RilHolder.callback((radioResponseInfo, rspMsgPayload) -> {
                if (radioResponseInfo.error == 0) {
                    listener.callSessionResumed(mProfile);
                } else {
                    Rlog.e(LOG_TAG, "failed to resume");
                    listener.callSessionResumeFailed(new ImsReasonInfo());
                }
            }, mSlotId));
        } catch (RemoteException e) {
            listener.callSessionResumeFailed(new ImsReasonInfo());
            Rlog.e(LOG_TAG, "failed to resume", e);
        }
    }

    @Override
    public void merge() {
        try {
            RilHolder.INSTANCE.getRadio(mSlotId).conference(RilHolder.callback((radioResponseInfo, rspMsgPayload) -> {
                if (radioResponseInfo.error == 0) {
                    // Do nothing, notifyConfDone will be called by the RadioResponse code (triggered by RadioIndication)
                } else {
                    listener.callSessionMergeFailed(new ImsReasonInfo());
                }
            }, mSlotId));
        } catch (RemoteException e) {
            listener.callSessionMergeFailed(new ImsReasonInfo());
            Rlog.e(LOG_TAG, "failed to request conference", e);
        }
    }

    public void notifyConfDone(RILImsCall call) {
        listener.callSessionMergeComplete(new HwImsCallSession(mSlotId, mProfile, call));
    }

    @Override
    public void update(int callType, ImsStreamMediaProfile profile) {
        //TODO
    }

    @Override
    public void extendToConference(String[] participants) {
        // Huawei shim this, so do we.
    }

    @Override
    public void inviteParticipants(String[] participants) {
        // Huawei shim this, so do we.
    }

    @Override
    public void removeParticipants(String[] participants) {
        // Huawei shim this, so do we.
    }

    //TODO wtf is DTMF? Should we implement it? How? What is it for? What priority? Who knows? Who cares?

    //TODO USSD

    //TODO Video Calling

    @Override
    public boolean isMultiparty() {
        return rilImsCall.isMpty > 0;
    }

    //TODO RealTimeText
}