package com.afollestad.smshelper;

import java.util.Hashtable;

import android.content.Context;

public class ContactCache {

	public ContactCache(Context context) {
		nameCache = new Hashtable<Long, String>();
		addressCache = new Hashtable<Long, String>();
		me = Contact.getMe(context, this);
	}
	
	private Contact me;
	private Hashtable<Long, String> nameCache;
	private Hashtable<Long, String> addressCache;
	
	public Contact getFromNumber(String number) {
		if(!addressCache.containsValue(number)) {
			return null;
		}
		String[] values = addressCache.values().toArray(new String[0]);
		for(int i = 0; i < values.length; i++) {
			if(values[i].equals(number)) {
				return getFromId(addressCache.keySet().toArray(new Long[0])[i]);
			}
		}
		return null;
	}
	
	public Contact getFromId(Long id) {
		if(!containsId(id)) {
			return null;
		}
		System.out.println("Got contact from cache: " + id + " (" + addressCache.get(id).toString() + ")");
		String name = nameCache.get(id);
		String address = addressCache.get(id);
		if(id == 0) {
			id = me.getId();
		}
		return new Contact(id, name, address);
	}
	
	public void put(Long id, Contact contact) {
		if(contact == null) {
			return;
		}
		nameCache.put(id, contact.getName());
		addressCache.put(id, contact.getNumber());
	}
	
	public boolean containsId(Long id) {
		return (nameCache.containsKey(id) && addressCache.containsKey(id));
	}
	
	public boolean containsNumber(String number) {
		return addressCache.containsValue(number);
	}
}