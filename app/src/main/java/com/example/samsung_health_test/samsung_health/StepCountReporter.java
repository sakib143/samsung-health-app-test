/**
 * Copyright (C) 2019 Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Mobile Communication Division,
 * Digital Media & Communications Business, Samsung Electronics Co., Ltd.
 *
 * This software and its documentation are confidential and proprietary
 * information of Samsung Electronics Co., Ltd.  No part of the software and
 * documents may be copied, reproduced, transmitted, translated, or reduced to
 * any electronic medium or machine-readable form without the prior written
 * consent of Samsung Electronics.
 *
 * Samsung Electronics makes no representations with respect to the contents,
 * and assumes no responsibility for any errors that might appear in the
 * software and documents. This publication and the contents hereof are subject
 * to change without notice.
 */

package com.example.samsung_health_test.samsung_health;

import android.util.Log;

import com.example.samsung_health_test.AppUtils.GlobalMethods;
import com.samsung.android.sdk.healthdata.HealthData;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataResolver.Filter;
import com.samsung.android.sdk.healthdata.HealthDataResolver.ReadRequest;
import com.samsung.android.sdk.healthdata.HealthDataStore;

import org.json.JSONException;

import java.util.Iterator;

public class StepCountReporter {
    private static final String TAG = "StepCountReporter";
    private final HealthDataStore mStore;

    public StepCountReporter(HealthDataStore store) {
        mStore = store;
    }

    public void getTodayStepSummary(GetStepCountSummury getStepCountData, String selectedDate) {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

        Filter filter = Filter.and(
                Filter.eq("day_time", GlobalMethods.getEpochTime(selectedDate)),
                Filter.eq("source_type", -2)); //-2 will be for all device

        HealthDataResolver.ReadRequest request = new ReadRequest.Builder()
                .setDataType("com.samsung.shealth.step_daily_trend")
                .setProperties(new String[] {"count", "binning_data","                                                          "})
                .setFilter(filter)
                .build();

        try {
            resolver.read(request).setResultListener(result -> {
                try {
                    Iterator<HealthData> iter = result.iterator();
                    if (iter.hasNext()) {
                        HealthData data = iter.next();
                        int totalCount = data.getInt("count");
                        byte[] binning = data.getBlob("binning_data");
                        float distance = data.getFloat("distance");
                        getStepCountData.getStepData(totalCount,distance,selectedDate);
                    }else {
                        getStepCountData.getStepData(0,0,selectedDate);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    result.close();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Getting step count fails.", e);
        }
    }

    public interface GetStepCountSummury{
        void getStepData(int totalCount, float distance, String date) throws JSONException;
    }
}
