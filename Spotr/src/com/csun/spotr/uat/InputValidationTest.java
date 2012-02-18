package com.csun.spotr.uat;

import com.csun.spotr.util.InputValidationHelper;

/**
 * By Chan's request, creating this to be used as a 
 * test case, possibly by Fitnesse.
 * @author Churro
 */
public class InputValidationTest {
	
	String eMail = "charitha.sathkumara.0@my.csun.edu";
	String phone = "(818) 268-3529";
	String IPAddr = "127.0.0.1";
	double latitude = 34.241856, longitude = -118.528275;
	String date = "2/18/2012";
	String time = "2:01 PM";
	
	public boolean checkEmail() {
		return InputValidationHelper.isValidEmail(eMail);
	}
	
	public boolean checkPhone() {
		return InputValidationHelper.isValidPhone(phone);
	}
	
	public boolean checkIP() {
		return InputValidationHelper.isValidIPAddr(IPAddr);
	}
	
	public boolean checkLatitude() {
		return InputValidationHelper.isValidLatLong(latitude);
	}
	
	public boolean checkLongitude() {
		return InputValidationHelper.isValidLatLong(longitude);
	}
	
	public boolean checkDate() {
		return InputValidationHelper.isValidDate(date);
	}
	
	public boolean checkTime() {
		return InputValidationHelper.isValidTime(time);
	}
	
}
