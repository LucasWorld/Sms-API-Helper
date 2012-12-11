package com.afollestad.smshelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsMessage;
import android.util.Base64;

public class Sms implements Serializable, Comparable<Sms> {

	private static final long serialVersionUID = -6711776602850418239L;

	private Sms() {		
	}
	
	private long id;
	private long threadId;
	private String address;
	private long person;
	private long date;
	private long dateSent;
	private int protocol;
	private int read;
	private int status = -1;
	private int type = 1;
	private int replyPathPresent;
	private String serviceCenter;
	private String subject;
	private String body;
	private int locked;
	private int errorCode;
	private int seen;
	
	public final static int TYPE_SENT = 2;
	public final static int TYPE_RECEIVED = 1;
	
	public static class Column {
		public final static String ID = "_id";
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
	
	public static Sms newSms(Context context, Long person, Long threadId, String body, boolean outgoing, ContactCache cache) {
		Calendar now = Calendar.getInstance();
		Contact contact = Contact.getFromId(context, person, cache);
		if(contact == null) {
			return null;
		}
		Sms msg = new Sms();
		msg.date = now.getTimeInMillis();
		msg.dateSent = now.getTimeInMillis();
		msg.person = person;
		msg.address = contact.getNumber();
		msg.threadId = threadId;
		msg.body = body;
		msg.type = (outgoing ? TYPE_SENT : TYPE_RECEIVED);
		return msg;
	}
	
	public static Sms fromCursor(Cursor cursor) {
		Sms toreturn = new Sms();
		toreturn.id = cursor.getLong(cursor.getColumnIndex(Column.ID));
		toreturn.threadId = cursor.getLong(cursor.getColumnIndex(Column.THREAD_ID)); 
		toreturn.address = cursor.getString(cursor.getColumnIndex(Column.ADDRESS));
		toreturn.person = cursor.getLong(cursor.getColumnIndex(Column.PERSON));
		toreturn.date = cursor.getLong(cursor.getColumnIndex(Column.DATE));
		toreturn.dateSent = cursor.getLong(cursor.getColumnIndex(Column.DATE_SENT));
		toreturn.protocol = cursor.getInt(cursor.getColumnIndex(Column.PROTOCOL));
		toreturn.read = cursor.getInt(cursor.getColumnIndex(Column.READ));
		toreturn.status = cursor.getInt(cursor.getColumnIndex(Column.STATUS));
		toreturn.type = cursor.getInt(cursor.getColumnIndex(Column.TYPE));
		toreturn.replyPathPresent = cursor.getInt(cursor.getColumnIndex(Column.REPLY_PATH_PRESENT));
		toreturn.subject = cursor.getString(cursor.getColumnIndex(Column.SUBJECT));
		toreturn.body = cursor.getString(cursor.getColumnIndex(Column.BODY));
		toreturn.serviceCenter = cursor.getString(cursor.getColumnIndex(Column.SERVICE_CENTER)); 
		toreturn.locked = cursor.getInt(cursor.getColumnIndex(Column.LOCKED));
		toreturn.errorCode = cursor.getInt(cursor.getColumnIndex(Column.ERROR_CODE));
		toreturn.seen = cursor.getInt(cursor.getColumnIndex(Column.SEEN));
		return toreturn;
	}
	
	public static String stripAddress(String address) {
		return address.replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
	}
	
	public static Sms fromStockSms(Context context, SmsMessage sms, boolean outgoing) {
		/*TelephonyManager tele = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String mynumber = tele.getLine1Number();*/
		Sms toreturn = new Sms();
		toreturn.body = sms.getDisplayMessageBody();
		toreturn.address = sms.getDisplayOriginatingAddress();
		toreturn.status = sms.getStatus();
		toreturn.date = sms.getTimestampMillis();
		toreturn.dateSent = sms.getTimestampMillis();
		toreturn.protocol = sms.getProtocolIdentifier();
		toreturn.replyPathPresent = sms.isReplyPathPresent() ? 1 : 0;
		toreturn.serviceCenter = sms.getServiceCenterAddress();
		toreturn.type = outgoing ? TYPE_SENT : TYPE_RECEIVED;
		return toreturn;
	}
	
