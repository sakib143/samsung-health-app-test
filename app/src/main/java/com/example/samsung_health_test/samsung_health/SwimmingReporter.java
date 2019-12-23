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
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthData;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

// TODO: 25-11-2019 This class is used to get Swimming related data by Sakib START
public class SwimmingReporter {

    private static final String TAG = "SwimmingReporter";
    private static final long ONE_DAY = 24 * 60 * 60 * 1000;
    private final HealthDataStore mStore;

    public SwimmingReporter(HealthDataStore store) {
        mStore = store;
    }

    public void getTodayExerciseData(GetSwimmingSummury getExerciseSummury, String lastSyncDate, JSONObject objHealthData) {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

        long dayTime = GlobalMethods.getEpochTime(lastSyncDate);
        long newTime = dayTime + ONE_DAY;

        HealthDataResolver.Filter filter =
                HealthDataResolver.Filter.and(HealthDataResolver.Filter.greaterThanEquals
                                (HealthConstants.Exercise.START_TIME, dayTime),
                HealthDataResolver.Filter.lessThanEquals(HealthConstants.Exercise.START_TIME, newTime));

        HealthDataResolver.ReadRequest swimRequest = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.Exercise.HEALTH_DATA_TYPE)
                .setProperties(new String[]{
                        HealthConstants.Exercise.EXERCISE_TYPE,
                        HealthConstants.Exercise.DURATION,
                        HealthConstants.Exercise.MAX_SPEED,
                        HealthConstants.Exercise.MEAN_SPEED,
                })
                .setFilter(filter)
                .build();

        try {
            resolver.read(swimRequest).setResultListener(result -> {
                try {
                    Iterator<HealthData> iter = result.iterator();
                    if (iter.hasNext()) {
                        HealthData data = iter.next();
                        double distance = data.getFloat(HealthConstants.Exercise.DISTANCE);
                        getExerciseSummury.getSwimmingData(distance,lastSyncDate,objHealthData);
                    }else {
                        getExerciseSummury.getSwimmingData(0,lastSyncDate,objHealthData);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    result.close();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Getting exercise summary fails.", e);
        }




//        HealthDataResolver.ReadRequest swimRequest = new HealthDataResolver.ReadRequest.Builder()
//                .setDataType(HealthConstants.Exercise.HEALTH_DATA_TYPE)
//                .setLocalTimeRange(HealthConstants.Exercise.START_TIME,
//                        HealthConstants.Exercise.TIME_OFFSET, dayTime, newTime)
//                .setSort(HealthConstants.Exercise.START_TIME, HealthDataResolver.SortOrder.DESC)
//                .setResultCount(0,1)
//                .build();
//        try {
//            resolver.read(swimRequest).setResultListener(result -> {
//                try {
//                    Iterator<HealthData> iter = result.iterator();
//                    if (iter.hasNext()) {
//                        HealthData data = iter.next();
//                        double distance = data.getFloat(HealthConstants.Exercise.DISTANCE);
//                        getExerciseSummury.getSwimmingData(distance,lastSyncDate,objHealthData);
//                    }else {
//                        getExerciseSummury.getSwimmingData(0,lastSyncDate,objHealthData);
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } finally {
//                    result.close();
//                }
//            });
//        } catch (Exception e) {
//            Log.e(TAG, "Getting exercise summary fails.", e);
//        }
    }

    public interface GetSwimmingSummury{
        void getSwimmingData(double distance, String date, JSONObject objHealthData) throws JSONException;
    }
}
