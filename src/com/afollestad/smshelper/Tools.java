package com.afollestad.smshelper;

import java.util.Calendar;

/**
 * Various tools used by the SMS library.
 * @author Aidan Follestad
 */
public class Tools {
	
	/**
	 * Strips a phone number string of parantheses, hyphens, and spaces, leaving only the digits.
	 */
	public static String stripNumber(String number) {
		return number.replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
	}
	
	public static String friendlyTimeLong(Calendar time) {
		String am_pm = " am";
		if (time.get(Calendar.AM_PM) == Calendar.PM)
			am_pm = " pm";
		String day = Integer.toString(time.get(Calendar.DAY_OF_MONTH));
		String minute = Integer.toString(time.get(Calendar.MINUTE));
		int hour = time.get(Calendar.HOUR);
		if (hour == 0)
			hour = 12;
		if (minute.length() == 1)
			minute = ("0" + minute);
		return hour + ":" + minute + am_pm + " " + convertMonth(time.get(Calendar.MONTH), false) + " " + day + ", " + time.get(Calendar.YEAR);
	}

	public static String friendlyTime(Calendar time) {
		Calendar now = Calendar.getInstance();
		String am_pm = " am";
		if (time.get(Calendar.AM_PM) == Calendar.PM)
			am_pm = " pm";
		String day = Integer.toString(time.get(Calendar.DAY_OF_MONTH));
		String minute = Integer.toString(time.get(Calendar.MINUTE));
		int hour = time.get(Calendar.HOUR);
		if (hour == 0)
			hour = 12;
		if (minute.length() == 1)
			minute = ("0" + minute);
		if (now.get(Calendar.YEAR) == time.get(Calendar.YEAR)) {
			if (now.get(Calendar.MONTH) == time.get(Calendar.MONTH)) {
				if (now.get(Calendar.WEEK_OF_MONTH) == time
						.get(Calendar.WEEK_OF_MONTH)) {
					if(now.get(Calendar.DAY_OF_MONTH) == time.get(Calendar.DAY_OF_MONTH)) {
						return hour + ":" + minute + am_pm;
					} else {
						int nowDay = now.get(Calendar.DAY_OF_YEAR);
						int timeDay = time.get(Calendar.DAY_OF_YEAR);
						if(nowDay == 1 && timeDay == 365) {
							return "yesterday";
						} else if((nowDay - 1) == timeDay) {
							return "yesterday";
						}
						return convertMonth(time.get(Calendar.MONTH), true) + " " + day;
					}
				} else {
					return convertMonth(time.get(Calendar.MONTH), true) + " " + day;
				}
			} else {
				return convertMonth(time.get(Calendar.MONTH), true) + " " + day;
			}
		} else {
			String year = Integer.toString(time.get(Calendar.YEAR));
			if (now.get(Calendar.YEAR) < time.get(Calendar.YEAR))
				year = year.substring(1, 3);
			return convertMonth(time.get(Calendar.MONTH), true) + " " + day + ", " + year;
		}
	}

	private static String convertMonth(int month, boolean isShort) {
		String toReturn = "";
		switch (month) {
		case Calendar.JANUARY:
			toReturn = "January";
			if (isShort)
				toReturn = "Jan";
			break;
		case Calendar.FEBRUARY:
			toReturn = "February";
			if (isShort)
				toReturn = "Feb";
			break;
		case Calendar.MARCH:
			toReturn = "March";
			if (isShort)
				toReturn = "Mar";
			break;
		case Calendar.APRIL:
			toReturn = "April";
			if (isShort)
				toReturn = "Apr";
			break;
		case Calendar.MAY:
			toReturn = "May";
			break;
		case Calendar.JUNE:
			toReturn = "June";
			if (isShort)
				toReturn = "Jun";
			break;
		case Calendar.JULY:
			toReturn = "July";
			if (isShort)
				toReturn = "Jul";
			break;
		case Calendar.AUGUST:
			toReturn = "August";
			if (isShort)
				toReturn = "Aug";
			break;
		case Calendar.SEPTEMBER:
			toReturn = "September";
			if (isShort)
				toReturn = "Sep";
			break;
		case Calendar.OCTOBER:
			toReturn = "October";
			if (isShort)
				toReturn = "Oct";
			break;
		case Calendar.NOVEMBER:
			toReturn = "November";
			if (isShort)
				toReturn = "Nov";
			break;
		case Calendar.DECEMBER:
			toReturn = "December";
			if (isShort)
				toReturn = "Dec";
			break;
		}
		return toReturn;
	}
}