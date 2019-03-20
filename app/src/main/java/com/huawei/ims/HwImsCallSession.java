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

import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.Rlog;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsCallSessionListener;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsStreamMediaProfile;
import android.telephony.ims.feature.MmTelFeature;
import android.telephony.ims.stub.ImsCallSessionImplBase;
import android.telephony.ims.stub.ImsRegistrationImplBase;
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
import static android.telephony.ims.ImsCallProfile.EXTRA_CNA;
import static android.telephony.ims.ImsCallProfile.EXTRA_CNAP;
import static android.telephony.ims.ImsCallProfile.EXTRA_OI;
import static android.telephony.ims.ImsCallProfile.EXTRA_OIR;
import static android.telephony.ims.ImsCallProfile.SERVICE_TYPE_NORMAL;

public class HwImsCallSession extends ImsCallSessionImplBase {
    private static final int OIR_BEHAVIOUR_TYPE_DEFAULT = 0;
    private static final int OIR_BEHAVIOUR_TYPE_NOT_RESTRICTED = 1;
    private static final int OIR_BEHAVIOUR_TYPE_RESTRICTED = 2;
    private static final int OIR_BEHAVIOUR_TYPE_NOT_SUBSCRIBED = 3;



    private static final String LOG_TAG = "HwImsCallSession";
    static final ConcurrentHashMap<String, HwImsCallSession> awaitingIdFromRIL = new ConcurrentHashMap<>();
    private final ImsCallProfile mProfile;
    private ImsCallProfile mLocalProfile;
    private ImsCallProfile mRemoteProfile;
    private ImsCallSessionListener listener;

    RILImsCall rilImsCall = null;
    private boolean mInCall = false;
    static final ConcurrentHashMap<Integer, HwImsCallSession> calls = new ConcurrentHashMap<Integer, HwImsCallSession>();
    private final int mSlotId;

    private final Object mCallIdLock = new Object();
    boolean confInProgress = false;
    private int mState;

    public String mCallee;

    private static int sCount = 0;
    private int mCount;

    public static final Object sCallsLock = new Object();

    // For outgoing (MO) calls
    public HwImsCallSession(int slotId, ImsCallProfile profile) {
        this.mCount = sCount++;
        this.mSlotId = slotId;
        this.mProfile = new ImsCallProfile(SERVICE_TYPE_NORMAL, profile.getCallType());
        this.mLocalProfile = new ImsCallProfile(SERVICE_TYPE_NORMAL, profile.getCallType());
        this.mRemoteProfile = new ImsCallProfile(SERVICE_TYPE_NORMAL, profile.getCallType());
        this.mState = State.IDLE;
    }

    // For incoming (MT) calls
    public HwImsCallSession(int slotId, ImsCallProfile profile, RILImsCall call) {
        this(slotId, profile);
        updateCall(call);
        calls.put(call.index, this);
    }

    public void addIdFromRIL(RILImsCall call) {
        synchronized (sCallsLock) {
            boolean worked = awaitingIdFromRIL.remove("+"+call.number, this);
            if (!worked)
                worked = awaitingIdFromRIL.remove(call.number, this);
            if (worked) {
                synchronized (mCallIdLock) {
                    updateCall(call);
                    calls.put(call.index, this);
                    mCallIdLock.notify();
                }
            }
        }
    }

    private int hwOirToOir(int oir) {
        switch (oir) {
            case OIR_BEHAVIOUR_TYPE_DEFAULT:
                return ImsCallProfile.OIR_PRESENTATION_NOT_RESTRICTED;
            case OIR_BEHAVIOUR_TYPE_NOT_RESTRICTED:
                return ImsCallProfile.OIR_PRESENTATION_NOT_RESTRICTED;
            case OIR_BEHAVIOUR_TYPE_NOT_SUBSCRIBED:
                return ImsCallProfile.OIR_PRESENTATION_PAYPHONE;
            case OIR_BEHAVIOUR_TYPE_RESTRICTED:
                return ImsCallProfile.OIR_PRESENTATION_RESTRICTED;
            default:
                return ImsCallProfile.OIR_PRESENTATION_UNKNOWN;
        }
    }

