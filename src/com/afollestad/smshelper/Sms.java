package com.afollestad.smshelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsMessage;
import android.util.Base64;

public class Sms implements Serializable, Comparable<Sms> {

	private static final long serialVersionUID = -6711776602850418239L;

	private Sms() { }
	public Sms(Cursor cursor) {
		id = cursor.getLong(cursor.getColumnIndex(Column._ID));
		threadId = cursor.getLong(cursor.getColumnIndex(Column.THREAD_ID)); 
		address = cursor.getString(cursor.getColumnIndex(Column.ADDRESS));
		person = cursor.getLong(cursor.getColumnIndex(Column.PERSON));
		date = cursor.getLong(cursor.getColumnIndex(Column.DATE));
		dateSent = cursor.getLong(cursor.getColumnIndex(Column.DATE_SENT));
		protocol = cursor.getInt(cursor.getColumnIndex(Column.PROTOCOL));
		read = cursor.getInt(cursor.getColumnIndex(Column.READ));
		status = cursor.getInt(cursor.getColumnIndex(Column.STATUS));
		type = cursor.getInt(cursor.getColumnIndex(Column.TYPE));
		replyPathPresent = cursor.getInt(cursor.getColumnIndex(Column.REPLY_PATH_PRESENT));
		subject = cursor.getString(cursor.getColumnIndex(Column.SUBJECT));
		body = cursor.getString(cursor.getColumnIndex(Column.BODY));
		serviceCenter = cursor.getString(cursor.getColumnIndex(Column.SERVICE_CENTER)); 
		locked = cursor.getInt(cursor.getColumnIndex(Column.LOCKED));
		errorCode = cursor.getInt(cursor.getColumnIndex(Column.ERROR_CODE));
		seen = cursor.getInt(cursor.getColumnIndex(Column.SEEN));
	}

	private long id;
	private long threadId;
	private String address;
	private long person;
	private long date;
	private long dateSent;
	private int protocol;
	private int read;
	private int status = Sms.STATUS_NONE;
	private int type = 1;
	private int replyPathPresent;
	private String serviceCenter;
	private String subject;
	private String body;
	private int locked;
	private int errorCode = Sms.ERROR_NONE;
	private int seen;
	private boolean isemail;

	public final static int TYPE_SENT = 2;
	public final static int TYPE_RECEIVED = 1;

	public static final int STATUS_NONE = -1;	
	public static final int STATUS_COMPLETE = 0;	
	public static final int STATUS_PENDING = 64;	
	public static final int STATUS_FAILED = 128;
	public static final int ERROR_NONE = 0;

	public static class Column {
		public final static String _ID = "_id";
		public final static String THREAD_ID = "thread_id";
		public final static String ADDRESS = "address";
		public final static String PERSON = "person";
		public final static String DATE = "date";
		public final static String DATE_SENT = "date_sent";
		public final static String PROTOCOL = "protocol";
		public final static String READ = "read";
		public final static String STATUS = "status";
		public final static String TYPE = "type";
		public final static String REPLY_PATH_PRESENT = "reply_path_present";
		public final static String SUBJECT = "subject";
		public final static String BODY = "body";
		public final static String SERVICE_CENTER = "service_center";
		public final static String LOCKED = "locked";
		public final static String ERROR_CODE = "error_code";
		public final static String SEEN = "seen";
	}

	public static Sms newSms(Context context, Long person, Long threadId, String body, boolean outgoing, ContactCache cache, boolean isemail) {
		Calendar now = Calendar.getInstance();
		Contact contact = Contact.getFromId(context, person, cache);
		if(contact == null) {
			return null;
		}
		Sms msg = new Sms();
		msg.date = now.getTimeInMillis();
		msg.dateSent = now.getTimeInMillis();
		msg.person = person;
		msg.address = contact.getAddress();
		msg.threadId = threadId;
		msg.body = body;
		msg.type = (outgoing ? TYPE_SENT : TYPE_RECEIVED);
		msg.isemail = isemail;
		return msg;
	}

	public static Sms fromStockSms(Context context, SmsMessage sms, boolean outgoing) {
		Sms toreturn = new Sms();
		toreturn.body = sms.getDisplayMessageBody();
		toreturn.address = sms.getDisplayOriginatingAddress();
		toreturn.isemail = sms.isEmail(); 
		toreturn.status = sms.getStatus();
		toreturn.date = sms.getTimestampMillis();
		toreturn.dateSent = sms.getTimestampMillis();
		toreturn.protocol = sms.getProtocolIdentifier();
		toreturn.replyPathPresent = sms.isReplyPathPresent() ? 1 : 0;
		toreturn.serviceCenter = sms.getServiceCenterAddress();
		toreturn.type = outgoing ? TYPE_SENT : TYPE_RECEIVED;
		return toreturn;
	}

