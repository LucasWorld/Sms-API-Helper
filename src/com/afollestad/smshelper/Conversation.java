package com.afollestad.smshelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Base64;

public class Conversation implements Serializable {
	
	private static final long serialVersionUID = 6251878782078550182L;

	public static class Column {
		/**
		 * The ID of the conversation row.
		 */
		public static final String _ID = "_id";
		/**
         * The date at which the thread was created.
         */
        public static final String DATE = "date";

        /**
         * A string encoding of the recipient IDs of the recipients of the
         * message, in numerical order and separated by spaces.
         */
        public static final String RECIPIENT_IDS = "recipient_ids";

        /**
         * The message count of the thread.
         */
        public static final String MESSAGE_COUNT = "message_count";
        /**
         * Indicates whether all messages of the thread have been read.
         */
        public static final String READ = "read";
        /**
         * The snippet of the latest message in the thread.
         */
        public static final String SNIPPET = "snippet";
        /**
         * The charset of the snippet.
         */
        public static final String SNIPPET_CHARSET = "snippet_cs";
        /**
         * Type of the thread, either Threads.COMMON_THREAD or
         * Threads.BROADCAST_THREAD.
         */
        public static final String TYPE = "type";
        /**
         * Indicates whether there is a transmission error in the thread.
         */
        public static final String ERROR = "error";
        /**
         * Indicates whether this thread contains any attachments.
         */
        public static final String HAS_ATTACHMENT = "has_attachment";
	}

	private Conversation() {
	}

	private static Conversation fromCursor(Context context, Cursor cursor) {
		Conversation toreturn = new Conversation();
		toreturn.id = cursor.getLong(cursor.getColumnIndex(Column._ID));
		toreturn.date = cursor.getLong(cursor.getColumnIndex(Column.DATE)); 
		toreturn.messageCount = cursor.getLong(cursor.getColumnIndex(Column.MESSAGE_COUNT)); 
		toreturn.recipientIds = cursor.getString(cursor.getColumnIndex(Column.RECIPIENT_IDS));
		toreturn.snippet = cursor.getString(cursor.getColumnIndex(Column.SNIPPET));
		toreturn.snippetCs = cursor.getString(cursor.getColumnIndex(Column.SNIPPET_CHARSET));
		toreturn.read = cursor.getInt(cursor.getColumnIndex(Column.READ));
		toreturn.type = cursor.getInt(cursor.getColumnIndex(Column.TYPE));
		toreturn.error = cursor.getInt(cursor.getColumnIndex(Column.ERROR));
		toreturn.hasAttachment = cursor.getInt(cursor.getColumnIndex(Column.HAS_ATTACHMENT));
		return toreturn;
	}

	public static Conversation[] getAll(Context context) {
		Cursor cursor = context.getContentResolver().query(Constants.ALL_CONVERSATIONS_URI, 
				Constants.ALL_CONVERSATIONS_PROJECTION, null, null, null);
		ArrayList<Conversation> toreturn = new ArrayList<Conversation>(); 
        while(cursor.moveToNext()) {
        	toreturn.add(Conversation.fromCursor(context, cursor));
        }
		return toreturn.toArray(new Conversation[0]);
	}
	
	public static Conversation[] getAllUnread(Context context) {
		Cursor cursor = context.getContentResolver().query(Constants.ALL_CONVERSATIONS_URI, 
				Constants.ALL_CONVERSATIONS_PROJECTION, "read = 0", null, null);
		ArrayList<Conversation> toreturn = new ArrayList<Conversation>(); 
        while(cursor.moveToNext()) {
        	toreturn.add(Conversation.fromCursor(context, cursor));
        }
		return toreturn.toArray(new Conversation[0]);
	}
	
	private long id;
	private long date;
	private long messageCount;
	private String recipientIds;
	private String snippet;
	private String snippetCs;
	private int read;
	private int type;
	private int error;
	private int hasAttachment;
	private ArrayList<Sms> smsMessages;

	/**
	 * This only temporarily adds an SMS to this conversations cache, it doesn't save to the actual conversation;
	 * use {@link Sms#save(Context)} for that.
	 */
	public void addMessage(Sms msg) {
		smsMessages.add(msg);
	}
	
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
		return Contact.getFromId(context, this.getRecipientIds().get(0), cache);
	}
	
	public ArrayList<Long> getRecipientIds() {
		ArrayList<Long> toReturn = new ArrayList<Long>();
		for(String id : recipientIds.split(" ")) {
			toReturn.add(Long.parseLong(id));
		}
		return toReturn;
	}

	public String getSnippet() {
		return snippet;
	}

	public String getSnippetCharset() {
		return snippetCs;
	}

	public boolean isRead() {
		return (read == 1);
	}

	public int getType() {
		return type;
	}

	public boolean isError() {
		return (error == 1);
	}
	
	public boolean hasAttachment() {
		return (hasAttachment == 1);
	}
	
	public ArrayList<Sms> getSmsMessages(Context context) {
		if(smsMessages == null) {
			smsMessages = new ArrayList<Sms>();
			Uri uri = Uri.withAppendedPath(Constants.CONVERSATION_SMS_URI, Long.toString(this.getId()));
			Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
			while(cursor.moveToNext()) {
				smsMessages.add(Sms.fromCursor(cursor));
			}
			cursor.close();
		}
		return smsMessages;
	}
	
	/**
	 * Deletes the conversation from the content resolver, returns number of rows that were deleted (should return 1 if successful).
	 */
	public int delete(Context context) {
		Uri uri = Uri.withAppendedPath(Constants.CONVERSATION_SMS_URI, Long.toString(this.getId()));
		return context.getContentResolver().delete(uri, null, null);
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
