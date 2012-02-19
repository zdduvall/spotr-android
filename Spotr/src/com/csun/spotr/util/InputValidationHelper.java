package com.csun.spotr.util;

import java.util.regex.Pattern;

/**
 * Use this class to validate user input
 * @author cas81673
 */
public class InputValidationHelper {
	
	/**
	 * Way too hard to check for, just returning true for now.
	 * @param address
	 * @return
	 */
	public static boolean isValidAddress(String address) {
		//TODO: Try to implement again later.
		return true;
	}
	
	/**
	 * Checks for a standard US phone number with or without area code.
	 * <b>Eg:</b>15615552323 | 1-561-555-1212 | 5613333 | 5551212 | 614555-1212 
	 * | (614)555-1212
	 * @param phone The String to check for.
	 * @return <code>true</code> if input is valid.
	 */
	public static boolean isValidPhone(String phone) {
		return Pattern.matches("^(1?(-?\\d{3})-?)?(\\d{3})(-?\\d{4})$", phone) || 
				Pattern.matches("^([\\(]{1}[0-9]{3}[\\)]{1}[\\.| |\\-]{0,1}|" +
						"^[0-9]{3}[\\.|\\-| ]?)?[0-9]{3}(\\.|\\-| )?[0-9]{4}$", phone);
	}
	
	/**
	 * Matches a standard IPv4 address (Unfortunately, only checks for four 
	 * 3-digit groups. This means 999.999.999.999 would work).
	 * @param IP The String to check for.
	 * @return <code>true</code> if input is valid.
	 */
	public static boolean isValidIPAddr(String IP) {
		return Pattern.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}", IP);
	}
	
	/**
	 * Matches an e-mail. Works for 99.9% of RFC 2822 addresses. 
	 * (Source:http://www.regular-expressions.info/email.html)
	 * @param email The String to check for
	 * @return <code>true</code> if input is valid.
	 */
	public static boolean isValidEmail(String email) {
		return Pattern.matches("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$" +
				"%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)" +
				"+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", email);
	}
	
	/**
	 * Checks for a valid longitude coordinate. Can match doubles in the form 
	 * of (+/-)ddd.dddddd
	 * @param longitude The String to check for
	 * @return <code>true</code> if input is valid.
	 */
	public static boolean isValidLatLong(double longitude) {
		return Pattern.matches("-?\\d{1,3}\\.\\d+", Double.toString(longitude));
	}
	
	
	public static boolean isValidPassword(String password) {
		//TODO: Develop criteria, so it can be checked.
		return true;
	}
	
	public static boolean isValidDate(String dateTime) {
		return isValidDT1(dateTime) || isValidDT2(dateTime);
	}
	
	
	public static boolean isValidTime(String time) {
		return isValid24Hr(time) || isValid12HrOr24Hr(time);
	}
	
	////////////////////////////// Date Checkers //////////////////////////////
	/**
	 * Matches a date time in the MM/DD/YYYY or MM/DD/YYYY hh:mm:ss format.<br />
	 * <b>Eg:</b> 12/3/2012 | 3/31/2012 3:33 pm | 3/3/2003 3:33:33 am
	 * @param dateTime The String to check for.
	 * @return <code>true</code> if input is valid.
	 */
	private static boolean isValidDT1(String dateTime) {
		return Pattern.matches("^((((([13578])|(1[0-2]))[\\-\\/\\s]?(([1-9])" +
				"|([1-2][0-9])|(3[01])))|((([469])|(11))[\\-\\/\\s]?(([1-9])" +
				"|([1-2][0-9])|(30)))|(2[\\-\\/\\s]?(([1-9])|([1-2][0-9]))))" +
				"[\\-\\/\\s]?\\d{4})(\\s((([1-9])|(1[02]))\\:([0-5][0-9])((" +
				"\\s)|(\\:([0-5][0-9])\\s))([AM|PM|am|pm]{2,2})))?$", dateTime);
	}
	
	/**
	 * Another date time checker, in case the first one doesn't work.
	 * <b>Eg:</b>12/31/2002 | 12/31/2002 08:00 | 12/31/2002 08:00 AM
	 * @param dateTime The String to check for.
	 * @return <code>true</code> if input is valid.
	 */
	private static boolean isValidDT2(String dateTime) {
		return Pattern.matches("^([0]\\d|[1][0-2])\\/([0-2]\\d|[3][0-1])\\/(" +
				"[2][01]|[1][6-9])\\d{2}(\\s([0]\\d|[1][0-2])(\\:[0-5]\\d)" +
				"{1,2})*\\s*([aApP][mM]{0,2})?$", dateTime);
	}
	
	
	////////////////////////////// Time Checkers //////////////////////////////
	/**
	 * Matches a 24 HR HH:MM:SS format, up to 23:59:59
	 * @param time The String to check for.
	 * @return <code>true</code> if input is valid.
	 */
	private static boolean isValid24Hr(String time) {
		return Pattern.matches("(([0-1][0-9])|([2][0-3])):([0-5][0-9]):([0-5][0-9])", time);
	}
	
	/**
	 * Matches a 24 HR HH:MM:SS format OR a 12 HR HH:MM:SS AM/PM format, 
	 * up to 23:59:59 <br/> <b>Eg:</b>1:01 AM | 23:52:01 | 03.24.36 AM
	 * @param time The String to check for.
	 * @return <code>true</code> if input is valid.
	 */
	private static boolean isValid12HrOr24Hr(String time) {
		return Pattern.matches("^((([0]?[1-9]|1[0-2])(:|\\.)[0-5][0-9]((:|\\" +
				".)[0-5][0-9])?( )?(AM|am|aM|Am|PM|pm|pM|Pm))|(([0]?[0-9]|1[" +
				"0-9]|2[0-3])(:|\\.)[0-5][0-9]((:|\\.)[0-5][0-9])?))$", time);
	}
}
