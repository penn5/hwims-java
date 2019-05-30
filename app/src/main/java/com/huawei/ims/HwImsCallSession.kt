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

import android.annotation.SuppressLint
import android.os.Message
import android.os.RemoteException
import android.telephony.PhoneNumberUtils
import android.telephony.Rlog
import android.telephony.ims.ImsCallProfile
import android.telephony.ims.ImsCallProfile.*
import android.telephony.ims.ImsCallSessionListener
import android.telephony.ims.ImsReasonInfo
import android.telephony.ims.ImsStreamMediaProfile
import android.telephony.ims.stub.ImsCallSessionImplBase
import android.util.Log
import com.android.ims.ImsConfig
import vendor.huawei.hardware.radio.V1_0.RILImsCall
import vendor.huawei.hardware.radio.V1_0.RILImsCallDomain
import vendor.huawei.hardware.radio.V1_0.RILImsCallType
import vendor.huawei.hardware.radio.V1_0.RILImsDial
import java.util.concurrent.ConcurrentHashMap

class HwImsCallSession
/* For outgoing (MO) calls */ (private val mSlotId: Int, profile: ImsCallProfile) : ImsCallSessionImplBase() {
    private val mProfile: ImsCallProfile
    private val mLocalProfile: ImsCallProfile
    private val mRemoteProfile: ImsCallProfile
    private var listener: ImsCallSessionListener? = null

    var rilImsCall: RILImsCall? = null
    private var mInCall = false

    private val mCallIdLock = Object()
    private var confInProgress = false
    private var mState: Int = 0

    var mCallee: String = ""
    private val mCount: Int

    init {
        this.mCount = sCount++
        this.mProfile = ImsCallProfile(SERVICE_TYPE_NORMAL, profile.callType)
        this.mLocalProfile = ImsCallProfile(SERVICE_TYPE_NORMAL, profile.callType)
        this.mRemoteProfile = ImsCallProfile(SERVICE_TYPE_NORMAL, profile.callType)
        this.mState = State.IDLE
    }

    // For incoming (MT) calls
    constructor(slotId: Int, profile: ImsCallProfile, call: RILImsCall) : this(slotId, profile) {
        updateCall(call)
        calls[call.index] = this
    }

    fun addIdFromRIL(call: RILImsCall) {
        synchronized(sCallsLock) {
            var worked = awaitingIdFromRIL.remove("+" + call.number, this)
            if (!worked)
                worked = awaitingIdFromRIL.remove(call.number, this)
            if (worked) {
                synchronized(mCallIdLock) {
                    updateCall(call)
                    calls[call.index] = this
                    mCallIdLock.notify()
                }
            }
        }
    }

    private fun hwOirToOir(oir: Int): Int {
        return when (oir) {
            OIR_BEHAVIOUR_TYPE_DEFAULT -> OIR_PRESENTATION_NOT_RESTRICTED
            OIR_BEHAVIOUR_TYPE_NOT_RESTRICTED -> OIR_PRESENTATION_NOT_RESTRICTED
            OIR_BEHAVIOUR_TYPE_NOT_SUBSCRIBED -> OIR_PRESENTATION_PAYPHONE
            OIR_BEHAVIOUR_TYPE_RESTRICTED -> OIR_PRESENTATION_RESTRICTED
            else -> OIR_PRESENTATION_UNKNOWN
        }
    }

    @SuppressLint("MissingPermission")
    fun updateCall(call: RILImsCall) {
        val lastState = mState
        when (call.state) {
            0 // ACTIVE
            -> if (rilImsCall == null) {
                mState = State.ESTABLISHED
                listener?.callSessionInitiated(mProfile)
            } else if (rilImsCall!!.state == 2 || // DIALING

                    rilImsCall!!.state == 3 || // ALERTING

                    rilImsCall!!.state == 4 || // INCOMING

                    rilImsCall!!.state == 5) { // WAITING
                mState = State.ESTABLISHED
                listener?.callSessionInitiated(mProfile)
            } else if (rilImsCall!!.state == 1 /* HOLDING */ && !confInProgress) { // HOLDING
                listener?.callSessionResumed(mProfile)
            } else {
                Rlog.e(tag, "stuff")
            }
            1 // HOLDING
            -> listener?.callSessionHeld(mProfile)
            2 // DIALING
            -> listener?.callSessionProgressing(ImsStreamMediaProfile())
            3 // ALERTING
            -> {
                mState = State.NEGOTIATING
                if (rilImsCall == null) {
                    Rlog.e(tag, "Alerting an incoming call wtf?")
                }
                listener?.callSessionProgressing(ImsStreamMediaProfile())
            }
            4/*INCOMING*/, 5 // WAITING
            -> {
            }
            6 // END
            -> die(ImsReasonInfo())
        }

        val subId = HwImsService.instance!!.subscriptionManager
                .getActiveSubscriptionInfoForSimSlotIndex(mSlotId).subscriptionId

        val telephonyManager = HwImsService.instance!!.telephonyManager.createForSubscriptionId(subId)

        // We have to do lots of complicated formatting stuff here because RIL returns different formats depending on the MCC-MNC
        Log.d(tag, "CC ${telephonyManager.networkCountryIso.toUpperCase()}")
        mProfile.setCallExtra(EXTRA_OI, PhoneNumberUtils.formatNumberToE164(
                call.number,
                (telephonyManager.networkCountryIso
                        ?: telephonyManager.simCountryIso).toUpperCase()))

        Log.d(tag, "Using OI ${Rlog.pii(tag, mProfile.getCallExtra(EXTRA_OI))} for profile")

        mProfile.setCallExtraInt(EXTRA_OIR, hwOirToOir(call.numberPresentation))
        mProfile.setCallExtra(EXTRA_CNA, if (call.name.isEmpty()) mProfile.getCallExtra(EXTRA_OI) else call.name)
        mProfile.setCallExtraInt(EXTRA_CNAP, hwOirToOir(call.namePresentation))

        if (rilImsCall?.callDetails?.callDomain != call.callDetails.callDomain)
        //TODO
            Log.w(tag, "NI change domain notify to aosp")

        if (rilImsCall?.callDetails?.callType != call.callDetails.callType)
        //TODO
            Log.w(tag, "NI change tech notify to aosp")


        if (lastState == mState /*state unchanged*/ && call.state != 6 /*END*/ && call != rilImsCall) {
            listener?.callSessionUpdated(mProfile)
        }
        rilImsCall = call
    }

    private fun die(reason: ImsReasonInfo) {
        if (rilImsCall != null)
            calls.remove(rilImsCall!!.index)
        awaitingIdFromRIL.remove(mCallee)
        mState = State.TERMINATED
        listener?.callSessionTerminated(reason)
    }

    fun notifyEnded() {
        die(ImsReasonInfo())
    }

    override fun setListener(listener: ImsCallSessionListener?) {
        this.listener = listener
    }

    override fun getCallId(): String {
        return "slot" + mSlotId + "id" + if (rilImsCall == null) "unknown!" + Integer.toString(mCount) else rilImsCall!!.index
    }

    override fun getCallProfile(): ImsCallProfile {
        return mProfile
    }

    override fun getRemoteCallProfile(): ImsCallProfile {
        //return mRemoteProfile;
        return mProfile
    }

    override fun getLocalCallProfile(): ImsCallProfile {
        //return mLocalProfile;
        return mProfile
    }

    override fun getProperty(name: String?): String {
        return mProfile.getCallExtra(name)
    }

    override fun getState(): Int {
        return mState
    }

    override fun isInCall(): Boolean {
        return mInCall
    }

    override fun setMute(muted: Boolean) {
        try {
            val serial = RilHolder.prepareBlock(mSlotId)
            RilHolder.getRadio(mSlotId)!!.setMute(serial, muted)
            if (RilHolder.blockUntilComplete(serial).error != 0) {
                Rlog.e(tag, "Failed to setMute! " + RilHolder.blockUntilComplete(serial))
            }
        } catch (e: RemoteException) {
            Rlog.e(tag, "Error sending setMute request!", e)
        }

    }

    private fun convertAospCallType(callType: Int): Int {
        return when (callType) {
            CALL_TYPE_VOICE_N_VIDEO, CALL_TYPE_VOICE -> RILImsCallType.CALL_TYPE_VOICE
            CALL_TYPE_VIDEO_N_VOICE, CALL_TYPE_VT -> RILImsCallType.CALL_TYPE_VT
            CALL_TYPE_VT_TX -> RILImsCallType.CALL_TYPE_VT_TX
            CALL_TYPE_VT_RX -> RILImsCallType.CALL_TYPE_VT_RX
            CALL_TYPE_VT_NODIR -> RILImsCallType.CALL_TYPE_VT_NODIR
            CALL_TYPE_VS -> throw RuntimeException("NI VS!!")
            CALL_TYPE_VS_TX -> RILImsCallType.CALL_TYPE_CS_VS_TX
            CALL_TYPE_VS_RX -> RILImsCallType.CALL_TYPE_CS_VS_RX
            else -> throw RuntimeException("Unknown callType $callType")
        }
    }

    override fun start(callee: String, profile: ImsCallProfile?) {
        Log.d(tag, "calling " + Rlog.pii(tag, callee))
        mCallee = callee
        val callInfo = RILImsDial()
        callInfo.address = callee
        callInfo.clir = profile!!.getCallExtraInt(EXTRA_OIR) // Huawei do this so it **must** be right... Oh wait...
        val extras = profile.mCallExtras.getBundle("OemCallExtras")
        if (extras != null) {
            Rlog.e(tag, "NI reading oemcallextras, it is $extras")
        }
        val callType: Int
        try {
            callType = convertAospCallType(profile.callType)
        } catch (e: RuntimeException) {
            listener?.callSessionInitiatedFailed(ImsReasonInfo(ImsReasonInfo.CODE_LOCAL_INTERNAL_ERROR, ImsReasonInfo.CODE_UNSPECIFIED, e.message))
            throw e
        }

        callInfo.callDetails.callType = callType
        if (HwImsService.instance!!.getConfig(mSlotId)!!.getConfigInt(ImsConfig.ConfigConstants.VLT_SETTING_ENABLED) == ImsConfig.FeatureValueConstants.ON) {
            callInfo.callDetails.callDomain = RILImsCallDomain.CALL_DOMAIN_AUTOMATIC
        } else {
            callInfo.callDetails.callDomain = RILImsCallDomain.CALL_DOMAIN_CS
        }

        try {
            Rlog.d(tag, "adding to awaiting id from ril")
            awaitingIdFromRIL[mCallee] = this // Do it sooner rather than later so that this call is not seen as a phantom
            RilHolder.getRadio(mSlotId)!!.imsDial(RilHolder.callback({ radioResponseInfo, _ ->
                if (radioResponseInfo.error == 0) {
                    Rlog.d(tag, "successfully placed call")
                    mInCall = true
                    mState = State.ESTABLISHED
                    listener?.callSessionInitiated(profile)
                } else {
                    Rlog.e(tag, "call failed")
                    mState = State.TERMINATED
                    awaitingIdFromRIL.remove(callee, this)
                    listener?.callSessionInitiatedFailed(ImsReasonInfo())
                }
            }, mSlotId), callInfo)
        } catch (e: RemoteException) {
            listener?.callSessionInitiatedFailed(ImsReasonInfo())
            awaitingIdFromRIL.remove(callee, this)
            Rlog.e(tag, "Sending imsDial failed with exception", e)
        }

    }

    override fun startConference(members: Array<String>?, profile: ImsCallProfile?) {
        // This method is to initiate the conference call, not to add all the members.
        start(members!![0], profile)
        //TODO is this right?
    }

    override fun accept(callType: Int, profile: ImsStreamMediaProfile?) {
        mState = State.ESTABLISHING
        try {
            RilHolder.getRadio(mSlotId)!!.acceptImsCall(RilHolder.callback({ radioResponseInfo, _ ->
                if (radioResponseInfo.error != 0) {
                    listener?.callSessionInitiatedFailed(ImsReasonInfo())
                    Rlog.e(tag, "error accepting ims call")
                } else {
                    listener?.callSessionInitiated(mProfile)
                    mInCall = true
                }
            }, mSlotId), convertAospCallType(callType))
        } catch (e: RemoteException) {
            listener?.callSessionInitiatedFailed(ImsReasonInfo())
            Rlog.e(tag, "failed to accept ims call")
        }

    }

    override fun deflect(destination: String?) {
        // Huawei shim this, we can do the same.
    }

    override fun reject(reason: Int) {
        /*
        try {
            getRilCallId();
            RilHolder.INSTANCE.getRadio(mSlotId).rejectCallWithReason(RilHolder.callback((radioResponseInfo, rspMsgPayload) -> {
                if (radioResponseInfo.error == 0) {
                    Rlog.d(tag, "Rejected incoming call.");
                } else {
                    Rlog.e(tag, "Failed to reject incoming call!");
                }
            }, mSlotId), rilImsCall.index, reason);
        } catch (RemoteException e) {
            //and here too
            Rlog.e(tag, "Error listing ims calls!");
        }
        */
        // The above doesn't work. So, we do it the huawei way, which is to hangup the call. Reeee.
        mState = State.TERMINATING
        try {
            getRilCallId()
            RilHolder.getRadio(mSlotId)!!.hangup(RilHolder.callback({ radioResponseInfo, _ ->
                Rlog.d(tag, "got cb for hangup!")
                if (radioResponseInfo.error != 0) {
                    mState = State.INVALID
                    Rlog.e(tag, "Error hanging up!")
                } else {
                    mState = State.TERMINATED
                    die(ImsReasonInfo())

                }
            }, mSlotId), rilImsCall!!.index)
            // TODO FIXME: Radio doesn't reply to hangup() so we assume it worked.
            mState = State.TERMINATED
            die(ImsReasonInfo())
        } catch (e: RemoteException) {
            Rlog.e(tag, "error hanging up", e)
        }

    }

    private fun getRilCallId() {
        synchronized(mCallIdLock) {
            while (rilImsCall == null) {
                try {
                    mCallIdLock.wait()
                } catch (ignored: InterruptedException) {
                }

            }
        }
    }

    override fun terminate(reason: Int) {
        mState = State.TERMINATING
        try {
            getRilCallId()
            Rlog.d(tag, "terminating call...")
            RilHolder.getRadio(mSlotId)!!.hangup(RilHolder.callback({ radioResponseInfo, _ ->
                Rlog.d(tag, "got cb for hangup!")
                if (radioResponseInfo.error != 0) {
                    mState = State.INVALID
                    Rlog.e(tag, "Error hanging up!")
                } else {
                    mState = State.TERMINATED
                    die(ImsReasonInfo())
                }
            }, mSlotId), rilImsCall!!.index)
            // TODO FIXME: Radio doesn't reply to hangup() so we assume it worked.
            mState = State.TERMINATED
            die(ImsReasonInfo())
        } catch (e: RemoteException) {
            Rlog.e(tag, "error hanging up", e)
        }

    }

    override fun hold(profile: ImsStreamMediaProfile?) {
        try {
            RilHolder.getRadio(mSlotId)!!.switchWaitingOrHoldingAndActive(RilHolder.callback({ radioResponseInfo, _ ->
                if (radioResponseInfo.error == 0) {
                    listener?.callSessionHeld(mProfile)
                } else {
                    listener?.callSessionHoldFailed(ImsReasonInfo())
                }
            }, mSlotId))
        } catch (e: RemoteException) {
            Rlog.e(tag, "Error holding", e)
        }

    }

    override fun resume(profile: ImsStreamMediaProfile?) {
        try {
            RilHolder.getRadio(mSlotId)!!.switchWaitingOrHoldingAndActive(RilHolder.callback({ radioResponseInfo, _ ->
                if (radioResponseInfo.error == 0) {
                    listener?.callSessionResumed(mProfile)
                } else {
                    Rlog.e(tag, "failed to resume")
                    listener?.callSessionResumeFailed(ImsReasonInfo())
                }
            }, mSlotId))
        } catch (e: RemoteException) {
            listener?.callSessionResumeFailed(ImsReasonInfo())
            Rlog.e(tag, "failed to resume", e)
        }

    }

    override fun merge() {
        try {
            RilHolder.getRadio(mSlotId)!!.conference(RilHolder.callback({ radioResponseInfo, _ ->
                if (radioResponseInfo.error == 0) {
                    // Do nothing, notifyConfDone will be called by the RadioResponse code (triggered by RadioIndication)
                } else {
                    listener?.callSessionMergeFailed(ImsReasonInfo())
                }
            }, mSlotId))
        } catch (e: RemoteException) {
            listener?.callSessionMergeFailed(ImsReasonInfo())
            Rlog.e(tag, "failed to request conference", e)
        }

    }

    fun notifyConfDone(call: RILImsCall) {
        listener?.callSessionMergeComplete(HwImsCallSession(mSlotId, mProfile, call))
    }

    override fun update(callType: Int, profile: ImsStreamMediaProfile?) {
        //TODO
    }

    override fun extendToConference(participants: Array<String>?) {
        // Huawei shim this, so do we.
    }

    override fun inviteParticipants(participants: Array<String>?) {
        // Huawei shim this, so do we.
    }

    override fun removeParticipants(participants: Array<String>?) {
        // Huawei shim this, so do we.
    }

    override fun sendDtmf(c: Char, m: Message?) {
        try {
            RilHolder.getRadio(mSlotId)!!.sendDtmf(RilHolder.callback({ radioResponseInfo, _ ->
                if (radioResponseInfo.error != 0) {
                    Rlog.e(tag, "send DTMF error!")
                    //TODO we need to reply don't we? Respond with an error to DTMF
                } else {
                    Rlog.d(tag, "sent dtmf ok!")
                    if (m!!.replyTo != null) {
                        try {
                            m.replyTo.send(m)
                        } catch (e: RemoteException) {
                            Rlog.e(tag, "failed to reply to DTMF!", e)
                        }

                    }
                }
            }, mSlotId), Character.toString(c))
        } catch (e: RemoteException) {
            Rlog.e(tag, "failed to send DTMF!", e)
        }

    }

    override fun startDtmf(c: Char) {
        try {
            RilHolder.getRadio(mSlotId)!!.startDtmf(RilHolder.callback({ radioResponseInfo, _ ->
                if (radioResponseInfo.error != 0) {
                    Rlog.e(tag, "DTMF error!")
                } else {
                    Rlog.d(tag, "start dtmf ok!")
                }
            }, mSlotId), Character.toString(c))
        } catch (e: RemoteException) {
            Rlog.e(tag, "failed to start DTMF!", e)
        }

    }

    override fun stopDtmf() {
        try {
            RilHolder.getRadio(mSlotId)!!.stopDtmf(RilHolder.callback({ radioResponseInfo, _ ->
                if (radioResponseInfo.error != 0) {
                    Rlog.e(tag, "stop DTMF error!")
                } else {
                    Rlog.d(tag, "stopped dtmf ok!")
                }
            }, mSlotId))
        } catch (e: RemoteException) {
            Rlog.e(tag, "failed to stop DTMF!", e)
        }

    }

    //TODO USSD

    //TODO Video Calling

    override fun isMultiparty(): Boolean {
        return (rilImsCall?.isMpty ?: 0) > 0
        //return if (rilImsCall == null) false else rilImsCall!!.isMpty > 0
    }

    companion object {
        private const val OIR_BEHAVIOUR_TYPE_DEFAULT = 0
        private const val OIR_BEHAVIOUR_TYPE_NOT_RESTRICTED = 1
        private const val OIR_BEHAVIOUR_TYPE_RESTRICTED = 2
        private const val OIR_BEHAVIOUR_TYPE_NOT_SUBSCRIBED = 3


        private const val tag = "HwImsCallSession"
        val awaitingIdFromRIL = ConcurrentHashMap<String, HwImsCallSession>()
        val calls = ConcurrentHashMap<Int, HwImsCallSession>()

        private var sCount = 0

        val sCallsLock = Object()
    }

    //TODO RealTimeText
}
