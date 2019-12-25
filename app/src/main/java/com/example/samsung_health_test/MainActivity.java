package com.example.samsung_health_test;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.samsung_health_test.AppUtils.GlobalMethods;
import com.example.samsung_health_test.samsung_health.NewData.DrinkWaterReport;
import com.example.samsung_health_test.samsung_health.NewData.SleeepReportNew;
import com.example.samsung_health_test.samsung_health.NewData.StepCountReportNew;
import com.example.samsung_health_test.samsung_health.NewData.SwimmingReportNew;
import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {


    // TODO: 20-11-2019 Samsung Health App Related stuff by Sakib START
    public static final HealthPermissionManager.PermissionKey STEP_DAILY_TREND_READ_PERMISSION = new HealthPermissionManager.PermissionKey(
            HealthConstants.StepCount.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ);

    public static final HealthPermissionManager.PermissionKey EXERCISE_READ_PERMISSION = new HealthPermissionManager.PermissionKey(
            HealthConstants.Exercise.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ);

    public static final HealthPermissionManager.PermissionKey SLEEP_PERMISSION = new HealthPermissionManager.PermissionKey(
            HealthConstants.Sleep.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ);

    public static final HealthPermissionManager.PermissionKey WATER_DRINK_PERMISSIONS = new HealthPermissionManager.PermissionKey(
            HealthConstants.WaterIntake.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ);


    private HealthDataStore mStore;
    private boolean mIsStoreConnected;
    private Set<HealthPermissionManager.PermissionKey> mKeys = new HashSet<>();

    {
        mKeys.add(STEP_DAILY_TREND_READ_PERMISSION);
        mKeys.add(EXERCISE_READ_PERMISSION);
        mKeys.add(SLEEP_PERMISSION);
        mKeys.add(WATER_DRINK_PERMISSIONS);
    }

    private String strLastSyncDate = "12/21/2019";
    private JSONObject objLastSychHealthData;
    private JSONArray arrayLastData;
    private JSONObject objHealthData;

    private StepCountReportNew stepCountReportNew;
    private SwimmingReportNew swimmingReportNew;
    private DrinkWaterReport drinkWaterReport;
    private SleeepReportNew sleeepReportNew;
    // TODO: 20-11-2019 Samsung Health App Related stuff by Sakib END


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStore = new HealthDataStore(MainActivity.this, mConnectionListener);


    }

    @Override
    protected void onResume() {
        super.onResume();
        mStore.connectService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mStore.disconnectService();
    }

    // TODO: 20-11-2019 Samsung Health Related stuff by Sakib START
    private final HealthDataStore.ConnectionListener mConnectionListener = new HealthDataStore.ConnectionListener() {
        @Override
        public void onConnected() {
            mIsStoreConnected = true;
            stepCountReportNew = new StepCountReportNew(mStore);
            swimmingReportNew = new SwimmingReportNew(mStore);
            drinkWaterReport = new DrinkWaterReport(mStore);
            sleeepReportNew = new SleeepReportNew(mStore);
            if (isPermissionAcquired()) {
                synchHealthAppData();
            } else {
                requestHealthPermission();
            }
        }

        @Override
        public void onConnectionFailed(HealthConnectionErrorResult error) {
            showConnectionFailureDialog(error);
        }

        @Override
        public void onDisconnected() {
            mIsStoreConnected = false;
            mStore.connectService();
        }
    };

    private void requestHealthPermission() {
        HealthPermissionManager pmsManager = new HealthPermissionManager(mStore);
        try {
            // Show user permission UI for allowing user to change options
            pmsManager.requestPermissions(mKeys, MainActivity.this).setResultListener(result -> {
                Map<HealthPermissionManager.PermissionKey, Boolean> resultMap = result.getResultMap();
                if (resultMap.containsValue(Boolean.FALSE)) {
                    showPermissionAlarmDialog();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isPermissionAcquired() {
        HealthPermissionManager pmsManager = new HealthPermissionManager(mStore);
        boolean isPermissionAllowed = false;
        try {
            Map<HealthPermissionManager.PermissionKey, Boolean> resultMap = pmsManager.isPermissionAcquired(mKeys);
            Iterator it = resultMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                System.out.println(pairs.getKey() + " = " + pairs.getValue());
                isPermissionAllowed = (boolean) pairs.getValue();
                break;
            }
            return isPermissionAllowed;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showPermissionAlarmDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle(getResources().getString(R.string.app_name))
                .setMessage(getResources().getString(R.string.health_permission_need))
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void showConnectionFailureDialog(final HealthConnectionErrorResult error) {
        String message = "";
        switch (error.getErrorCode()) {
            case HealthConnectionErrorResult.PLATFORM_NOT_INSTALLED:
                message = getResources().getString(R.string.msg_req_install);
                break;
            case HealthConnectionErrorResult.OLD_VERSION_PLATFORM:
                message = getResources().getString(R.string.msg_req_upgrade);
                break;
            case HealthConnectionErrorResult.PLATFORM_DISABLED:
                message = getResources().getString(R.string.msg_req_enable);
                break;
            case HealthConnectionErrorResult.USER_AGREEMENT_NEEDED:
                message = getResources().getString(R.string.msg_req_agree);
                break;
            default:
                message = getResources().getString(R.string.msg_req_available);
                break;
        }

        GlobalMethods.showConfirmAlert(MainActivity.this, message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (error.hasResolution()) {
                    error.resolve(MainActivity.this);
                }
            }
        });
    }


    private void synchHealthAppData() {
        try {
            if (!mIsStoreConnected) {
                return;
            }
//            if (strLastSyncDate.equalsIgnoreCase("")) {
//                strLastSyncDate = GlobalMethods.getCurrentDate();
//            }
            Date startDate = GlobalMethods.mmddyyFormat.parse(strLastSyncDate);
            Date endDate = GlobalMethods.mmddyyFormat.parse(GlobalMethods.getCurrentDate());

            Calendar start = Calendar.getInstance();
            start.setTime(startDate);
            Calendar end = Calendar.getInstance();
            end.setTime(endDate);


            List<Date> listDates = GlobalMethods.getBetweenDates(strLastSyncDate, GlobalMethods.getCurrentDate());
            for (Date date : listDates) {
                String strDate = GlobalMethods.mmddyyFormat.format(date);
                arrayLastData = new JSONArray();
                objLastSychHealthData = new JSONObject();
                objHealthData = new JSONObject();
                swimmingReportNew.start(swimObserver,strDate,objHealthData);
            }

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // TODO: 20-11-2019 Call this api after 5 second due to data fetching takes some time
                    //callSaveDeviceTrackingData();//
                    Log.e("=>"," Sync data ==> " + objLastSychHealthData);
                }
            }, 5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // TODO: 20-11-2019 Samsung Health Related stuff by Sakib END

    private DrinkWaterReport.DrinkWaterObserver drinkWaterObserver = (drinkAmount,date,inputParam)-> {
        try {
            Log.d("=> ", "Drink water reported : Date " +date + ", drinkAmount " + drinkAmount);
            inputParam.put("waterQuantity ", drinkAmount);
            inputParam.put("activityDate", date);
            arrayLastData.put(inputParam);
            objLastSychHealthData.put("array", arrayLastData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    };

    private SleeepReportNew.SleepObserver sleepObserver = (totalSleepMinute , date,inputParam)->{
        try {
            Log.d("=> ", "Sleep reported : Date " +date + ", sleepData " + totalSleepMinute);
            inputParam.put("sleepMinutes", totalSleepMinute);
            inputParam.put("activityDate", date);
            drinkWaterReport.start(drinkWaterObserver,date,inputParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    private StepCountReportNew.StepCountObserver mStepCountObserver = (count,distance,date,inputParam)-> {
        try {
            Log.d("=> ", "Step reported : " + count + " date ->"+ date +", Distance " + distance);
            inputParam.put("steps", count);
            inputParam.put("stepsDistance", distance);
            inputParam.put("activityDate", date);
            sleeepReportNew.start(sleepObserver,date,inputParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    private SwimmingReportNew.SwimObserver swimObserver = (distance, date,inputParam) ->{
        try {
            Log.d("=> ", "Swimming reported : Date " +date + ", Distance " +distance);
            inputParam.put("swimDistance", distance);
            inputParam.put("activityDate", date);
            stepCountReportNew.start(mStepCountObserver,date,inputParam);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    };
}