    public void updateCall(RILImsCall call) {
        int lastState = mState;
        switch (call.state) {
            case 0: // ACTIVE
                if (rilImsCall == null) {
                    mState = State.ESTABLISHED;
                    if (listener != null)
                        listener.callSessionInitiated(mProfile);
                } else if (rilImsCall.state == 2 || // DIALING
                        rilImsCall.state == 3 || // ALERTING
                        rilImsCall.state == 4 || // INCOMING
                        rilImsCall.state == 5) { // WAITING
                    mState = State.ESTABLISHED;
                    if (listener != null)
                        listener.callSessionInitiated(mProfile);
                } else if (rilImsCall.state == 1 && !confInProgress) { // HOLDING
                    if (listener != null)
                        listener.callSessionResumed(mProfile);
                } else {
                    Rlog.e(LOG_TAG, "stuff");
                }
                break;
            case 1: // HOLDING
                if (listener != null)
                    listener.callSessionHeld(mProfile);
                break;
            case 2: // DIALING
                if (listener != null)
                    listener.callSessionProgressing(new ImsStreamMediaProfile());
                break;
            case 3: // ALERTING
                mState = State.NEGOTIATING;
                if (rilImsCall == null) {
                    Rlog.e(LOG_TAG, "Alerting an incoming call wtf?");
                }
                if (listener != null)
                    listener.callSessionProgressing(new ImsStreamMediaProfile());
                break;
            case 4: // INCOMING
            case 5: // WAITING
                break;
            case 6: // END
                mState = State.TERMINATED;
                if (listener != null)
                    die(new ImsReasonInfo());
                break;
        }

        mProfile.setCallExtra(EXTRA_OI, call.number);
        mProfile.setCallExtraInt(EXTRA_OIR, hwOirToOir(call.numberPresentation));
        mProfile.setCallExtra(EXTRA_CNA, call.name.isEmpty() ? call.number : call.name);
        mProfile.setCallExtraInt(EXTRA_CNAP, hwOirToOir(call.namePresentation));

        mCallee = call.number;

        if (lastState == mState /*state unchanged*/ && call.state != 6 /*END*/ && (!call.equals(rilImsCall)) && listener != null) {
            listener.callSessionUpdated(mProfile);
        }
        rilImsCall = call;
    }

    private void die(ImsReasonInfo reason) {
        if (rilImsCall != null)
            calls.remove(rilImsCall.index);
        awaitingIdFromRIL.remove(mCallee);
        mState = State.TERMINATED;
        if (listener != null) {
            listener.callSessionTerminated(reason);
        }
    }

    public void notifyEnded() {
        die(new ImsReasonInfo());
    }

    @Override
    public void setListener(ImsCallSessionListener listener) {
        this.listener = listener;
    }

    @Override
    public String getCallId() {
        return "slot" + mSlotId + "id" + (rilImsCall == null ? "unknown!" + Integer.toString(mCount) : rilImsCall.index);
    }

    @Override
    public ImsCallProfile getCallProfile() {
        return mProfile;
    }

    @Override
    public ImsCallProfile getRemoteCallProfile() {
        //return mRemoteProfile;
        return mProfile;
    }

    @Override
    public ImsCallProfile getLocalCallProfile() {
        //return mLocalProfile;
        return mProfile;
    }

