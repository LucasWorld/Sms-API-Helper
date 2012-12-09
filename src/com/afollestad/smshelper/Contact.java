package com.afollestad.smshelper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;

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
		cursor.moveToFirst();
		Long id = cursor.getLong(cursor.getColumnIndex(PhoneLookup._ID));
		Contact toreturn = new Contact(
				id,
				cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME)),
				number);
		cursor.close();
		cache.put(id, toreturn);
		return toreturn;
	}

	public static Contact getFromId(Context context, long id, ContactCache cache) {
		if(cache != null && cache.containsId(id)) {
			return cache.getFromId(id);
		}
		Cursor cursor = context.getContentResolver().query(
				Constants.ALL_CANONICAL, null, "_id=" + id, null, null);
		cursor.moveToFirst();
		Contact toreturn = getFromNumber(context, cursor.getString(1), cache);
		cursor.close();
		cache.put(id, toreturn);
		return toreturn;
	}

	private long id;
	private String name;
	private String number;

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getNumber() {
		return number;
	}
}
