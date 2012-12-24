package com.afollestad.smshelper;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.PhoneLookup;

public class Contact implements Serializable {
	
	private static final long serialVersionUID = 7900545052474377060L;
	
	public final static String TYPE_GOOGLE_ACCOUNT = "com.google";
	private final static String TYPE_NORMAL = "contact";	
	
	public Contact(long id, String name, String address, boolean isemail) {
		this.id = id;
		this.name = name;
		this.address = address;
		this.isemail = isemail;
	}

	private static Contact fromProfileCursor(Cursor cursor) {
		String type = cursor.getString(cursor.getColumnIndex(ContactsContract.SyncState.ACCOUNT_TYPE));
		return new Contact(
				cursor.getLong(cursor.getColumnIndex("contact_id")),
				cursor.getString(cursor.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME)),
				cursor.getString(cursor.getColumnIndex(ContactsContract.SyncState.ACCOUNT_NAME)),
				true)
		.setType(type);

	}
	
	public static Contact getFromNumber(Context context, String number, ContactCache cache) {
		if(number == null) {
			return new Contact(0l, "Unknown Name", "Unknown Number", false);
		}
		if(cache != null && cache.containsAddress(number)) {
			return cache.getFromAddress(number);
		}
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
		Cursor cursor = context.getContentResolver().query(uri, new String[] { PhoneLookup.DISPLAY_NAME, PhoneLookup._ID }, null, null, null);
		if(!cursor.moveToFirst()) {
			//This number is not in any contacts on the local device
			return new Contact(0l, number, number, false);
		}
		Long id = cursor.getLong(cursor.getColumnIndex(PhoneLookup._ID));
		Contact toreturn = new Contact(
				id,
				cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME)),
				number,
				false);
		cursor.close();
		if(cache != null) {
			cache.put(id, toreturn);
		}
		return toreturn;
	}
	
	public static Contact getFromEmail(Context context, String address, ContactCache cache) {
		if(cache != null && cache.containsAddress(address)) {
			return cache.getFromAddress(address);
		}
		Uri uri = Uri.withAppendedPath(Email.CONTENT_FILTER_URI, Uri.encode(address));
		Cursor cursor = context.getContentResolver().query(uri, new String[] { Email._ID, Email.DISPLAY_NAME }, null, null, null);
		if(!cursor.moveToFirst()) {
			//This number is not in any contacts on the local device
			return new Contact(0l, address, address, true);
		}
		Long id = cursor.getLong(cursor.getColumnIndex(Email._ID));
		Contact toreturn = new Contact(
				id,
				cursor.getString(cursor.getColumnIndex(Email.DISPLAY_NAME)),
				address,
				true);
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
		Cursor cursor = context.getContentResolver().query(Constants.ALL_CANONICAL, null, "_id = " + id, null, null);
		if(!cursor.moveToFirst()) {
			return new Contact(id, "Unknown Name", "Unknown Number", false);
		}
		Contact toreturn = getFromNumber(context, cursor.getString(1), cache);
		if(toreturn.getId() == 0) {
			// Don't allow it to be cached if the ID is 0
			return toreturn;
		}
		cursor.close();
		if(cache != null) {
			cache.put(id, toreturn);		
		}
		return toreturn;
	}

	public static Contact getMe(Context context, ContactCache cache) {
		if(cache.containsId(-1l)) {
			return cache.getFromId(-1l);
		}
		Contact[] profiles = getProfiles(context);
		for(Contact acc : profiles) {
			if(acc.isGoogleAccount()) {
				cache.put(-1l, acc);
				return acc;
			}
		}
		return null;
	}
	
	public static Contact[] getProfiles(Context context) {
		ArrayList<Contact> toreturn = new ArrayList<Contact>();
		Cursor c = context.getContentResolver().query(ContactsContract.Profile.CONTENT_RAW_CONTACTS_URI, null, null, null, null);
		while(c.moveToNext()) {
			toreturn.add(Contact.fromProfileCursor(c));
		}
		c.close();
		return toreturn.toArray(new Contact[0]);
	}
	
	private long id;
	private String name;
	private String address;
	private boolean isemail;
	private String type = TYPE_NORMAL;

	public long getId() {
		return id;
	}

	public String getName() {
		if(name == null || name.trim().isEmpty()) {
			return address;
		}
		return name;
	}

	public String getAddress() {
		return address;
	}
	
	public boolean isEmail() {
		return isemail;
	}

	public Contact setType(String type) {
		this.type = type;
		return this;
	}
	
	public String getType() { 
		return type;
	}
	
	public boolean isGoogleAccount() {
		if(getType() == null) {
			return false;
		}
		return getType().equals(TYPE_GOOGLE_ACCOUNT);
	}
	
	public static Uri getContactUri(Long id) {
		return ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
	}
}
