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

public class WaterDrinkReport {
    private static final String TAG = "Water drink report";

    private static final long ONE_DAY = 24 * 60 * 60 * 1000;
    private final HealthDataStore mStore;

    public WaterDrinkReport(HealthDataStore store) {
        mStore = store;
    }


    public void getDrinkData(GetWaterDrinkSummury waterDrinkSummury, String lastSyncDate, JSONObject jsonObject) {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

        long dayTime = GlobalMethods.getEpochTime(lastSyncDate);
        long newTime = dayTime + ONE_DAY;

        HealthDataResolver.ReadRequest swimRequest = new HealthDataResolver.ReadRequest.Builder()
                .setDataType("com.samsung.health.water_intake")
                .setLocalTimeRange(HealthConstants.WaterIntake.START_TIME,
                        HealthConstants.WaterIntake.TIME_OFFSET, dayTime, newTime)
                .setSort(HealthConstants.WaterIntake.UNIT_AMOUNT, HealthDataResolver.SortOrder.DESC)
                .setResultCount(0,1)
                .build();
        try {
            resolver.read(swimRequest).setResultListener(result -> {
                try {
                    Iterator<HealthData> iter = result.iterator();
                    if (iter.hasNext()) {
                        HealthData data = iter.next();
                        double drinkedWater = data.getFloat(HealthConstants.WaterIntake.AMOUNT);
                        waterDrinkSummury.getWaterDrinkData(drinkedWater,lastSyncDate,jsonObject);
                    }else {
                        waterDrinkSummury.getWaterDrinkData(0,lastSyncDate,jsonObject);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    result.close();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Getting exercise summary fails.", e);
        }
    }

    public interface GetWaterDrinkSummury{
        void getWaterDrinkData(double distance, String date, JSONObject objHealthData) throws JSONException;
    }
}
