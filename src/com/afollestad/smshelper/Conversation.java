package com.afollestad.smshelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import android.content.Context;
import android.database.Cursor;
import android.telephony.TelephonyManager;
import android.util.Base64;

public class Conversation implements Serializable {

	private static final long serialVersionUID = 6251878782078550182L;

	private static class Column {
		public final static String _ID = "_id"; // = 1950
		public final static String ADDRESS = "address"; // = 6512784578
		public final static String SUBJECT = "subject"; // = null
		public final static String BODY = "body";  //= So you can't drive now? That's all you had to say. And yeah I know i'm bipolar pms if you didnt know 
		public final static String TYPE = "type"; // = 1
		public final static String DATE = "date"; // = 1356294183575
		public final static String DATE_SENT = "date_sent"; // = 1356294155000
		public final static String READ = "read"; // = 1
		public final static String THREAD_ID = "thread_id"; // = 2
		public final static String STATUS = "status"; // = -1
		public final static String ERROR_CODE = "error_code"; // = 0
		public final static String LOCKED = "locked"; // = 0
	}
	private final static String[] PROJECTION = new String[] {
		Column._ID, Column.ADDRESS, Column.SUBJECT, Column.BODY, Column.TYPE,
		Column.DATE, Column.DATE_SENT, Column.READ, Column.THREAD_ID, Column.STATUS, Column.ERROR_CODE, Column.LOCKED
	};

	private Conversation() { }
	private static Conversation fromCursor(Context context, Cursor cursor, ContactCache cache) {
		Conversation toreturn = new Conversation();
		toreturn.id = cursor.getLong(cursor.getColumnIndex(Column._ID));
		toreturn.address = cursor.getString(cursor.getColumnIndex(Column.ADDRESS));
		toreturn.subject = cursor.getString(cursor.getColumnIndex(Column.SUBJECT));
		toreturn.snippet = cursor.getString(cursor.getColumnIndex(Column.BODY));
		toreturn.type = cursor.getInt(cursor.getColumnIndex(Column.TYPE));
		toreturn.date = cursor.getLong(cursor.getColumnIndex(Column.DATE));
		toreturn.dateSent = cursor.getLong(cursor.getColumnIndex(Column.DATE_SENT));
		toreturn.read = cursor.getInt(cursor.getColumnIndex(Column.READ));
		toreturn.threadId = cursor.getLong(cursor.getColumnIndex(Column.THREAD_ID));
		toreturn.status = cursor.getInt(cursor.getColumnIndex(Column.READ));
		toreturn.errorCode = cursor.getInt(cursor.getColumnIndex(Column.READ));
		toreturn.locked = cursor.getInt(cursor.getColumnIndex(Column.READ));
		
		toreturn.getMessages(context);
		Contact contact = toreturn.getRecipient(context, cache);
		toreturn.person = contact.getId();
		toreturn.name = contact.getName();
		
		return toreturn;
	}

	private static Conversation[] getAll(Context context, String where, ContactCache cache) {
		Cursor cursor = context.getContentResolver().query(
				Constants.ALL_CONVERSATIONS_URI, PROJECTION, where, null, null);
		ArrayList<Conversation> toreturn = new ArrayList<Conversation>();
		while (cursor.moveToNext()) {
			toreturn.add(Conversation.fromCursor(context, cursor, cache));
		}
		return toreturn.toArray(new Conversation[0]);
	}
	public static Conversation get(Context context, long threadId, ContactCache cache) {
		Conversation[] convos = getAll(context, Column.THREAD_ID + " = " + threadId, cache);
		if (convos == null || convos.length == 0) {
			return null;
		}
		return convos[0];
	}
	public static Conversation[] getAll(Context context, ContactCache cache) {
		return getAll(context, null, cache);
	}

	private long id;
	private long person;
	private String address;
	private String name;
	private String subject;
	private String snippet;
	private int type;
	private long date;
	private long dateSent;
	private int read;
	private long threadId;
	private int status;
	private int errorCode;
	private int locked;
	private ArrayList<Sms> smsMessages;
	
	private Contact getRecipient(Context context, ContactCache cache) {
		return Contact.getFromNumber(context, getAddress(), cache); 
	}

