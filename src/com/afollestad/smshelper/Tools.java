package com.afollestad.smshelper;

import java.util.Calendar;
import java.util.Date;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

/**
 * Various tools used by the SMS library.
 * @author Aidan Follestad
 */
public class Tools {

	public final static Long getLong(Cursor c, String col) {
		return c.getLong(c.getColumnIndex(col));
	}

	public final static int getInt(Cursor c, String col) {
		return c.getInt(c.getColumnIndex(col));
	}

	public final static String getString(Cursor c, String col) {
		return c.getString(c.getColumnIndex(col));
	}

	public final static boolean getBoolean(Cursor c, String col) {
		return getInt(c, col) == 1;
	}

	public static Uri getContactPhotoUri(ContentResolver cr, Long id) {
		return ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
	}

	public final static Calendar getDateSeconds(Cursor c, String col) {
		Calendar toreturn = Calendar.getInstance();
		toreturn.setTime(new Date(Long.parseLong(Tools.getString(c, col)) * 1000));
		return toreturn;
	}

	public final static Calendar getDateMilliSeconds(Cursor c, String col) {
		Calendar toreturn = Calendar.getInstance();
		toreturn.setTime(new Date(Long.parseLong(Tools.getString(c, col))));
		return toreturn;
	}

	public static String encodeSQL(String s) {
		return s.replaceAll("'", "''");
	}

	/**
	 * Strips a phone number string of parantheses, hyphens, and spaces, leaving only the digits.
	 */
	public static String stripNumber(String number) {
		return number.replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
	}
}