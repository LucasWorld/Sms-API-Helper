package com.afollestad.smshelper;

import android.net.Uri;

public class Constants {
	
	private static final Uri BASE_CONVERSATIONS_URI = Uri.withAppendedPath(Uri.parse("content://mms-sms/"), "conversations");
	
	public static final Uri ALL_CONVERSATIONS_URI = BASE_CONVERSATIONS_URI.buildUpon()
			.appendQueryParameter("simple", "false").build();
	
	public static final Uri MMS_PART = Uri.parse("content://mms/part");
	
	public static final Uri SMS_INBOX = Uri.parse("content://sms/inbox");
	
	public static final Uri SMS_SENT = Uri.parse("content://sms/sent");
	
	public static final Uri SMS_ALL = Uri.parse("content://sms");
	
	public static final Uri MMS_ALL = Uri.parse("content://mms");
	
	public static Uri ALL_CANONICAL = Uri.parse("content://mms-sms/canonical-addresses");
}
