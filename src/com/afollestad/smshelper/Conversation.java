package com.afollestad.smshelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.telephony.TelephonyManager;
import android.util.Base64;

public class Conversation implements Serializable {

	private static final long serialVersionUID = 6251878782078550182L;

	private static class Column {
		public static final String _ID = "_id";
		public static final String DATE = "date";
		public static final String RECIPIENT_IDS = "recipient_ids";
		public static final String MESSAGE_COUNT = "message_count";
		public static final String READ = "read";
		public static final String TYPE = "type";
		public static final String ERROR = "error";
		public static final String HAS_ATTACHMENT = "has_attachment";
		public static final String SNIPPET = "snippet";
	}

	private Conversation() {
	}

	private static Conversation fromCursor(Context context, Cursor cursor) {
		Conversation toreturn = new Conversation();
		toreturn.id = cursor.getLong(cursor.getColumnIndex(Column._ID));
		toreturn.date = cursor.getLong(cursor.getColumnIndex(Column.DATE)); 
		toreturn.messageCount = cursor.getLong(cursor.getColumnIndex(Column.MESSAGE_COUNT)); 
		toreturn.recipientIds = cursor.getString(cursor.getColumnIndex(Column.RECIPIENT_IDS));
		toreturn.read = cursor.getInt(cursor.getColumnIndex(Column.READ));
		toreturn.type = cursor.getInt(cursor.getColumnIndex(Column.TYPE));
		toreturn.error = cursor.getInt(cursor.getColumnIndex(Column.ERROR));
		toreturn.hasAttachment = cursor.getInt(cursor.getColumnIndex(Column.HAS_ATTACHMENT));
		toreturn.snippet = cursor.getString(cursor.getColumnIndex(Column.SNIPPET));
		return toreturn;
	}

	public ContentValues getContentValues() {
		ContentValues val = new ContentValues(10);
		val.put(Column._ID, this.getId());
		val.put(Column.DATE, this.getDate().getTimeInMillis());
		val.put(Column.MESSAGE_COUNT, this.getMessageCount());
		String recipientIds = "";
		for(Long id : this.getRecipientIds()) {
			recipientIds += (id + " ");
		}
		val.put(Column.RECIPIENT_IDS, recipientIds);
		val.put(Column.READ, this.isRead() ? 1 : 0);
		val.put(Column.TYPE, this.getType());
		val.put(Column.ERROR, this.isError() ? 1 : 0);
		val.put(Column.HAS_ATTACHMENT, this.hasAttachment() ? 1 : 0);
		return val;
	}

	private static Conversation[] getAll(Context context, String where) {
		Cursor cursor = context.getContentResolver().query(Constants.ALL_CONVERSATIONS_URI, 
				null, where, null, null);
		ArrayList<Conversation> toreturn = new ArrayList<Conversation>();
		while(cursor.moveToNext()) {
			toreturn.add(Conversation.fromCursor(context, cursor));
		}
		return toreturn.toArray(new Conversation[0]);
	}

	public static Conversation get(Context context, long threadId) {
		Conversation[] convos = getAll(context, Column._ID + " = " + threadId);
		if(convos == null || convos.length == 0) {
			return null;
		}
		return convos[0];
	}

	public static Conversation[] getAll(Context context) {
		return getAll(context, null);
	}

	private long id;
	private long date;
	private long messageCount;
	private String recipientIds;
	private int read;
	private int type;
	private int error;
	private int hasAttachment;
	private String snippet;
	private ArrayList<Sms> smsMessages;

	public long getId() {
		return id;
	}

	public Calendar getDate() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date);
		return cal;
	}

	public long getMessageCount() {
		return messageCount;
	}

	public Contact getRecipient(Context context, ContactCache cache) {
		return Contact.getFromId(context, getRecipientIds().get(0), cache);
	}

	public ArrayList<Long> getRecipientIds() {
		ArrayList<Long> toReturn = new ArrayList<Long>();
		for(String id : recipientIds.split(" ")) {
			toReturn.add(Long.parseLong(id));
		}
		return toReturn;
	}

	public String getSnippet() {
		if(snippet != null && snippet.trim().isEmpty()) {
			snippet = null;
		}
		return snippet;
	}

	public boolean isRead() {
		return (read == 1);
	}

	public int getType() {
		return type;
	}

	public boolean isOutgoing(Context context) {
		ArrayList<Sms> msges = getMessages(context); 
		if(msges.size() == 0) {
			return false;
		}
		return msges.get(0).isOutgoing();
	}
	
	public boolean isError() {
		return (error < -1 || error > 0);
	}

	public boolean hasAttachment() {
		return (hasAttachment == 1);
	}

	public ArrayList<Sms> getMessages(Context context) {
		return getMessages(context, true);
	}

	public ArrayList<Sms> getMessages(Context context, boolean cached) {
		if(!cached) {
			smsMessages = null;
		}
		if(smsMessages == null || smsMessages.size() == 0) {
			smsMessages = new ArrayList<Sms>();
			
			Cursor smsCursor = context.getContentResolver().query(Constants.SMS_ALL, null, 
					Sms.Column.THREAD_ID + " = " + getId(), null, null);
			while(smsCursor.moveToNext()) {
				smsMessages.add(Sms.fromCursor(smsCursor));
			}
			smsCursor.close();

			Cursor mmsCursor = context.getContentResolver().query(Constants.MMS_ALL, null, 
					Sms.Column.THREAD_ID + " = " + getId(), null, null);
			TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			String address = smsMessages.get(0).getAddress();
			while(mmsCursor.moveToNext()) {
				Sms mms = Sms.fromCursorMms(mmsCursor, context, manager.getLine1Number(), address);
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
		Cursor smsCursor = context.getContentResolver().query(Constants.SMS_ALL, null, 
				Sms.Column.READ + " = 0 AND " + Sms.Column.THREAD_ID + " = " + getId(), null, null);
		while(smsCursor.moveToNext()) {
			Sms.fromCursor(smsCursor).setIsRead(context, true, true);
		}
		smsCursor.close();

		Cursor mmsCursor = context.getContentResolver().query(Constants.MMS_ALL, null, 
				Sms.Column.READ + " = 0 AND " + Sms.Column.THREAD_ID + " = " + getId(), null, null);
		while(mmsCursor.moveToNext()) {
			Sms.fromCursorMms(mmsCursor, context, null, null).setIsRead(context, true, true);
		}
		mmsCursor.close();
		
		for(Sms msg : smsMessages) {
			msg.setIsRead(context, true, false);
		}
	}

	/**
	 * Deletes the conversation from the content resolver, returns number of rows that were deleted (should return 1 if successful).
	 */
	public int delete(Context context) {
		int toreturn = 0;
		toreturn += context.getContentResolver().delete(Constants.SMS_ALL, Sms.Column.THREAD_ID + " = " + getId(), null);
		toreturn += context.getContentResolver().delete(Constants.MMS_ALL, Sms.Column.THREAD_ID + " = " + getId(), null);
		return toreturn;
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
}