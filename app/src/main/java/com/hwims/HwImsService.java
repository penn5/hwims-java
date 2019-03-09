package com.hwims;

import android.telephony.ims.ImsService;
import android.telephony.ims.feature.ImsFeature;
import android.telephony.ims.feature.MmTelFeature;
import android.telephony.ims.stub.ImsFeatureConfiguration;
import android.util.Log;

public class HwImsService extends ImsService {
    public static final String LOG_TAG = "HwImsService";
    public static HwImsService mInstance;
    public static HwImsService getInstance() {
        return mInstance;
    }

    public HwMmTelFeature[] mmTelFeatures = {null, null, null};

    private int lastSerial = -1;
    public synchronized int getSerial() {
        lastSerial += 1;
        return lastSerial;
    }

    public static String[] IMS_SERVICE_NAMES = {"rildi", "rildi2", "rildi3"};

    @Override
    public void onCreate() {
        Log.v(LOG_TAG, "HwImsService created!");
        if (mInstance != null) {
            throw new RuntimeException("This class may only be instantiated once!");
        }
        mInstance = this;
    }

    @Override
    public ImsFeatureConfiguration querySupportedImsFeatures() {
        
        return new ImsFeatureConfiguration.Builder()
                .addFeature(0, ImsFeature.FEATURE_MMTEL)
                .addFeature(1, ImsFeature.FEATURE_MMTEL)
                .addFeature(2, ImsFeature.FEATURE_MMTEL)
                .build();
    }

    @Override
    public MmTelFeature createMmTelFeature(int slotId) {
        if (mmTelFeatures[slotId] == null) {
            mmTelFeatures[slotId] = HwMmTelFeature.getInstance(slotId);
        }
        return mmTelFeatures[slotId];
    }
}
