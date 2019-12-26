package com.example.samsung_health_test.samsung_health.NewData;

import android.util.Log;

import com.example.samsung_health_test.AppUtils.GlobalMethods;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthData;
import com.samsung.android.sdk.healthdata.HealthDataObserver;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataStore;

import org.json.JSONObject;

public class StepCountReport {

    private final HealthDataStore mStore;
    private StepCountObserver mStepCountObserver;
    private static final long ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L;

    public StepCountReport(HealthDataStore store) {
        mStore = store;
    }

    public void start(StepCountObserver listener, String strDate, JSONObject jsonObject) {
        mStepCountObserver = listener;
        HealthDataObserver.addObserver(mStore, HealthConstants.StepCount.HEALTH_DATA_TYPE, new HealthDataObserver(null) {
            @Override
            public void onChange(String s) {
                readTodayStepCount(strDate,jsonObject);
            }
        });
        readTodayStepCount(strDate,jsonObject);
    }

    private void readTodayStepCount(String strDate, JSONObject jsonObject) {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);
        long startTime = GlobalMethods.getEpochTime(strDate);
        long endTime = startTime + ONE_DAY_IN_MILLIS;

        HealthDataResolver.ReadRequest request = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.StepCount.HEALTH_DATA_TYPE)
                .setProperties(new String[]{HealthConstants.StepCount.COUNT,HealthConstants.StepCount.DISTANCE})
                .setLocalTimeRange(HealthConstants.StepCount.START_TIME, HealthConstants.StepCount.TIME_OFFSET,
                        startTime, endTime)
                .build();

        try {
            resolver.read(request).setResultListener(result ->{
                int count = 0;
                int distance = 0;
                try {
                    for (HealthData data : result) {
                        count += data.getInt(HealthConstants.StepCount.COUNT);
                        distance += data.getInt(HealthConstants.StepCount.DISTANCE);
                    }
                } finally {
                    result.close();
                }
                if (mStepCountObserver != null) {
                    mStepCountObserver.onChanged(count,distance,strDate,jsonObject);
                }
            });
        } catch (Exception e) {
            Log.e("=> ", "Getting step count fails.", e);
        }
    }

    public interface StepCountObserver {
        void onChanged(int count, int distance, String date, JSONObject jsonObject);
    }
}