	/**
	 * Gets the database row ID this conversation was extracted from, use getThreadId() for the conversation ID.
	 */
	public long getRowId() {
		return id;
	}
	public long getPerson() {
		return person;
	}
	public String getAddress() {
		return address;
	}
	public String getName() {
		return name;
	}
	public String getSubject() {
		return subject;
	}
	public String getSnippet() { 
		return snippet;
	}
	public int getType() {
		return type;
	}
	public boolean isOutgoing() {
		return type == Sms.TYPE_SENT;
	}
	public Calendar getDate() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date);
		return cal;
	}
	public String getDateFriendly() {
		return Tools.friendlyTime(getDate());
	}
	public Calendar getDateSent() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(dateSent);
		return cal;
	}
	public String getDateSentFriendly() {
		return Tools.friendlyTime(getDateSent());
	}
	public boolean isRead() {
		return read == 1;
	}
	public long getThreadId() {
		return threadId;
	}
	public int getStatus() {
		return status;
	}
	public int getErrorCode() {
		return errorCode;
	}
	public boolean isLocked() {
		return locked == 1;
	}
	
	public ArrayList<Sms> getMessages(Context context) {
		return getMessages(context, true);
	}

	public ArrayList<Sms> getMessages(Context context, boolean cached) {
		if (!cached) {
			smsMessages = null;
		}
		if (smsMessages == null || smsMessages.size() == 0) {
			smsMessages = new ArrayList<Sms>();

			Cursor smsCursor = context.getContentResolver().query(
					Constants.SMS_ALL, null,
					Sms.Column.THREAD_ID + " = " + getThreadId(), null, null);
			while (smsCursor.moveToNext()) {
				smsMessages.add(Sms.fromCursor(smsCursor));
			}
			smsCursor.close();

			Cursor mmsCursor = context.getContentResolver().query(
					Constants.MMS_ALL, null,
					Sms.Column.THREAD_ID + " = " + getThreadId(), null, null);
			TelephonyManager manager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			while (mmsCursor.moveToNext()) {
				Sms mms = Sms.fromCursorMms(mmsCursor, context,
						manager.getLine1Number(), getAddress());
				smsMessages.add(mms);
			}
			mmsCursor.close();

			Collections.sort(smsMessages, new Sms.SmsReverseComparator());
		}
		return smsMessages;
	}

	/**
	 * Marks all SMS messages in the conversation as read.
	 */
	public void markMessagesRead(Context context) {
		Cursor smsCursor = context.getContentResolver().query(
				Constants.SMS_ALL,
				null,
				Sms.Column.READ + " = 0 AND " + Sms.Column.THREAD_ID + " = "
						+ getThreadId(), null, null);
		while (smsCursor.moveToNext()) {
			Sms.fromCursor(smsCursor).setIsRead(context, true, true);
		}
		smsCursor.close();

		Cursor mmsCursor = context.getContentResolver().query(
				Constants.MMS_ALL,
				null,
				Sms.Column.READ + " = 0 AND " + Sms.Column.THREAD_ID + " = "
						+ getThreadId(), null, null);
		while (mmsCursor.moveToNext()) {
			Sms.fromCursorMms(mmsCursor, context, null, null).setIsRead(
					context, true, true);
		}
		mmsCursor.close();

		if(smsMessages != null) {
			for (Sms msg : smsMessages) {
				msg.setIsRead(context, true, false);
			}
		}
	}

	/**
	 * Deletes the conversation from the content resolver, returns number of
	 * rows that were deleted (should return 1 if successful).
	 */
	public int delete(Context context) {
		int toreturn = 0;
		toreturn += context.getContentResolver().delete(Constants.SMS_ALL,
				Sms.Column.THREAD_ID + " = " + getThreadId(), null);
		toreturn += context.getContentResolver().delete(Constants.MMS_ALL,
				Sms.Column.THREAD_ID + " = " + getThreadId(), null);
		return toreturn;
	}

	public static Sms deserializeObject(String input) {
		try {
			byte[] data = Base64.decode(input, Base64.DEFAULT);
			ObjectInputStream ois = new ObjectInputStream(
					new ByteArrayInputStream(data));
			Object o = ois.readObject();
			ois.close();
			return (Sms) o;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String serializeObject() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			oos.close();
			return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
}