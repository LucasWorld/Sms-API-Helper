package com.afollestad.smshelper;

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
}