package com.example.min.jvideoplay.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 日期工具类
 *
 * @author zengliang
 */
public class DateUtil {
	
	// 日期类型
		/**
		 * 如2014-12-12
		 */
		public static final String DATE_TYPE_Y_M_D = "yyyy-MM-dd";
		// 日期类型
		/**
		 * 如20141212
		 */
		public static final String DATE_TYPE_YMD = "yyyyMMdd";
		/**
		 * 如12-12
		 */
		public static final String DATE_TYPE_MD = "M-d";
		/**
		 * 如12:30
		 */
		public static final String DATE_TYPE_HM = "HH:mm";
		/**
		 * 12-12 12:30
		 */
		public static final String DATE_TYPE_MD_HM = "MM-dd HH:mm";
		/**
		 * 20141212093000
		 */
		public static final String DATE_TYPEYMDHMS = "yyyyMMddHHmmss";
		/**
		 * 20141212 12:30:00
		 */
		public static final String DATE_TYPE_YMDHMS = "yyyyMMdd HH:mm:ss";
		/**
		 * 2014-12-12 12:30
		 */
		public static final String DATE_TYPE_Y_M_DHM = "yyyy-MM-dd HH:mm";
		/**
		 * 2014-12-12 12:30:30
		 */
		public static final String DATE_TYPE_Y_M_DHMS = "yyyy-MM-dd HH:mm:ss";
	
	
	
    /**
     * 将long类型时间转换为指定格式时间字符
     *
     * @param seconds 默认格式 yyyyMMdd
     * @return
     */
    public static String getTimeString(long seconds) {
        return getTimeString(seconds, DATE_TYPE_YMD);
    }

    /**
     * 将long类型时间转换为指定格式时间字符
     *
     * @param seconds
     * @param type    MM月dd日 HH:mm
     *                HH:mm
     *                yyyyMMdd
     * @return
     */
    public static String getTimeString(long seconds, String type) {
        SimpleDateFormat formatter = new SimpleDateFormat(type, Locale.getDefault());
        Date currentTime = new Date(getMilliseconds(seconds));
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    /**
     * 将Date类型时间转换为指定格式时间字符
     *
     * @param date
     * @param type MM月dd日 HH:mm
     *             HH:mm
     *             yyyyMMdd
     * @return
     */
    public static String getTimeString(Date date, String type) {
        SimpleDateFormat formatter = new SimpleDateFormat(type, Locale.getDefault());
        String dateString = formatter.format(date);
        return dateString;
    }

    /**
     * 将String类型时间转换为指定格式时间字符
     *
     * @param time       传入的参数字符串时间，
     * @param sourceType 传入的参数格式
     * @param destiType  将要转成日期格式
     *                   MM月dd日 HH:mm
     *                   HH:mm
     *                   yyyyMMdd
     * @return
     */
    public static String getTimeString(String time, String sourceType, String destiType) {

        //首先将当前字符串时间转换为毫秒值，
        long timeLong = getMilliseconds(time, sourceType);
        return getTimeString(timeLong, destiType);
    }

    /**
     * 将时间秒值或者毫秒值 转换 为毫秒值
     *
     * @param time
     * @return
     */
    public static long getMilliseconds(long time) {
        int length = String.valueOf(time).length();
        if (length == 10) {// 时间为秒数 转换为毫秒数
            return time * 1000;
        } else if (length == 13) {// 时间为毫秒数
            return time;
        }
        return time;
    }

    /**
     * 将字符串时间转换为毫秒值
     *
     * @param dateTime 默认类型  yyyyMMdd
     * @return
     */
    public static long getMilliseconds(String dateTime) {
        return getMilliseconds(dateTime, DATE_TYPE_YMD);
    }

    /**
     * 将字符串时间转换为毫秒值
     *
     * @param dateTime
     * @param type     类型
     *                 yyyyMMdd
     *                 yyyyMMdd HH:mm:ss
     * @return
     */
    public static long getMilliseconds(String dateTime, String type) {
        try {
            DateFormat format = new SimpleDateFormat(type, Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(format.parse(dateTime));
            return calendar.getTimeInMillis();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取当天0点的毫秒值
     *
     * @return
     */
    public static long getDayStartMilliseconds() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.AM_PM, Calendar.AM);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取某一天0点的毫秒值
     *
     * @param dayTimeMillis 某一天时间内的某一个毫秒值
     * @return
     */
    public static long getDayStartMilliseconds(long dayTimeMillis) {
        return getMilliseconds(getTimeString(dayTimeMillis));
    }

}
