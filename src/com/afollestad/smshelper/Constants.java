package com.afollestad.smshelper;

import android.net.Uri;

public class Constants {
	
	private static final Uri BASE_CONVERSATIONS_URI = Uri.withAppendedPath(Uri.parse("content://mms-sms/"), "conversations");

	public static final Uri CONVERSATION_SMS_URI = Uri.withAppendedPath(Uri.parse("content://sms/"), "conversations"); 
	
	public static final Uri ALL_CONVERSATIONS_URI = BASE_CONVERSATIONS_URI.buildUpon()
            .appendQueryParameter("simple", "true").build();
	
	public static final Uri SMS_INBOX = Uri.parse("content://sms/inbox");
	
	public static final Uri SMS_DRAFTS = Uri.parse("content://sms/draft");
	
	public static final Uri SMS_SENT = Uri.parse("content://sms/sent");
	
	public static final Uri SMS_ALL = Uri.parse("content://sms");

	public static final Uri SMS_FAILED = Uri.parse("content://sms/failed");
	
	public static Uri ALL_CANONICAL = Uri.parse("content://mms-sms/canonical-addresses");
}
