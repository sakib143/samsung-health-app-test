package com.example.samsung_health_test.samsung_health.NewData;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.samsung_health_test.AppUtils.GlobalMethods;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthData;
import com.samsung.android.sdk.healthdata.HealthDataObserver;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthDataUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

public class SwimmingReport {

    private final HealthDataStore mStore;
    private SwimObserver swimObserver;
    private static final long ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L;
    private String SWIM = "140001";

    public SwimmingReport(HealthDataStore store) {
        mStore = store;
    }

    public void start(SwimObserver listener, String strDate, JSONObject jsonObject) {
        swimObserver = listener;
        HealthDataObserver.addObserver(mStore, HealthConstants.Exercise.HEALTH_DATA_TYPE, new HealthDataObserver(null) {
            @Override
            public void onChange(String s) {
                readTodaySwimData(strDate,jsonObject);
            }
        });

        readTodaySwimData(strDate,jsonObject);
    }

    private void readTodaySwimData(String strDate,JSONObject jsonObject) {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);
        long startTime = GlobalMethods.getEpochTime(strDate);
        long endTime = startTime + ONE_DAY_IN_MILLIS;

        HealthDataResolver.ReadRequest request = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.Exercise.HEALTH_DATA_TYPE)
                .setProperties(new String[]{HealthConstants.Exercise.EXERCISE_TYPE})
                .setLocalTimeRange(HealthConstants.Exercise.START_TIME, HealthConstants.Exercise.TIME_OFFSET,
                        startTime, endTime)
                .build();

        try {
            resolver.read(request).setResultListener(result ->{
                double distance = 0.0;
                try {
                    for (HealthData data : result) {
                        distance += data.getFloat(HealthConstants.Exercise.DISTANCE);
                    }
                } finally {
                    result.close();
                }
                if (swimObserver != null) {
                    swimObserver.onChanged(distance,strDate,jsonObject);
                }
            });
        } catch (Exception e) {
            Log.e("=> ", "Getting step count fails.", e);
        }
    }

    public interface SwimObserver {
        void onChanged(Double distance, String date, JSONObject jsonObject);
    }

}
