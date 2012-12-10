package com.afollestad.smshelper;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.TelephonyManager;

public class Contact {

	public Contact(long id, String name, String number) {
		this.id = id;
		this.name = name;
		this.number = number;
	}

	public static Contact getFromNumber(Context context, String number, ContactCache cache) {
		if(cache != null && cache.containsNumber(number)) {
			return cache.getFromNumber(number);
		}
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
		Cursor cursor = context.getContentResolver().query(uri, new String[] { PhoneLookup.DISPLAY_NAME, PhoneLookup._ID }, null, null, null);
		if(!cursor.moveToFirst()) {
			return null;
		}
		Long id = cursor.getLong(cursor.getColumnIndex(PhoneLookup._ID));
		Contact toreturn = new Contact(
				id,
				cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME)),
				number);
		cursor.close();
		if(cache != null) {
			cache.put(id, toreturn);
		}
		return toreturn;
	}

	public static Contact getFromId(Context context, long id, ContactCache cache) {
		if(cache != null && cache.containsId(id)) {
			return cache.getFromId(id);
		}
		Cursor cursor = context.getContentResolver().query(
				Constants.ALL_CANONICAL, null, "_id=" + id, null, null);
		if(!cursor.moveToFirst()) {
			return null;
		}
		Contact toreturn = getFromNumber(context, cursor.getString(1), cache);
		cursor.close();
		if(cache != null) {
			cache.put(id, toreturn);		
		}
		return toreturn;
	}

	public static Contact getMe(Context context) {
		TelephonyManager tele = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		return Contact.getFromNumber(context, tele.getLine1Number(), null);
	}

	private long id;
	private String name;
	private String number;

	public long getId() {
		return id;
	}

	public String getName() {
		if(name == null || name.trim().isEmpty()) {
			return number;
		}
		return name;
	}

	public String getNumber() {
		return number;
	}
	
	public Uri getContactUri(Context context) {
		long lookupId = getId();
		if(lookupId == 0) {
			lookupId = Contact.getMe(context).getId();
		}
		
		return ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, lookupId);
	}
	
	public Uri getProfilePic(Context context) {
		Cursor cursor = context.getContentResolver().query(getContactUri(context), new String[] { 
			ContactsContract.Contacts.PHOTO_URI }, null, null, null);
		cursor.moveToFirst();
		if(cursor.getType(0) == Cursor.FIELD_TYPE_NULL) {
			return null;
		}
		Uri toreturn = Uri.parse(cursor.getString(0));
		cursor.close();
		return toreturn;
	}
}
