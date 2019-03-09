package com.hwims;

import android.os.RemoteException;

import java.util.concurrent.ConcurrentHashMap;

import vendor.huawei.hardware.radio.V1_0.IRadio;

//import android.hardware.radio.V1_0.*;

public class RilHolder {
    private static String[] serviceNames = {"rildi", "rildi2", "rildi3"};
    private static HwImsRadioResponse[] responseCallbacks = new HwImsRadioResponse[3];
    private static HwImsRadioIndication[] unsolCallbacks = new HwImsRadioIndication[3];
    private static IRadio[] radioImpls = new IRadio[3];
    private static int nextSerial = -1;
    private static ConcurrentHashMap<Integer, Runnable> callbacks = new ConcurrentHashMap<>();
    private static final String LOG_TAG = "HwImsRilHolder";
    public static RilHolder INSTANCE = new RilHolder();
    public synchronized IRadio getRadio(int slotId) {
        if (radioImpls[slotId] == null) {
            try {
                radioImpls[slotId] = IRadio.getService(serviceNames[slotId]);
                responseCallbacks[slotId] = new HwImsRadioResponse();
                unsolCallbacks[slotId] = new HwImsRadioIndication();
                radioImpls[slotId].setResponseFunctionsHuawei(responseCallbacks[slotId], unsolCallbacks[slotId]);
            } catch (RemoteException e) {
                return null;
            }
        }
        return radioImpls[slotId];
    }
    private RilHolder() {}


    public synchronized static int callback(Runnable cb) {
        nextSerial += 1;
        callbacks.put(nextSerial, cb);
        return nextSerial;
    }

}

