package com.example.samsung_health_test.samsung_health.NewData;

import android.util.Log;

import com.example.samsung_health_test.AppUtils.GlobalMethods;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthData;
import com.samsung.android.sdk.healthdata.HealthDataObserver;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataStore;

public class SwimmingReportNew {

    private final HealthDataStore mStore;
    private SwimObserver swimObserver;
    private static final long ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L;

    public SwimmingReportNew(HealthDataStore store) {
        mStore = store;
    }

    public void start(SwimObserver listener, String strDate) {
        swimObserver = listener;
        HealthDataObserver.addObserver(mStore, HealthConstants.Exercise.HEALTH_DATA_TYPE, new HealthDataObserver(null) {
            @Override
            public void onChange(String s) {
                readTodaySwimData(strDate);
            }
        });

        readTodaySwimData(strDate);
    }

    private void readTodaySwimData(String strDate) {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);
        long startTime = GlobalMethods.getEpochTime(strDate);
        long endTime = startTime + ONE_DAY_IN_MILLIS;

        HealthDataResolver.ReadRequest request = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.Exercise.HEALTH_DATA_TYPE)
                .setProperties(new String[]{HealthConstants.Exercise.ADDITIONAL,HealthConstants.Exercise.DISTANCE})
                .setLocalTimeRange(HealthConstants.Exercise.START_TIME, HealthConstants.Exercise.TIME_OFFSET,
                        startTime, endTime)
                .build();

        try {
            resolver.read(request).setResultListener(result ->{
                int distance = 0;
                try {
                    for (HealthData data : result) {
                        distance += data.getFloat(HealthConstants.Exercise.DISTANCE);
                    }
                } finally {
                    result.close();
                }
                if (swimObserver != null) {
                    swimObserver.onChanged(distance,strDate);
                }
            });
        } catch (Exception e) {
            Log.e("=> ", "Getting step count fails.", e);
        }
    }

    public interface SwimObserver {
        void onChanged(int distance, String date);
    }

}
