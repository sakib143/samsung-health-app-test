package com.example.samsung_health_test.samsung_health.NewData;

import android.util.Log;

import com.example.samsung_health_test.AppUtils.GlobalMethods;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthData;
import com.samsung.android.sdk.healthdata.HealthDataObserver;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataStore;

import org.json.JSONObject;

public class DrinkWaterReport {

    private final HealthDataStore mStore;
    private DrinkWaterObserver drinkWaterObserver;
    private static final long ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L;

    public DrinkWaterReport(HealthDataStore store) {
        mStore = store;
    }

    public void start(DrinkWaterObserver listener, String strDate,JSONObject jsonObject) {
        drinkWaterObserver = listener;
        HealthDataObserver.addObserver(mStore, HealthConstants.StepCount.HEALTH_DATA_TYPE, new HealthDataObserver(null) {
            @Override
            public void onChange(String s) {
                readTodayStepCount(strDate,jsonObject);
            }
        });
        readTodayStepCount(strDate,jsonObject);
    }

    private void readTodayStepCount(String strDate,JSONObject jsonObject) {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);
        long startTime = GlobalMethods.getEpochTime(strDate);
        long endTime = startTime + ONE_DAY_IN_MILLIS;

        HealthDataResolver.ReadRequest request = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.WaterIntake.HEALTH_DATA_TYPE)
                .setProperties(new String[]{HealthConstants.WaterIntake.AMOUNT})
                .setLocalTimeRange(HealthConstants.WaterIntake.START_TIME, HealthConstants.WaterIntake.TIME_OFFSET,
                        startTime, endTime)
                .build();

        try {
            resolver.read(request).setResultListener(result ->{
                double waterAmount = 0;
                try {
                    for (HealthData data : result) {
                        waterAmount  += GlobalMethods.miliLiterToOZ(data.getInt(HealthConstants.WaterIntake.AMOUNT));
                    }
                } finally {
                    result.close();
                }
                if (drinkWaterObserver != null) {
                    drinkWaterObserver.onChanged(waterAmount,strDate,jsonObject);
                }
            });
        } catch (Exception e) {
            Log.e("=> ", "Getting step count fails.", e);
        }
    }

    public interface DrinkWaterObserver {
        void onChanged(double drinkAmount, String date, JSONObject jsonObject);
    }

}
