package com.example.samsung_health_test.AppUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.example.samsung_health_test.R;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public class GlobalMethods {

    public static SimpleDateFormat mmddyyFormat = new SimpleDateFormat("MM/dd/yy");

    public static long getEpochTime(String myDate) {
        Calendar cal = Calendar.getInstance();
        try {
            Date tempDate = mmddyyFormat.parse(myDate);
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.setTime(tempDate);

            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int date = cal.get(Calendar.DATE);

            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DATE, date);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cal.getTimeInMillis();
    }

    public static String getDiffMinute(Date startDate, Date endDate) {
        //For Reference check below URL
        //https://crunchify.com/how-to-calculate-the-difference-between-two-java-date-instances/
        String diffMinute = "";
        String format = "MM/dd/yyyy hh:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        DecimalFormat crunchifyFormatter = new DecimalFormat("###,###");
        long diff = endDate.getTime() - startDate.getTime();
        int diffmin = (int) (diff / (60 * 1000));
        diffMinute = crunchifyFormatter.format(diffmin);
        return diffMinute;
    }

    public static String getCurrentDate() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        return df.format(c);
    }

    public static double meterToMile(double data) {
        return data * 0.00062137119;
    }

    public static void showConfirmAlert(Context context, String msg, DialogInterface.OnClickListener onYesClick) {
        new AlertDialog.Builder(context).setIcon(0).setTitle(context.getString(R.string.app_name)).setMessage(msg).setCancelable(true).setNegativeButton("NO", null)
                .setPositiveButton("YES", onYesClick).show();
    }

    public static List<Date> getBetweenDates(String dateString1, String dateString2) {
        List<Date> dates = new ArrayList<Date>();
        Date date1 = null;
        Date date2 = null;

        try {
            date1 = mmddyyFormat.parse(dateString1);
            date2 = mmddyyFormat.parse(dateString2);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        while (!cal1.after(cal2)) {
            dates.add(cal1.getTime());
            cal1.add(Calendar.DATE, 1);
        }
        return dates;
    }
}
