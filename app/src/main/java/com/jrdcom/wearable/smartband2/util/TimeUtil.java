package com.jrdcom.wearable.smartband2.util;

import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtil {
	
	private static final String TAG = "TimeUtil";
	
	public static long getDayStart() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.setTimeInMillis(0);
		calendar.set(year, month, day, 0, 0, 0);
		return calendar.getTimeInMillis();
	}

    public static long getTimeInMillisStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(0);
        calendar.set(2000, 0, 1, 0, 0, 0);
        return calendar.getTimeInMillis();
    }

	public static long getDurationStart() {
		
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		
		int durationStartMinute = minute - minute % 15;
        calendar.setTimeInMillis(0);
		calendar.set(year, month, day, hour, durationStartMinute, 0);
		
		return calendar.getTimeInMillis();
	}
	
	public static long getDurationStart(long timestamp) {
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp);
		
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		
		int durationStartMinute = minute - minute % 15;
        calendar.setTimeInMillis(0);
		calendar.set(year, month, day, hour, durationStartMinute, 0);
		
		return calendar.getTimeInMillis();
	}
	
	public static long getNextDurationStart() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		
		int durationNextStartMinute = minute + (15 - minute % 15);
        calendar.setTimeInMillis(0);
		calendar.set(year, month, day, hour, durationNextStartMinute, 0);
		
		return calendar.getTimeInMillis();
	}
	
	public static boolean isDurationStart() {
		Calendar calendar = Calendar.getInstance();
		int minute = calendar.get(Calendar.MINUTE);
		
		if (minute % 15 == 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 得到几天前0点的时间戳
	 * @param days
	 * 			天数
	 * @return
	 * 			时间戳
	 */
	public static long getDayStartTimestamp(int days) {
		long startTimestampS = getDayStartTimestamp();
		long durationTimeStamp = days * DateUtils.DAY_IN_MILLIS;
		return startTimestampS - durationTimeStamp;
	}
	
	/**
	 * 得到某个时间戳所在天的0点的时间戳
	 * @param timestamp
	 * 			时间戳
	 * @return
	 * 			0点时间戳
	 */
	public static long getDayStartTimestamp(long timestamp) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.setTimeInMillis(0);
		calendar.set(year, month, day, 0, 0, 0);
		return calendar.getTimeInMillis();
	}
	
	/**
	 * 得到几天前的日期
	 * @param days
	 * 			天数
	 * @return
	 * 			日期字符串
	 */
	public static String getDateBeforeDays(int days) {
//		Calendar calendar = Calendar.getInstance();
		long durationTimeStamp = days * DateUtils.DAY_IN_MILLIS;
		long beforeTimeStamp = System.currentTimeMillis() - durationTimeStamp;
		
		return getDate(beforeTimeStamp);
	}
	
	/**
	  * get date from timestamp
	  * 
	  * @return date format yyyy-MM-dd
	  */
	public static String getDate(long timestamp) {
		Date date = new Date(timestamp);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(date);
	}

    public static String getDateTime(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss .SSS ");
        return formatter.format(date);
    }
    public static String getTimeMouthDayLine(long timestamp)
    {
        Date date = new Date(timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd");
        return formatter.format(date);
    }
    public static String getTimeLine(long timestamp)
    {
        Date date = new Date(timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        return formatter.format(date);
    }
	/**
	  * get timestamp from date
	  * 
	  * @return timestamp
	  */
	public static long getTimestamp(String dateStr) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = formatter.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			return -1;
		}
		return date.getTime();
	}
	
	public static String timestampToDate(long timestamp) {		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp);
		
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		
		LogUtil.d(TAG,"year:" + year);
		LogUtil.d(TAG,"month:" + month);
		LogUtil.d(TAG,"day:" + day);
		String yearStr = String.valueOf(year);
		String monthStr = String.valueOf(month);
		if(month < 10 && month >0) {
			monthStr = "0" + monthStr;
		} 
		
		String dayStr = String.valueOf(day);
		if(day > 0 && day < 10) {
			dayStr = "0" + dayStr;
		} 
		
		return yearStr + "-" + monthStr + "-" + dayStr;
	}
	
	public static String timestampToDateByMonth(long timestamp) {		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp);
		
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		String monthStr = String.valueOf(month);
		if(month < 10 && month >0) {
			monthStr = "0" + monthStr;
		} 
		
		String dayStr = String.valueOf(day);
		if(day > 0 && day < 10) {
			dayStr = "0" + dayStr;
		} 
		
		return monthStr + "月" + dayStr;
	}
	
	public static long dateToTimestamp(String date) {
		
		int year = Integer.valueOf(date.substring(0, 3));
		int month = Integer.valueOf(date.substring(4, 5));
		int day = Integer.valueOf(date.substring(6, 7));
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day);
		
		return calendar.getTimeInMillis()/1000;
	}
	
	public static int getCurrentYear() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.YEAR);
	}

    public static int getCurrentHour() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static int getCurrentYearMonthDay() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return (year*10000+month*100+day);
    }
	
	public static long getDayStartTimestamp() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.setTimeInMillis(0);
		calendar.set(year, month, day, 0, 0, 0);
		return calendar.getTimeInMillis();
	}
	
	public static long getMonthStartTimestamp() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = 1;
        calendar.setTimeInMillis(0);
		calendar.set(year, month, day);
		return calendar.getTimeInMillis()/1000;
	}
	
	public static int getDayCountForCurrentMonth() {
		Calendar calendar = Calendar.getInstance();
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}
	
	public static String getLastYear() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR) - 1;
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		
		String yearStr = String.valueOf(year);
		String monthStr;
		
		if(month < 10 && month > 0) {
			monthStr = "0" + String.valueOf(month);
		} else {
			monthStr = String.valueOf(month);
		}
		
		String dayStr = String.valueOf(day);
		
		return yearStr + monthStr + dayStr; 
	}
	
	public static int daysBetween(Calendar startDate, Calendar endDate) {  
		 Calendar date = (Calendar) startDate.clone();  
		 int daysBetween = 0;
		 while (date.before(endDate)) {  
		       date.add(Calendar.DAY_OF_MONTH, 1);  
		       daysBetween++;  
		    }  
		    return daysBetween;  
		}
	
	public static int monthBetween(Calendar startDate, Calendar endDate){
		int startYear = startDate.get(Calendar.YEAR);
		int endYear = endDate.get(Calendar.YEAR);
		return (endYear - startYear)*12+endDate.get(Calendar.MONTH)-startDate.get(Calendar.MONTH);
	}

    public static boolean isSameDay(long frontTimeStamp, long timeStamp) {
        return getDayStartTimestamp(frontTimeStamp) == getDayStartTimestamp(timeStamp);
    }


	public static  int getYear(long millis)
    {
    /*    Date date  = new Date();
        date.setTime(millis);
        return date.getYear()-100;*/
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.YEAR)-2000;
    }

	public static  int getMonth(long millis)
    {
 /*       Date date  = new Date();
        date.setTime(millis);
        return date.getMonth()+1;*/
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.MONTH)+1;
    }

	public static  int getDay(long millis)
    {
  /*      Date date  = new Date();
        date.setTime(millis);
        return date.getDate();*/
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }
	
    public static  int getYearByTimeStanp(long millis)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.YEAR);
    }
}
