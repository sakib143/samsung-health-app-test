package com.example.samsung_health_test.samsung_health;

import android.util.Log;

import com.example.samsung_health_test.AppUtils.GlobalMethods;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthData;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Iterator;

public class SleepReporter {
    private static final long ONE_DAY = 24 * 60 * 60 * 1000;
    private final HealthDataStore mStore;

    public SleepReporter(HealthDataStore store) {
        mStore = store;
    }

    public void getTodaySleepData(GetSleepSummury getSleepSummury, String selectedDate, JSONObject jsonObject) {

        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

        long dayTime = GlobalMethods.getEpochTime(selectedDate);
        long newTime = dayTime + ONE_DAY;

        HealthDataResolver.ReadRequest sleepRequest = new HealthDataResolver.ReadRequest.Builder()
                .setDataType("com.samsung.health.sleep")
                .setLocalTimeRange(HealthConstants.Sleep.START_TIME,
                        HealthConstants.Sleep.TIME_OFFSET, dayTime, newTime)
                .build();
        try {
            resolver.read(sleepRequest).setResultListener(result -> {
                try {
                    Iterator<HealthData> iter = result.iterator();
                    if (iter.hasNext()) {
                        HealthData data = iter.next();
                        long startTime = data.getLong(HealthConstants.Sleep.START_TIME);
                        long endTime = data.getLong(HealthConstants.Sleep.END_TIME);

                        Date startDate = new Date(startTime);
                        Date endDate = new Date(endTime);
                        String diffmin = GlobalMethods.getDiffMinute(startDate, endDate);
                        getSleepSummury.getSleepData(diffmin, jsonObject);
                    } else {
                        getSleepSummury.getSleepData("0", jsonObject);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    result.close();
                }
            });
        } catch (Exception e) {
            Log.e("=> ", "Getting step count fails.", e);
        }

    }

    public interface GetSleepSummury{
        void getSleepData(String totalSleepTime, JSONObject jsonObject) throws JSONException;
    }

}
