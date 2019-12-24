package com.example.samsung_health_test.samsung_health.NewData;

import android.util.Log;

import com.example.samsung_health_test.AppUtils.GlobalMethods;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthData;
import com.samsung.android.sdk.healthdata.HealthDataObserver;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataStore;

import java.util.Date;

public class SleeepReportNew {

    private final HealthDataStore mStore;
    private SleepObserver sleepObserver;
    private static final long ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L;

    public SleeepReportNew(HealthDataStore mStore) {
        this.mStore = mStore;
    }

    public void start(SleepObserver sleepObserver, String strDate) {
        this.sleepObserver = sleepObserver;
        HealthDataObserver.addObserver(mStore, HealthConstants.Sleep.HEALTH_DATA_TYPE, new HealthDataObserver(null) {
            @Override
            public void onChange(String s) {
                readTodayStepCount(strDate);
            }
        });
        readTodayStepCount(strDate);
    }

    private void readTodayStepCount(String strDate) {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);
        long startTime = GlobalMethods.getEpochTime(strDate);
        long endTime = startTime + ONE_DAY_IN_MILLIS;

        HealthDataResolver.ReadRequest request = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.Sleep.HEALTH_DATA_TYPE)
                .setProperties(new String[]{HealthConstants.Sleep.START_TIME, HealthConstants.Sleep.END_TIME})
                .setLocalTimeRange(HealthConstants.Sleep.START_TIME, HealthConstants.Sleep.TIME_OFFSET,
                        startTime, endTime)
                .build();

        try {
            resolver.read(request).setResultListener(result -> {
                String totalSleepMinute = "0";


                try {
                    for (HealthData data : result) {
                        totalSleepMinute = GlobalMethods.getDiffMinute(
                                new Date(data.getInt(HealthConstants.Sleep.START_TIME)),
                                new Date(data.getInt(HealthConstants.Sleep.END_TIME)));
                    }
                } finally {
                    result.close();
                }
                if (sleepObserver != null) {
                    sleepObserver.onChanged(totalSleepMinute, strDate);
                }
            });
        } catch (Exception e) {
            Log.e("=> ", "Getting step count fails.", e);
        }
    }

    public interface SleepObserver {
        void onChanged(String totalSleepMinute, String date);
    }
}
