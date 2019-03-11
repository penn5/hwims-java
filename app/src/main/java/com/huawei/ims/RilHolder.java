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
    public static RilHolder INSTANCE = new RilHolder();
    private static String[] serviceNames = {"rildi", "rildi2", "rildi3"};
    private static HwImsRadioResponse[] responseCallbacks = new HwImsRadioResponse[3];
    private static HwImsRadioIndication[] unsolCallbacks = new HwImsRadioIndication[3];
    private static IRadio[] radioImpls = new IRadio[3];
    private static int nextSerial = -1;
    private static ConcurrentHashMap<Integer, Callback> callbacks = new ConcurrentHashMap<>();

    private RilHolder() {
    }

    public synchronized static int callback(Callback cb) {
        nextSerial += 1;
        callbacks.put(nextSerial, cb);
        Log.v(LOG_TAG, "Setting callback for serial " + nextSerial);
        return nextSerial;
    }

    public static void triggerCB(int serial, @NonNull RadioResponseInfo radioResponseInfo, @Nullable RspMsgPayload rspMsgPayload) {
        Objects.requireNonNull(callbacks.get(serial)).run(radioResponseInfo, rspMsgPayload);
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
                responseCallbacks[slotId] = new HwImsRadioResponse();
                unsolCallbacks[slotId] = new HwImsRadioIndication();
                radioImpls[slotId].setResponseFunctionsHuawei(responseCallbacks[slotId], unsolCallbacks[slotId]);
            } catch (RemoteException e) {
                return null;
            }
        }
        return radioImpls[slotId];
    }

    public interface Callback {
        void run(@NonNull RadioResponseInfo radioResponseInfo, @Nullable RspMsgPayload rspMsgPayload);
    }

}

