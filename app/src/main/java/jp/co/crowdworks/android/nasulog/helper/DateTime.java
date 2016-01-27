package jp.co.crowdworks.android.nasulog.helper;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import jp.co.crowdworks.android.nasulog.R;

public class DateTime {
    private static final String TAG=DateTime.class.getName();

    private static final SimpleDateFormat sSimpleTimeFormat = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private static final SimpleDateFormat sSimpleDayFormat = new SimpleDateFormat("MM/dd");
    private static final SimpleDateFormat sSimpleDayTimeFormat = new SimpleDateFormat("MM/dd HH:mm");
    private static final SimpleDateFormat sSimpleDateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    public enum Format{
        /**
         * 日付(yyyy/mm/dd)のみ
         */
        DATE

        /**
         * 日付(mm/dd)のみ
         */
        ,DAY

        /**
         * 時刻のみ
         */
        ,TIME

        /**
         * 日付(yyyy/mm/dd)＋時刻
         */
        ,DATE_TIME

        /**
         * 日付(mm/dd)＋時刻
         */
        ,DAY_TIME

        /**
         * 当日中は日付(mm/dd)＋時刻、〜前日　もしくは未来の日付であれば日付
         */
        , AUTO_DAY_TIME

    }

    public static String fromEpocMs(long epocMs, Format format){
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("JST"));
        cal.setTimeInMillis(epocMs);

        if(format==Format.DAY) return sSimpleDayFormat.format(cal.getTime());
        if(format==Format.DATE) return sSimpleDateFormat.format(cal.getTime());
        if(format==Format.TIME) return sSimpleTimeFormat.format(cal.getTime());
        if(format==Format.DATE_TIME) return sSimpleDateTimeFormat.format(cal.getTime());
        if(format==Format.DAY_TIME) return sSimpleDayTimeFormat.format(cal.getTime());
        if(format==Format.AUTO_DAY_TIME){
            final long curTimeMs = System.currentTimeMillis();
            Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("JST"));
            cal2.setTimeInMillis(curTimeMs);

            if(cal.get(Calendar.YEAR)==cal2.get(Calendar.YEAR)
                    && cal.get(Calendar.DAY_OF_YEAR)==cal2.get(Calendar.DAY_OF_YEAR)){
                //same day.
                return sSimpleDayTimeFormat.format(cal.getTime());
            }
            else {
                return sSimpleDayFormat.format(cal.getTime());
            }
        }

        throw new IllegalArgumentException();
    }

    public static long fromDateToEpocMs(String dateString){
        try {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("JST"));
            cal.setTime(sSimpleDateFormat.parse(dateString));
            return cal.getTimeInMillis();
        } catch (ParseException e) {
        }
        return 0;
    }



    public static String timeAgo(Context context, long millis) {
        long diff = System.currentTimeMillis() - millis;


        double seconds = Math.abs(diff) / 1000;
        double minutes = seconds / 60;
        double hours = minutes / 60;
        double days = hours / 24;
        double years = days / 365;

        String words;

        if (seconds < 45) {
            words = context.getString(R.string.time_ago_seconds, Math.round(seconds));
        } else if (seconds < 90) {
            words = context.getString(R.string.time_ago_minute, 1);
        } else if (minutes < 45) {
            words = context.getString(R.string.time_ago_minutes, Math.round(minutes));
        } else if (minutes < 90) {
            words = context.getString(R.string.time_ago_hour, 1);
        } else if (hours < 24) {
            words = context.getString(R.string.time_ago_hours, Math.round(hours));
        } else if (hours < 42) {
            words = context.getString(R.string.time_ago_day, 1);
        } else if (days < 30) {
            words = context.getString(R.string.time_ago_days, Math.round(days));
        } else if (days < 45) {
            words = context.getString(R.string.time_ago_month, 1);
        } else if (days < 560/*originally 365*/) {
            words = context.getString(R.string.time_ago_months, Math.round(days / 30));
        } else if (years < 1.5) {
            words = context.getString(R.string.time_ago_year, 1); //使わない.
        } else {
            words = context.getString(R.string.time_ago_years, Math.round(years));
        }

        return words;
    }
}
