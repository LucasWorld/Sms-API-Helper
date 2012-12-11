package com.afollestad.smshelper;

import android.net.Uri;

import com.afollestad.smshelper.Conversation.Column;

public class Constants {
	
	public static final String[] ALL_CONVERSATIONS_PROJECTION = new String[] { 
		Column._ID, Column.DATE, Column.MESSAGE_COUNT, Column.TYPE,
        Column.RECIPIENT_IDS, Column.SNIPPET, Column.SNIPPET_CHARSET, 
        Column.READ, Column.ERROR, Column.HAS_ATTACHMENT
	};
	
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