	public ContentValues getContentValues(boolean draft) {
		ContentValues val = new ContentValues();
		if(this.getId() > 0) {
			val.put(Column.ID, this.getId());
		}
		val.put(Column.THREAD_ID, this.getThreadId());
		val.put(Column.ADDRESS, this.getAddress());
		val.put(Column.PERSON, this.getPerson());
		val.put(Column.DATE, this.getDate().getTimeInMillis());
		if(!draft) {
			val.put(Column.DATE_SENT, this.getDateSent().getTimeInMillis());
		}
		val.put(Column.READ, this.isRead() ? 1 : 0);
		val.put(Column.TYPE, draft ? 3 : this.getType());
		val.put(Column.BODY, this.getBody());
		val.put(Column.LOCKED, this.isLocked() ? 1 : 0);
		val.put(Column.SEEN, this.isSeen() ? 1 : 0);
		val.put(Column.SERVICE_CENTER, this.getServiceCenter());
		val.put(Column.PROTOCOL, this.getProtocol());
		val.put(Column.STATUS, this.getStatus());
		val.put(Column.REPLY_PATH_PRESENT, this.isReplyPathPresent() ? 1 : 0);
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
				unread.add(Sms.fromCursor(cursor));
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
		return Contact.getFromNumber(context, this.getAddress(), cache);
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
	
	/**
	 * Gets whether it was sent by you or received by you based on the
	 * value of {@link #getType()} (2 means sent, 1 means received).
	 */
	public boolean isOutgoing() {
		return (getType() == 2);
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
	
	public int setIsRead(Context context, boolean read) {
		Uri uri = isOutgoing() ? Constants.SMS_SENT : Constants.SMS_INBOX;
		this.read = read ? 1 : 0;
		return context.getContentResolver().update(uri, getContentValues(false), Column.ID + " = ?", 
				new String[] { Long.toString(this.getId()) });
	}
	
	public int setIsSeen(Context context, boolean seen) {
		Uri uri = isOutgoing() ? Constants.SMS_SENT : Constants.SMS_INBOX;
		this.seen = seen ? 1 : 0;
		return context.getContentResolver().update(uri, getContentValues(false), Column.ID + " = ?", 
				new String[] { Long.toString(this.getId()) });
	}
	
	public int setIsLocked(Context context, boolean locked) {
		Uri uri = isOutgoing() ? Constants.SMS_SENT : Constants.SMS_INBOX;
		this.locked = locked ? 1 : 0;
		return context.getContentResolver().update(uri, getContentValues(false), Column.ID + " = ?", 
				new String[] { Long.toString(this.getId()) });
	}
	
	public Sms save(Context context) {
		Uri uri = isOutgoing() ? Constants.SMS_SENT : Constants.SMS_INBOX;
		Uri row = context.getContentResolver().insert(uri, getContentValues(false));
		Cursor cursor = context.getContentResolver().query(row, null, null, null, null);
		cursor.moveToFirst();
		Sms toreturn = Sms.fromCursor(cursor);
		cursor.close();
		return toreturn;
	}
	
	/**
	 * This doesn't work yet.
	 */
	public Uri saveDraft(Context context) {
		return context.getContentResolver().insert(Constants.SMS_DRAFTS, getContentValues(true));
	}
	
	public Uri saveError(Context context) {
		return context.getContentResolver().insert(Constants.SMS_FAILED, getContentValues(false));
	}
	
	public int deleteError(Context context) {
		return context.getContentResolver().delete(Constants.SMS_FAILED, Sms.Column.ID + " = ?", new String[] { Long.toString(getId()) });
	}
	
	public int delete(Context context) {
		return context.getContentResolver().delete(Constants.SMS_ALL, Sms.Column.ID + " = ?", new String[] { Long.toString(getId()) });
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
	
	public static class SmsComparator implements Comparator<Sms> {

		@Override
		public int compare(Sms left, Sms right) {
			return left.compareTo(right);
		}
	}
	
	public static class SmsReverseComparator implements Comparator<Sms> {

		@Override
		public int compare(Sms left, Sms right) {
			return right.compareTo(left);
		}
	}

	@Override
	public int compareTo(Sms other) {
		return getDate().compareTo(other.getDate());
	}
}