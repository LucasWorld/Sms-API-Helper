package com.afollestad.smshelper;

import java.util.Hashtable;

import android.content.Context;

public class ContactCache {

	public ContactCache(Context context) {
		idToNumber = new Hashtable<Long, String>();
		cache = new Hashtable<String, Contact>();
	}
	
	private Hashtable<Long, String> idToNumber;
	private Hashtable<String, Contact> cache;
	
	public Contact getFromAddress(String address) {
		if(!cache.containsKey(address)) {
			return null;
		}
		return cache.get(address);
	}
	
	public Contact getFromId(Long id) {
		if(!idToNumber.containsKey(id)) {
			return null;
		}
		String number = idToNumber.get(id);
		if(!cache.containsKey(number)) {
			return null;
		}
		return cache.get(number);
	}
	
	public void put(Long id, Contact contact) {
		if(contact == null) {
			return;
		}
		idToNumber.put(id, contact.getAddress());
		cache.put(contact.getAddress(), contact);
	}
	
	public boolean containsId(Long id) {
		return idToNumber.containsKey(id);
	}
	
	public boolean containsAddress(String address) {
		return cache.containsKey(address);
	}
}