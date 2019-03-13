/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// From aosp testapps
package com.huawei.ims;

import android.annotation.NonNull;
import android.telephony.ims.stub.ImsConfigImplBase;

import com.android.ims.ImsConfig;

import java.util.concurrent.ConcurrentHashMap;

public class HwImsConfig extends ImsConfigImplBase {
    private final ConcurrentHashMap<Integer, Integer> configInt = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, String> configString = new ConcurrentHashMap<>();


    @Override
    public int setConfig(int item, int value) {
        configInt.put(item, value);
        return ImsConfig.OperationStatusConstants.SUCCESS;
    }

    @Override
    public int setConfig(int item, String value) {
        configString.put(item, value);
        return ImsConfig.OperationStatusConstants.SUCCESS;
    }

    @Override
    public int getConfigInt(@NonNull int item) {
        if (configInt.containsKey(item)) {
            return configInt.get(item);
        } else {
            return ImsConfig.FeatureValueConstants.ERROR;
        }
    }

    @Override
    public String getConfigString(int item) {
        if (configString.containsKey(item)) {
            return configString.get(item);
        } else {
            return null;
        }
    }
}