	public ContentValues getContentValues() {
		ContentValues val = new ContentValues(13);
		val.put(Column.THREAD_ID, this.getThreadId());
		val.put(Column.ADDRESS, this.getAddress());
		val.put(Column.PERSON, this.getPerson());
		val.put(Column.DATE, this.getDate().getTimeInMillis());
		val.put(Column.DATE_SENT, this.getDateSent().getTimeInMillis());
		val.put(Column.READ, this.isRead() ? 1 : 0);
		val.put(Column.TYPE, this.getType());
		val.put(Column.BODY, this.getBody());
		val.put(Column.LOCKED, this.isLocked() ? 1 : 0);
		val.put(Column.SEEN, this.isSeen() ? 1 : 0);
		val.put(Column.STATUS, this.getStatus());
		val.put(Column.SUBJECT, this.getSubject());
		val.put(Column.ERROR_CODE, this.getErrorCode());
		return val;
	}

	public static Sms[] getAllUnread(Context context) {
		Cursor cursor = context.getContentResolver().query(Constants.SMS_ALL, 
				null, Column.READ + " = 0", null, null);
		ArrayList<Sms> unread = new ArrayList<Sms>(); 
		while(cursor.moveToNext()) {
			if(cursor.getInt(cursor.getColumnIndex(Sms.Column.READ)) == 0) {
				unread.add(new Sms(cursor));
			}
		}
		cursor.close();
		return unread.toArray(new Sms[0]);
	}

	public long getId() {
		return id;
	}

	public long getThreadId() {
		return threadId;
	}

	public String getAddress() {
		return address;
	}

	public long getPerson() {
		return person;
	}

	public Contact getContact(Context context, ContactCache cache) {
		if(this.isEmail()) {
			return Contact.getFromEmail(context, this.getAddress(), cache);
		} else {
			return Contact.getFromNumber(context, this.getAddress(), cache);
		}
	}

	public Calendar getDate() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date);
		return cal;
	}

	public Calendar getDateSent() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(dateSent);
		return cal;
	}

	public int getProtocol() {
		return protocol;
	}

	public boolean isRead() {
		return (read == 1);
	}

	public int getStatus() {
		return status;
	}

	public int getType() { 
		return type;
	}

	public boolean isOutgoing() {
		return (getType() == Sms.TYPE_SENT);
	}

	public boolean isReplyPathPresent() { 
		return (replyPathPresent == 1);
	}

	public String getSubject() {
		return subject;
	}

	public String getBody() { 
		return body;
	}

	public String getServiceCenter() {
		return serviceCenter;
	}

	public boolean isLocked() {
		return (locked == 1);
	}

	public int getErrorCode() { 
		return errorCode;
	}

	public boolean isSeen() { 
		return (seen == 1);
	}

	public boolean isError() {
		return (errorCode < -1 || errorCode > 0) && isOutgoing();
	}

	public boolean isPending() {
		return (status == Sms.STATUS_PENDING);
	}

	public boolean isEmail() {
		return isemail;
	}

	public int setIsRead(Context context, boolean read) {
		this.read = read ? 1 : 0;
		ContentValues values = new ContentValues(1);
		values.put(Column.READ, this.read);
		return update(context, values);
	}

	public int update(Context context, ContentValues values) {
		return context.getContentResolver().update(Constants.SMS_ALL, 
				getContentValues(), Column._ID + "=?", new String[] { Long.toString(getId()) });
	}
	
	public int setErrorAndStatus(Context context, int errorCode, int statusCode, boolean update) {
		this.errorCode = errorCode;
		this.status = statusCode;
		if(update) {
			ContentValues values = new ContentValues(2);
			values.put(Column.ERROR_CODE, errorCode);
			values.put(Column.STATUS, statusCode);
			return context.getContentResolver().update(Constants.SMS_ALL, values, Column._ID + "=" + getId(), null);
		} else {
			return -1;
		}
	}

	/**
	 * Places in the message in the inbox/sentbox (based on whether it's outgoing or not).
	 */
	public Sms saveInbox(Context context) {
		Uri uri = isOutgoing() ? Constants.SMS_SENT : Constants.SMS_INBOX;
		Uri row = context.getContentResolver().insert(uri, getContentValues());
		Cursor cursor = context.getContentResolver().query(row, null, null, null, null);
		cursor.moveToFirst();
		Sms toreturn = new Sms(cursor);
		cursor.close();
		return toreturn;
	}

	/**
	 * This doesn't work yet.
	 */
	public Uri saveDraft(Context context) {
		return context.getContentResolver().insert(Constants.SMS_DRAFTS, getContentValues());
	}

	public static Sms deserializeObject(String input) {
		try {
			byte[] data = Base64.decode(input, Base64.DEFAULT);
			ObjectInputStream ois = new ObjectInputStream( 
					new ByteArrayInputStream(data));
			Object o = ois.readObject();
			ois.close();
			return (Sms)o;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String serializeObject() {
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			oos.close();
			return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
		} catch(Exception e){
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public int compareTo(Sms other) {
		return getDate().compareTo(other.getDate());
	}

	public int delete(Context context) {
		return context.getContentResolver().delete(Constants.SMS_ALL, "thread_id=" + getThreadId() + " and _id=" + getId(), null);
	}
	
	public static class SmsComparator implements java.util.Comparator<Sms> {

		@Override
		public int compare(Sms left, Sms right) {
			return left.getDate().compareTo(right.getDate());
		}
	}

	public static class SmsReverseComparator implements java.util.Comparator<Sms> {

		@Override
		public int compare(Sms left, Sms right) {
			return right.getDate().compareTo(left.getDate());
		}
	}
}