    @Override
    public String getProperty(String name) {
        return mProfile.getCallExtra(name);
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

    private int convertAospCallType(int callType) {
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
        Log.d(LOG_TAG, "calling " + Rlog.pii(LOG_TAG, callee));
        mCallee = callee;
        RILImsDial callInfo = new RILImsDial();
        callInfo.address = callee;
        callInfo.clir = profile.getCallExtraInt(EXTRA_OIR); // Huawei do this so it **must** be right... Oh wait...
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
        if (HwImsService.getInstance().createMmTelFeature(mSlotId).queryCapabilityConfiguration(ImsRegistrationImplBase.REGISTRATION_TECH_LTE, MmTelFeature.MmTelCapabilities.CAPABILITY_TYPE_VOICE)) {
            callInfo.callDetails.callDomain = RILImsCallDomain.CALL_DOMAIN_AUTOMATIC;
        } else {
            callInfo.callDetails.callDomain = RILImsCallDomain.CALL_DOMAIN_CS;
        }

        try {
            Rlog.d(LOG_TAG, "adding to awaiting id from ril");
            awaitingIdFromRIL.put(mCallee, this); // Do it sooner rather than later so that this call is not seen as a phantom
            RilHolder.INSTANCE.getRadio(mSlotId).imsDial(RilHolder.callback((radioResponseInfo, rspMsgPayload) -> {
                if (radioResponseInfo.error == 0) {
                    Rlog.e(LOG_TAG, "MADE AN IMS CALL OMG WOW");
                    Log.e(LOG_TAG, "MADE AN IMS CALL OMG WOW");
                    mInCall = true;
                    mState = State.ESTABLISHED;
                    listener.callSessionInitiated(profile);
                } else {
                    Rlog.e(LOG_TAG, "Failed to make ims call :(");
                    Log.e(LOG_TAG, "failed to make ims call :(");
                    mState = State.TERMINATED;
                    awaitingIdFromRIL.remove(mCallee, this);
                    listener.callSessionInitiatedFailed(new ImsReasonInfo());
                }
            }, mSlotId), callInfo);
        } catch (RemoteException e) {
            listener.callSessionInitiatedFailed(new ImsReasonInfo());
            awaitingIdFromRIL.remove(mCallee, this);
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
                    listener.callSessionInitiated(mProfile);
                    mInCall = true;
                }
            }, mSlotId), convertAospCallType(callType));
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
                    die(new ImsReasonInfo());

                }
            }, mSlotId), rilImsCall.index);
            // TODO FIXME: Radio doesn't reply to hangup() so we assume it worked.
            mState = State.TERMINATED;
            die(new ImsReasonInfo());
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
                    die(new ImsReasonInfo());
                }
            }, mSlotId), rilImsCall.index);
            // TODO FIXME: Radio doesn't reply to hangup() so we assume it worked.
            mState = State.TERMINATED;
            die(new ImsReasonInfo());
        } catch (RemoteException e) {
            Rlog.e(LOG_TAG, "error hanging up", e);
        }
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

    public void sendDtmf(char c, Message m) {
        try {
            RilHolder.INSTANCE.getRadio(mSlotId).sendDtmf(RilHolder.callback((radioResponseInfo, rspMsgPayload) -> {
                if (radioResponseInfo.error != 0) {
                    Rlog.e(LOG_TAG, "send DTMF error!");
                    //TODO we need to reply don't we? Respond with an error to DTMF
                } else {
                    Rlog.d(LOG_TAG, "sent dtmf ok!");
                    if (m.replyTo != null) {
                        try {
                            m.replyTo.send(m);
                        } catch (RemoteException e) {
                            Rlog.e(LOG_TAG, "failed to reply to DTMF!", e);
                        }
                    }
                }
            }, mSlotId), Character.toString(c));
        } catch (RemoteException e) {
            Rlog.e(LOG_TAG, "failed to send DTMF!", e);
        }
    }

    public void startDtmf(char c) {
        try {
            RilHolder.INSTANCE.getRadio(mSlotId).startDtmf(RilHolder.callback((radioResponseInfo, rspMsgPayload) -> {
                if (radioResponseInfo.error != 0) {
                    Rlog.e(LOG_TAG, "DTMF error!");
                } else {
                    Rlog.d(LOG_TAG, "start dtmf ok!");
                }
            }, mSlotId), Character.toString(c));
        } catch (RemoteException e) {
            Rlog.e(LOG_TAG, "failed to start DTMF!", e);
        }
    }

    public void stopDtmf() {
        try {
            RilHolder.INSTANCE.getRadio(mSlotId).stopDtmf(RilHolder.callback((radioResponseInfo, rspMsgPayload) -> {
                if (radioResponseInfo.error != 0) {
                    Rlog.e(LOG_TAG, "stop DTMF error!");
                } else {
                    Rlog.d(LOG_TAG, "stopped dtmf ok!");
                }
            }, mSlotId));
        } catch (RemoteException e) {
            Rlog.e(LOG_TAG, "failed to stop DTMF!", e);
        }
    }

    //TODO USSD

    //TODO Video Calling

    @Override
    public boolean isMultiparty() {
        if (rilImsCall == null)
            return false;
        return rilImsCall.isMpty > 0;
    }

    //TODO RealTimeText
}
