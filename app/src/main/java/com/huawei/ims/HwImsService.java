package com.huawei.ims;

import android.telephony.TelephonyManager;
import android.telephony.ims.ImsService;
import android.telephony.ims.feature.ImsFeature;
import android.telephony.ims.feature.MmTelFeature;
import android.telephony.ims.stub.ImsConfigImplBase;
import android.telephony.ims.stub.ImsFeatureConfiguration;
import android.util.Log;

public class HwImsService extends ImsService {
    private static final String LOG_TAG = "HwImsService";
    private static HwImsService mInstance = null;
    private final HwMmTelFeature[] mmTelFeatures = {null, null, null};
    private final HwImsRegistration[] registrations = {null, null, null};
    private final HwImsConfig[] configs = new HwImsConfig[3];

    public static HwImsService getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        Log.v(LOG_TAG, "HwImsService created!");
    }

    @Override
    public void enableIms(int slotId) {
        ((HwMmTelFeature) createMmTelFeature(slotId)).registerIms();
    }

    @Override
    public void disableIms(int slotId) {
        ((HwMmTelFeature) createMmTelFeature(slotId)).unregisterIms();
    }

    @Override
    public void readyForFeatureCreation() {
        if (mInstance != null && mInstance != this) {
            throw new RuntimeException();
        }
        mInstance = this;
    }

    public boolean supportsDualIms() {
        return HwModemCapability.isCapabilitySupport(21) && getSystemService(TelephonyManager.class).getPhoneCount() > 1;
    }

    @Override
    public ImsFeatureConfiguration querySupportedImsFeatures() {
        ImsFeatureConfiguration.Builder builder = new ImsFeatureConfiguration.Builder()
                .addFeature(0, ImsFeature.FEATURE_MMTEL); // assume it works.
        if (supportsDualIms()) {
            builder.addFeature(1, ImsFeature.FEATURE_MMTEL);
        }
        return builder.build();
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