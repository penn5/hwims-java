package com.huawei.ims;

import android.telephony.ims.ImsService;
import android.telephony.ims.feature.ImsFeature;
import android.telephony.ims.feature.MmTelFeature;
import android.telephony.ims.stub.ImsConfigImplBase;
import android.telephony.ims.stub.ImsFeatureConfiguration;
import android.util.Log;

public class HwImsService extends ImsService {
    private static final String LOG_TAG = "HwImsService";
    private static HwImsService mInstance = null;
    public static String[] IMS_SERVICE_NAMES = {"rildi", "rildi2", "rildi3"};
    private HwMmTelFeature[] mmTelFeatures = {null, null, null};
    private HwImsRegistration[] registrations = {null, null, null};
    private HwImsConfig[] configs = new HwImsConfig[3];

    private int lastSerial = -1;

    public static HwImsService getInstance() {
        return mInstance;
    }

    public synchronized int getSerial() {
        lastSerial += 1;
        return lastSerial;
    }

    @Override
    public void onCreate() {
        Log.v(LOG_TAG, "HwImsService created!");
        if (mInstance != null) {
//            throw new RuntimeException("This class may only be instantiated once!");
        }
        //mInstance = this;
        //mInstances.add(this);
    }

    @Override
    public void enableIms(int slotId) {
        ((HwMmTelFeature) createMmTelFeature(slotId)).registerIms();
    }

    @Override
    public void disableIms(int slotId) {
        //((HwMmTelFeature)createMmTelFeature(slotId)).unregisterIms();
    }

    @Override
    public void readyForFeatureCreation() {
        if (mInstance != null && mInstance != this) {
            throw new RuntimeException();
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
            registrations[slotId] = new HwImsRegistration();
        }
        return mmTelFeatures[slotId];
    }

    @Override
    public ImsConfigImplBase getConfig(int slotId) {
        if (configs[slotId] == null) {
            configs[slotId] = new HwImsConfig();
        }
        return configs[slotId];
    }

    @Override
    public HwImsRegistration getRegistration(int slotId) {
        if (this.registrations[slotId] == null) {
            registrations[slotId] = new HwImsRegistration();
        }
        return this.registrations[slotId];
    }
}
