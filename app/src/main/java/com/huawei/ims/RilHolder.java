package com.huawei.ims;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.os.RemoteException;
import android.util.Log;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import vendor.huawei.hardware.radio.V1_0.IRadio;
import vendor.huawei.hardware.radio.V1_0.RspMsgPayload;

//import android.hardware.radio.V1_0.*;

public class RilHolder {
    private static final String LOG_TAG = "HwImsRilHolder";
    public final static RilHolder INSTANCE = new RilHolder();
    private final static String[] serviceNames = {"rildi", "rildi2", "rildi3"};
    private final static HwImsRadioResponse[] responseCallbacks = new HwImsRadioResponse[3];
    private final static HwImsRadioIndication[] unsolCallbacks = new HwImsRadioIndication[3];
    private final static IRadio[] radioImpls = new IRadio[3];
    private static int nextSerial = -1;
    private final static ConcurrentHashMap<Integer, Integer> serialToSlot = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<Integer, Callback> callbacks = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<Integer, BlockingCallback> blocks = new ConcurrentHashMap<>();

    private RilHolder() {
    }

    public synchronized static int callback(Callback cb, int slotId) {
        int serial = getNextSerial();
        serialToSlot.put(serial, slotId);
        callbacks.put(serial, cb);
        Log.v(LOG_TAG, "Setting callback for serial " + serial);
        return serial;
    }

    public synchronized static int getNextSerial() {
        return ++nextSerial;
    }

    public static void triggerCB(int serial, @NonNull RadioResponseInfo radioResponseInfo, @Nullable RspMsgPayload rspMsgPayload) {
        Log.e(LOG_TAG, "Incoming response for slot " + serialToSlot.get(serial) + ", serial " + serial + ", radioResponseInfo " + radioResponseInfo + ", rspMsgPayload " + rspMsgPayload);
        Objects.requireNonNull(callbacks.get(serial)).run(radioResponseInfo, rspMsgPayload);
    }

    public static int prepareBlock(int slotId) {
        BlockingCallback cb = new BlockingCallback();
        int serial = callback(cb, slotId);
        blocks.put(serial, cb);
        return serial;
    }

    public interface Callback {
        void run(@NonNull RadioResponseInfo radioResponseInfo, @Nullable RspMsgPayload rspMsgPayload);
    }

    /*
     * It is safe to call this method multiple times, it will always return the same for the same serial.
     */
    public static RadioResponseInfo blockUntilComplete(int serial) {
        if (!blocks.containsKey(serial)) {
            return null;
        }
        try {
            while (!Objects.requireNonNull(blocks.get(serial)).done)
                Objects.requireNonNull(blocks.get(serial)).wait();
        } catch (InterruptedException ignored) {
        }
        return Objects.requireNonNull(blocks.get(serial)).radioResponseInfo;
    }

    public synchronized IRadio getRadio(int slotId) {
        if (radioImpls[slotId] == null) {
            try {
                try {
                    radioImpls[slotId] = IRadio.getService(serviceNames[slotId]);
                } catch (IndexOutOfBoundsException e) {
                    Log.e(LOG_TAG, "Index oob in rilholder. Bail Out!!!", e);
                    NotificationManager notificationManager = HwImsService.getInstance().getSystemService(NotificationManager.class);
                    NotificationChannel channel = new NotificationChannel("HwIms", "HwIms", NotificationManager.IMPORTANCE_HIGH);
                    notificationManager.createNotificationChannel(channel);
                    notificationManager.cancelAll();
                    Notification n = new Notification.Builder(HwImsService.getInstance(), "HwIms")
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle("HwIms not supported")
                            .setContentText("Please uninstall HwIms application from settings ASAP! Caused by broken IRadio or SELinux, try permissive.")
                            .setAutoCancel(true)
                            .build();
                    notificationManager.notify(0, n);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    // We're dead.
                }
                responseCallbacks[slotId] = new HwImsRadioResponse(slotId);
                unsolCallbacks[slotId] = new HwImsRadioIndication(slotId);
                radioImpls[slotId].setResponseFunctionsHuawei(responseCallbacks[slotId], unsolCallbacks[slotId]);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "remoteexception getting serivce. will throw npe later ig.");
                return null;
            }
        }
        return radioImpls[slotId];
    }

    private final static class BlockingCallback implements Callback {
        private boolean done = false;
        private RadioResponseInfo radioResponseInfo;

        @Override
        public void run(RadioResponseInfo radioResponseInfo, RspMsgPayload rspMsgPayload) {
            this.radioResponseInfo = radioResponseInfo;
            this.notify();
        }
    }

}

